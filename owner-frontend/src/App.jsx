import React, { useState, useEffect, useCallback } from 'react';
import { Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import ReservationsPage from './pages/ReservationsPage';
import AddPropertyPage from './pages/AddPropertyPage';
import EditPropertyPage from './pages/EditPropertyPage';
import PropertyReservationsPage from './pages/PropertyReservationsPage';
import ConversationPage from './pages/ConversationPage';
import { getOwnerReservations, getUnreadCounts } from './api';

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const token = localStorage.getItem('token');
  const ownerId = localStorage.getItem('userId');
  const [notifCount, setNotifCount] = useState(0);

  const refreshNotifs = useCallback(async () => {
    if (!token || !ownerId) return;
    try {
      const reservations = await getOwnerReservations(ownerId);
      const pending = (reservations || []).filter(r => r.status === 'PENDING' || r.status === 'CANCELLATION_REQUESTED').length;

      const confirmedIds = (reservations || []).filter(r => r.status === 'CONFIRMED').map(r => r.id);
      let unreadMessages = 0;
      if (confirmedIds.length > 0) {
        try {
          const counts = await getUnreadCounts('OWNER', confirmedIds);
          unreadMessages = counts?.total || 0;
        } catch {}
      }
      setNotifCount(pending + unreadMessages);
    } catch {}
  }, [token, ownerId]);

  // Rafraîchir à chaque changement de page + polling
  useEffect(() => {
    refreshNotifs();
  }, [location.pathname, refreshNotifs]);

  useEffect(() => {
    if (!token) return;
    const interval = setInterval(refreshNotifs, 30000);
    return () => clearInterval(interval);
  }, [token, refreshNotifs]);

  function handleLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userId');
    window.location.href = '/login';
  }

  return (
    <div className="app">
      <nav className="navbar">
        <div className="nav-brand">
          <Link to="/">Espace Propriétaire</Link>
        </div>
        <div className="nav-links">
          {token ? (
            <>
              <Link to="/">Tableau de bord</Link>
              <Link to="/reservations" className="nav-link-with-badge">
                Réservations
                {notifCount > 0 && <span className="badge-notif">{notifCount}</span>}
              </Link>
              <Link to="/properties/new">Ajouter un logement</Link>
              <button onClick={handleLogout} className="btn btn-outline">Déconnexion</button>
            </>
          ) : (
            <>
              <Link to="/login">Connexion</Link>
              <Link to="/register">Inscription</Link>
            </>
          )}
        </div>
      </nav>
      <main className="container">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/reservations" element={<ReservationsPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/properties/new" element={<AddPropertyPage />} />
          <Route path="/properties/:id/edit" element={<EditPropertyPage />} />
          <Route path="/properties/:id/reservations" element={<PropertyReservationsPage />} />
          <Route path="/reservations/:reservationId/messages" element={<ConversationPage />} />
        </Routes>
      </main>
    </div>
  );
}
