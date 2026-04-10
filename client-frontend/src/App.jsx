import React, { useState, useEffect } from "react";
import { Routes, Route, Link, useNavigate } from "react-router-dom";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import PropertyDetailPage from "./pages/PropertyDetailPage";
import MyReservationsPage from "./pages/MyReservationsPage";
import StatusPage from "./pages/StatusPage";
import ConversationPage from "./pages/ConversationPage";

function App() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");
    const email = localStorage.getItem("userEmail");
    const role = localStorage.getItem("userRole");
    const userId = localStorage.getItem("userId");
    if (token && email) {
      setUser({ token, email, role, userId });
    }
  }, []);

  const handleLogin = (data) => {
    localStorage.setItem("token", data.token);
    localStorage.setItem("userEmail", data.email);
    localStorage.setItem("userRole", data.role);
    localStorage.setItem("userId", data.userId);
    setUser(data);
    navigate("/");
  };

  const handleLogout = () => {
    localStorage.clear();
    setUser(null);
    navigate("/");
  };

  return (
    <div className="app">
      <nav className="navbar">
        <Link to="/" className="nav-brand">
          RentApp
        </Link>
        <div className="nav-links">
          <Link to="/" className="nav-link">
            Accueil
          </Link>
          <Link to="/status" className="nav-link">
            Statut
          </Link>
          {user ? (
            <>
              {user.role === "TENANT" && (
                <Link to="/my-reservations" className="nav-link">
                  Mes réservations
                </Link>
              )}
              {user.role === "OWNER" && (
                <a href="/owner/" className="nav-link">
                  Espace propriétaire
                </a>
              )}
              <button onClick={handleLogout} className="btn-logout">
                Déconnexion
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-link">
                Connexion
              </Link>
              <Link to="/register" className="nav-link">
                Inscription
              </Link>
            </>
          )}
        </div>
      </nav>

      <main className="container">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/properties/:id"
            element={<PropertyDetailPage user={user} />}
          />
          <Route
            path="/my-reservations"
            element={<MyReservationsPage user={user} />}
          />
          <Route path="/status" element={<StatusPage />} />
          <Route path="/reservations/:reservationId/messages" element={<ConversationPage user={user} />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
