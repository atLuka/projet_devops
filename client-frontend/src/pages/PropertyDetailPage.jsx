import React, { useState, useEffect } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import {
  getProperty,
  getPropertyPhotos,
  createReservation,
  getReservedDates,
} from "../api";
import ServiceUnavailable from "../components/ServiceUnavailable";
import DatePicker, { registerLocale } from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import fr from "date-fns/locale/fr";

registerLocale("fr", fr);

const TYPE_LABELS = {
  APARTMENT: "Appartement",
  HOUSE: "Maison",
  STUDIO: "Studio",
  LOFT: "Loft",
};

function getDisabledDates(reservedRanges) {
  const dates = [];
  for (const range of reservedRanges) {
    let current = new Date(range.startDate);
    const end = new Date(range.endDate);
    while (current < end) {
      dates.push(new Date(current));
      current.setDate(current.getDate() + 1);
    }
  }
  return dates;
}

function PropertyDetailPage({ user }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [property, setProperty] = useState(null);
  const [photos, setPhotos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [serviceDown, setServiceDown] = useState(false);
  const [disabledDates, setDisabledDates] = useState([]);

  const parseDate = (str) => {
    if (!str) return null;
    const d = new Date(str + "T00:00:00");
    return isNaN(d.getTime()) ? null : d;
  };

  const [startDate, setStartDate] = useState(
    parseDate(searchParams.get("startDate")),
  );
  const [endDate, setEndDate] = useState(
    parseDate(searchParams.get("endDate")),
  );
  const [reservationMsg, setReservationMsg] = useState("");
  const [reservationError, setReservationError] = useState(false);
  const [reserving, setReserving] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const prop = await getProperty(id);
        setProperty(prop);
        try {
          const ph = await getPropertyPhotos(id);
          setPhotos(Array.isArray(ph) ? ph : []);
        } catch {}
        try {
          const reserved = await getReservedDates(id);
          setDisabledDates(
            getDisabledDates(Array.isArray(reserved) ? reserved : []),
          );
        } catch {}
      } catch (err) {
        if (err.status === 503) {
          setServiceDown(true);
        } else {
          setError("Impossible de charger les détails du logement");
        }
      }
      setLoading(false);
    };
    fetchData();
  }, [id]);

  const formatDate = (date) => {
    if (!date) return "";
    return date.toISOString().split("T")[0];
  };

  const handleReservation = async (e) => {
    e.preventDefault();
    setReservationMsg("");
    setReservationError(false);
    setReserving(true);
    try {
      const res = await fetch("/api/reservations", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({
          propertyId: parseInt(id),
          tenantId: user.userId || user.email,
          startDate: formatDate(startDate),
          endDate: formatDate(endDate),
          tenantEmail: localStorage.getItem('userEmail') || user.email || '',
        }),
      });
      if (res.status === 409) {
        setReservationMsg("Ces dates sont déjà réservées.");
        setReservationError(true);
      } else if (!res.ok) {
        throw new Error("Failed");
      } else {
        setReservationMsg("Réservation effectuée !");
        setReservationError(false);
        setStartDate(null);
        setEndDate(null);
        try {
          const reserved = await getReservedDates(id);
          setDisabledDates(
            getDisabledDates(Array.isArray(reserved) ? reserved : []),
          );
        } catch {}
      }
    } catch (err) {
      setReservationMsg(
        "Impossible de créer la réservation. Veuillez réessayer.",
      );
      setReservationError(true);
    }
    setReserving(false);
  };

  if (loading) return <p>Chargement...</p>;
  if (serviceDown)
    return (
      <ServiceUnavailable
        message="Le service des logements est temporairement indisponible."
        onRetry={() => window.location.reload()}
      />
    );
  if (error) return <p className="error">{error}</p>;
  if (!property) return <p>Logement introuvable</p>;

  const today = new Date();
  const minEndDate = startDate
    ? new Date(startDate.getTime() + 86400000)
    : new Date(today.getTime() + 86400000);

  return (
    <div className="property-detail">
      <button onClick={() => navigate(-1)} className="btn-back">
        Retour
      </button>

      <h1>{property.title || property.name || "Logement"}</h1>

      <div className="property-info">
        <p>
          <strong>Type :</strong> {TYPE_LABELS[property.type] || property.type}
        </p>
        <p>
          <strong>Localisation :</strong>{" "}
          {property.location || property.address}
        </p>
        <p>
          <strong>Prix :</strong>{" "}
          {property.price ? `${property.price} € / nuit` : "Prix sur demande"}
        </p>
        {property.description && (
          <p>
            <strong>Description :</strong> {property.description}
          </p>
        )}
        {property.surface && (
          <p>
            <strong>Surface :</strong> {property.surface} m²
          </p>
        )}
        {property.rooms && (
          <p>
            <strong>Pièces :</strong> {property.rooms}
          </p>
        )}
      </div>

      {photos.length > 0 && (
        <div className="photo-gallery">
          <h2>Photos</h2>
          <div className="photos-grid">
            {photos.map((photo, i) => (
              <img key={i} src={photo.downloadUrl} alt={`Photo ${i + 1}`} />
            ))}
          </div>
        </div>
      )}

      {user ? (
        <div className="reservation-form-section">
          <h2>Réserver ce logement</h2>
          <p
            style={{
              color: "#888",
              fontSize: "0.9rem",
              marginBottom: "0.5rem",
            }}
          >
            Dates indisponibles affichées en gris
          </p>
          <form className="reservation-form" onSubmit={handleReservation}>
            {reservationMsg && (
              <p className={reservationError ? "error" : "success"}>
                {reservationMsg}
              </p>
            )}
            <label>Date d'arrivée</label>
            <DatePicker
              selected={startDate}
              onChange={(date) => {
                setStartDate(date);
                if (endDate && date && endDate <= date) {
                  setEndDate(null);
                }
              }}
              placeholderText="Date d'arrivée"
              dateFormat="dd/MM/yyyy"
              locale="fr"
              minDate={today}
              excludeDates={disabledDates}
              className="datepicker-input"
              required
            />
            <label>Date de départ</label>
            <DatePicker
              selected={endDate}
              onChange={(date) => setEndDate(date)}
              placeholderText="Date de départ"
              dateFormat="dd/MM/yyyy"
              locale="fr"
              minDate={minEndDate}
              excludeDates={disabledDates}
              className="datepicker-input"
              required
            />
            <button
              type="submit"
              className="btn-primary"
              disabled={reserving || !startDate || !endDate}
            >
              {reserving ? "Réservation en cours..." : "Réserver"}
            </button>
          </form>
        </div>
      ) : (
        <p className="login-prompt">
          <a href="/login">Connectez-vous pour réserver</a>
        </p>
      )}
    </div>
  );
}

export default PropertyDetailPage;
