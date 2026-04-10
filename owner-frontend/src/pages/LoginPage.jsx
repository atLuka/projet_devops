import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { login } from "../api";
import ServiceUnavailable from "../components/ServiceUnavailable";

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [serviceDown, setServiceDown] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setServiceDown(false);
    setLoading(true);
    try {
      const data = await login(email, password);
      localStorage.setItem("token", data.token);
      localStorage.setItem("userEmail", data.email);
      localStorage.setItem("userRole", data.role);
      localStorage.setItem("userId", data.userId || data.email);
      navigate("/");
      window.location.reload();
    } catch (err) {
      if (err.status === 503) {
        setServiceDown(true);
      } else {
        setError("Échec de la connexion. Vérifiez vos identifiants.");
      }
    } finally {
      setLoading(false);
    }
  }

  if (serviceDown) {
    return (
      <ServiceUnavailable
        message="Le service d'authentification est temporairement indisponible."
        onRetry={() => setServiceDown(false)}
      />
    );
  }

  return (
    <div className="form-page">
      <h2>Connexion Propriétaire</h2>
      <form onSubmit={handleSubmit} className="form-card">
        {error && <div className="error-msg">{error}</div>}
        <label>Email</label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <label>Mot de passe</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? "Connexion en cours..." : "Se connecter"}
        </button>
        <p className="form-footer">
          Pas encore de compte ? <Link to="/register">Créer un compte</Link>
        </p>
      </form>
    </div>
  );
}
