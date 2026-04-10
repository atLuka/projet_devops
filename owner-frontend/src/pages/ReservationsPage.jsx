import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getOwnerProperties, getOwnerReservations, updateReservationStatus, getUnreadCounts } from '../api';
import ServiceUnavailable from '../components/ServiceUnavailable';

function getDaysUntil(dateStr) {
  const target = new Date(dateStr);
  const now = new Date();
  now.setHours(0,0,0,0);
  target.setHours(0,0,0,0);
  return Math.ceil((target - now) / (1000 * 60 * 60 * 24));
}

const STATUS_LABELS = {
  PENDING: 'En attente',
  CONFIRMED: 'Confirmée',
  CANCELLED: 'Annulée',
  CANCELLATION_REQUESTED: 'Annulation demandée',
  CANCELLATION_REFUSED: 'Annulation refusée',
};

export default function ReservationsPage() {
  const navigate = useNavigate();
  const ownerId = localStorage.getItem('userId');
  const [reservations, setReservations] = useState([]);
  const [propertyMap, setPropertyMap] = useState({});
  const [unreadCounts, setUnreadCounts] = useState({});
  const [loading, setLoading] = useState(true);
  const [serviceDown, setServiceDown] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setServiceDown(false);
    try {
      const [props, reservs] = await Promise.all([
        getOwnerProperties(ownerId),
        getOwnerReservations(ownerId),
      ]);
      const map = {};
      (props || []).forEach(p => { map[p.id] = p.title; });
      setPropertyMap(map);
      setReservations(reservs || []);

      // Charger les compteurs de messages non lus pour les réservations confirmées
      const confirmedIds = (reservs || []).filter(r => r.status === 'CONFIRMED').map(r => r.id);
      if (confirmedIds.length > 0) {
        try {
          const counts = await getUnreadCounts('OWNER', confirmedIds);
          setUnreadCounts(counts || {});
        } catch {}
      }
    } catch (err) {
      if (err.status === 503) setServiceDown(true);
    }
    setLoading(false);
  }

  async function handleStatus(id, status) {
    try {
      await updateReservationStatus(id, status);
      loadData();
    } catch {
      alert('Impossible de mettre à jour le statut.');
    }
  }

  if (loading) return <p>Chargement...</p>;
  if (serviceDown) return <ServiceUnavailable message="Le service des réservations est temporairement indisponible." onRetry={loadData} />;

  const actionRequired = reservations.filter(r => r.status === 'PENDING' || r.status === 'CANCELLATION_REQUESTED');

  return (
    <div>
      <div className="page-header">
        <h2>
          Réservations
          {actionRequired.length > 0 && (
            <span className="badge-notif">{actionRequired.length}</span>
          )}
        </h2>
      </div>

      {reservations.length === 0 ? (
        <p>Aucune réservation.</p>
      ) : (
        <table className="reservations-table">
          <thead>
            <tr>
              <th>Logement</th>
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
            {reservations.map(r => {
              const unread = unreadCounts[String(r.id)] || 0;
              return (
                <tr key={r.id} className={unread > 0 ? 'row-unread' : ''}>
                  <td>{propertyMap[r.propertyId] || `#${r.propertyId}`}</td>
                  <td>
                    {r.tenantName || 'Locataire #' + r.tenantId}
                    {unread > 0 && <span className="badge-notif badge-notif-small">{unread}</span>}
                  </td>
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
                  <td>{r.startDate}</td>
                  <td>{r.endDate}</td>
                  <td>
                    {(() => {
                      const days = getDaysUntil(r.startDate);
                      if (r.status === 'CANCELLED') return <span className="text-muted">-</span>;
                      if (days === 0) return <span className="countdown-today"><i className="fas fa-calendar-check"></i> Aujourd'hui</span>;
                      if (days > 0) return <span className="countdown-future"><i className="fas fa-clock"></i> J-{days}</span>;
                      return <span className="text-muted">Passée</span>;
                    })()}
                  </td>
                  <td>
                    <span className={`status-badge status-${(r.status || '').toLowerCase()}`}>
                      {STATUS_LABELS[r.status] || r.status}
                    </span>
                  </td>
                  <td>
                    {r.status === 'PENDING' && (
                      <>
                        <button onClick={() => handleStatus(r.id, 'CONFIRMED')} className="btn btn-small btn-confirm">Confirmer</button>
                        {' '}
                        <button onClick={() => handleStatus(r.id, 'CANCELLED')} className="btn btn-small btn-danger">Refuser</button>
                      </>
                    )}
                    {r.status === 'CANCELLATION_REQUESTED' && (
                      <>
                        <button onClick={() => handleStatus(r.id, 'CANCELLED')} className="btn btn-small btn-confirm">Accepter</button>
                        {' '}
                        <button onClick={() => handleStatus(r.id, 'CANCELLATION_REFUSED')} className="btn btn-small btn-outline">Refuser</button>
                      </>
                    )}
                    {r.status === 'CONFIRMED' && (
                      <button onClick={() => navigate(`/reservations/${r.id}/messages`)} className="btn btn-small btn-messages">
                        <i className="fas fa-comments"></i> Messages
                        {unread > 0 && <span className="badge-notif badge-notif-small">{unread}</span>}
                      </button>
                    )}
                    {(r.status === 'CANCELLED' || r.status === 'CANCELLATION_REFUSED') && <span>—</span>}
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
