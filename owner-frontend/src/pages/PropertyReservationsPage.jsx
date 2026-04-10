import React, { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { getPropertyReservations, updateReservationStatus } from "../api";
import ServiceUnavailable from "../components/ServiceUnavailable";

function getDaysUntil(dateStr) {
  const target = new Date(dateStr);
  const now = new Date();
  now.setHours(0,0,0,0);
  target.setHours(0,0,0,0);
  return Math.ceil((target - now) / (1000 * 60 * 60 * 24));
}

export default function PropertyReservationsPage() {
  const { id } = useParams();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [serviceUnavailable, setServiceUnavailable] = useState(false);

  useEffect(() => {
    loadReservations();
  }, [id]);

  async function loadReservations() {
    setLoading(true);
    setServiceUnavailable(false);
    setError("");
    try {
      const data = await getPropertyReservations(id);
      setReservations(data || []);
    } catch (err) {
      if (err.status === 503) {
        setServiceUnavailable(true);
      } else {
        setError("Impossible de charger les réservations.");
      }
    } finally {
      setLoading(false);
    }
  }

  async function handleStatus(reservationId, status) {
    try {
      await updateReservationStatus(reservationId, status);
      loadReservations();
    } catch {
      alert("Impossible de mettre à jour le statut de la réservation.");
    }
  }

  if (loading) return <p>Chargement...</p>;
  if (serviceUnavailable)
    return (
      <ServiceUnavailable
        message="Le service des réservations est temporairement indisponible."
        onRetry={loadReservations}
      />
    );
  if (error) return <p className="error-msg">{error}</p>;

  return (
    <div>
      <div className="page-header">
        <h2>Réservations pour le logement #{id}</h2>
        <Link to="/" className="btn btn-outline">
          Retour
        </Link>
      </div>
      {reservations.length === 0 ? (
        <p>Aucune réservation pour ce logement.</p>
      ) : (
        <table className="reservations-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Locataire</th>
              <th>Contact</th>
              <th>Arrivée</th>
              <th>Départ</th>
              <th>Délai</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {reservations.map((r) => {
              const statusLabels = {
                PENDING: "En attente",
                CONFIRMED: "Confirmée",
                CANCELLED: "Annulée",
                CANCELLATION_REQUESTED: "Annulation demandée",
                CANCELLATION_REFUSED: "Annulation refusée",
              };
              return (
                <tr key={r.id}>
                  <td>{r.id}</td>
                  <td>{r.tenantName || 'Locataire #' + r.tenantId}</td>
                  <td>
                    {r.tenantEmail && (
                      <a href={`mailto:${r.tenantEmail}`} className="contact-link" title={r.tenantEmail}>
                        <i className="fas fa-envelope"></i>
                      </a>
                    )}
                    {r.tenantPhone && (
                      <a href={`tel:${r.tenantPhone}`} className="contact-link" title={r.tenantPhone}>
                        <i className="fas fa-phone"></i>
                      </a>
                    )}
                    {!r.tenantEmail && !r.tenantPhone && <span>-</span>}
                  </td>
                  <td>{r.startDate || r.checkIn || "-"}</td>
                  <td>{r.endDate || r.checkOut || "-"}</td>
                  <td>
                    {(() => {
                      const days = getDaysUntil(r.startDate || r.checkIn);
                      if (r.status === 'CANCELLED') return <span className="text-muted">-</span>;
                      if (days === 0) return <span className="countdown-today"><i className="fas fa-calendar-check"></i> Aujourd'hui</span>;
                      if (days > 0) return <span className="countdown-future"><i className="fas fa-clock"></i> J-{days}</span>;
                      return <span className="text-muted">Passée</span>;
                    })()}
                  </td>
                  <td>
                    <span
                      className={`status-badge status-${(r.status || "").toLowerCase().replace("_", "-")}`}
                    >
                      {statusLabels[r.status] || r.status}
                    </span>
                  </td>
                  <td>
                    {r.status === "PENDING" && (
                      <>
                        <button
                          onClick={() => handleStatus(r.id, "CONFIRMED")}
                          className="btn btn-small btn-confirm"
                        >
                          Confirmer
                        </button>
                        <button
                          onClick={() => handleStatus(r.id, "CANCELLED")}
                          className="btn btn-small btn-danger"
                        >
                          Refuser
                        </button>
                      </>
                    )}
                    {r.status === "CANCELLATION_REQUESTED" && (
                      <>
                        <button
                          onClick={() => handleStatus(r.id, "CANCELLED")}
                          className="btn btn-small btn-confirm"
                        >
                          Accepter l'annulation
                        </button>{" "}
                        <button
                          onClick={() =>
                            handleStatus(r.id, "CANCELLATION_REFUSED")
                          }
                          className="btn btn-small btn-outline"
                        >
                          Refuser l'annulation
                        </button>
                      </>
                    )}
                    {(r.status === "CONFIRMED" ||
                      r.status === "CANCELLED" ||
                      r.status === "CANCELLATION_REFUSED") && <span>—</span>}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
}
