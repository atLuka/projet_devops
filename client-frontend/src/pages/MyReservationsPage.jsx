import React, { useState, useEffect, useCallback } from "react";
import { useNavigate, Link } from "react-router-dom";
import { getMyReservations, requestCancellation } from "../api";
import ServiceUnavailable from "../components/ServiceUnavailable";

const STATUS_LABELS = {
  PENDING: "En attente",
  CONFIRMED: "Confirmée",
  CANCELLED: "Annulée",
  CANCELLATION_REQUESTED: "Annulation demandée",
  CANCELLATION_REFUSED: "Annulation refusée",
};

function getDaysUntil(dateStr) {
  const target = new Date(dateStr);
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  target.setHours(0, 0, 0, 0);
  const diff = Math.ceil((target - now) / (1000 * 60 * 60 * 24));
  return diff;
}

function MyReservationsPage({ user }) {
  const navigate = useNavigate();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [serviceDown, setServiceDown] = useState(false);

  const loadReservations = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setServiceDown(false);
    setError("");
    try {
      const tenantId = user.userId || user.email;
      const data = await getMyReservations(tenantId);
      setReservations(Array.isArray(data) ? data : []);
    } catch (err) {
      if (err.status === 503) {
        setServiceDown(true);
      } else {
        setError("Impossible de charger les réservations");
      }
    }
    setLoading(false);
  }, [user]);

  useEffect(() => {
    if (!user) {
      navigate("/login");
      return;
    }
    loadReservations();
  }, [user, navigate, loadReservations]);

  async function handleRequestCancellation(id) {
    if (
      !window.confirm(
        "Voulez-vous demander l'annulation de cette réservation ?",
      )
    )
      return;
    try {
      await requestCancellation(id);
      loadReservations();
    } catch {
      alert("Impossible de demander l'annulation.");
    }
  }

  if (!user) return null;
  if (loading) return <p>Chargement...</p>;
  if (serviceDown)
    return (
      <ServiceUnavailable
        message="Le service des réservations est temporairement indisponible."
        onRetry={loadReservations}
      />
    );
  if (error) return <p className="error">{error}</p>;

  return (
    <div>
      <h1>Mes réservations</h1>

      {reservations.length === 0 ? (
        <p className="no-results">Vous n'avez pas encore de réservation.</p>
      ) : (
        <div className="reservations-list">
          {reservations.map((r) => (
            <div key={r.id} className="reservation-card">
              <h3>Réservation n°{r.id}</h3>
              <p>
                <strong>Logement n°</strong> {r.propertyId}
              </p>
              <p>
                <strong>Du :</strong> {r.startDate}
              </p>
              <p>
                <strong>Au :</strong> {r.endDate}
              </p>
              <p>
                <strong>Statut :</strong>{" "}
                <span
                  className={`status status-${(r.status || "pending").toLowerCase().replace("_", "-")}`}
                >
                  {STATUS_LABELS[r.status] || r.status || "En attente"}
                </span>
              </p>
              {(() => {
                const days = getDaysUntil(r.startDate);
                if (r.status === 'CANCELLED') return null;
                if (days === 0) return <p className="countdown countdown-today"><i className="fas fa-calendar-check"></i> Aujourd'hui !</p>;
                if (days > 0) return <p className="countdown"><i className="fas fa-clock"></i> Dans {days} jour{days > 1 ? 's' : ''}</p>;
                return <p className="countdown countdown-past"><i className="fas fa-check-circle"></i> Terminée</p>;
              })()}
              {(r.status === "PENDING" || r.status === "CONFIRMED") && (
                <button
                  className="btn-cancel-request"
                  onClick={() => handleRequestCancellation(r.id)}
                >
                  Demander l'annulation
                </button>
              )}
              {r.status === "CANCELLATION_REQUESTED" && (
                <p className="cancel-info">
                  Demande d'annulation envoyée au propriétaire.
                </p>
              )}
              {r.status === "CANCELLATION_REFUSED" && (
                <p className="cancel-refused">
                  Le propriétaire a refusé l'annulation.
                </p>
              )}
              {r.status === 'CONFIRMED' && (
                <Link to={`/reservations/${r.id}/messages`} className="btn-messages">
                  <i className="fas fa-comments"></i> Messages
                </Link>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default MyReservationsPage;
