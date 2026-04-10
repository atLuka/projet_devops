import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { getProperties, getPropertyPhotos } from "../api";
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

function HomePage() {
  const [properties, setProperties] = useState([]);
  const [location, setLocation] = useState("");
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);
  const [type, setType] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [serviceDown, setServiceDown] = useState(false);

  const formatDate = (date) => {
    if (!date) return undefined;
    return date.toISOString().split("T")[0];
  };

  const fetchProperties = async (filters) => {
    setLoading(true);
    setError("");
    setServiceDown(false);
    try {
      const data = await getProperties(filters);
      const props = Array.isArray(data) ? data : [];
      // Load first photo for each property
      const propsWithPhotos = await Promise.all(
        props.map(async (p) => {
          try {
            const photos = await getPropertyPhotos(p.id);
            return { ...p, firstPhoto: photos?.[0]?.downloadUrl || null };
          } catch {
            return { ...p, firstPhoto: null };
          }
        }),
      );
      setProperties(propsWithPhotos);
    } catch (err) {
      if (err.status === 503) {
        setServiceDown(true);
      } else {
        setError(
          "Impossible de charger les logements. L'API est-elle accessible ?",
        );
      }
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchProperties({});
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    const filters = {};
    if (location) filters.location = location;
    if (type) filters.type = type;
    if (maxPrice) filters.maxPrice = maxPrice;
    if (startDate) filters.startDate = formatDate(startDate);
    if (endDate) filters.endDate = formatDate(endDate);
    fetchProperties(filters);
  };

  const buildPropertyLink = (propertyId) => {
    const params = new URLSearchParams();
    if (startDate) params.append("startDate", formatDate(startDate));
    if (endDate) params.append("endDate", formatDate(endDate));
    const query = params.toString();
    return `/properties/${propertyId}${query ? "?" + query : ""}`;
  };

  const today = new Date();
  const minEndDate = startDate
    ? new Date(startDate.getTime() + 86400000)
    : new Date(today.getTime() + 86400000);

  return (
    <div>
      <h1 className="hero-heading">Trouvez votre logement idéal</h1>

      <div className="search-bar">
        <form className="search-form" onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Ville ou destination"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
          />

          <DatePicker
            selected={startDate}
            onChange={(date) => {
              setStartDate(date);
              if (endDate && date && endDate <= date) {
                setEndDate(null);
              }
            }}
            placeholderText="Arrivée"
            dateFormat="dd/MM/yyyy"
            locale="fr"
            minDate={today}
            className="datepicker-input"
          />

          <DatePicker
            selected={endDate}
            onChange={(date) => setEndDate(date)}
            placeholderText="Départ"
            dateFormat="dd/MM/yyyy"
            locale="fr"
            minDate={minEndDate}
            className="datepicker-input"
          />

          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="">Tous types</option>
            <option value="APARTMENT">Appartement</option>
            <option value="HOUSE">Maison</option>
            <option value="STUDIO">Studio</option>
          </select>

          <input
            type="number"
            placeholder="Prix max"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
          />

          <button type="submit" className="btn-primary">
            Rechercher
          </button>
        </form>
      </div>

      {loading && <p className="loading-text">Chargement...</p>}
      {error && <p className="error">{error}</p>}

      {serviceDown && (
        <ServiceUnavailable
          message="Le service des logements est temporairement indisponible."
          onRetry={() => fetchProperties({})}
        />
      )}

      {!serviceDown && (
        <div className="property-grid">
          {properties.map((p) => (
            <Link
              to={buildPropertyLink(p.id)}
              key={p.id}
              className="property-card"
            >
              {p.firstPhoto ? (
                <img
                  src={p.firstPhoto}
                  alt={p.title}
                  className="property-card-image"
                />
              ) : (
                <div className="property-card-placeholder">
                  <i className="fas fa-home"></i>
                </div>
              )}
              <div className="property-card-body">
                <span className="property-card-type">
                  {TYPE_LABELS[p.type] || p.type}
                </span>
                <div className="property-card-title">
                  {p.title || p.name || "Logement"}
                </div>
                <div className="property-card-location">
                  {p.location || p.address}
                </div>
                <div className="property-card-price">
                  {p.price ? `${p.price} \u20AC / nuit` : "Prix sur demande"}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}

      {!loading && properties.length === 0 && !error && !serviceDown && (
        <p className="no-results">
          Aucun logement disponible pour ces critères
        </p>
      )}
    </div>
  );
}

export default HomePage;
