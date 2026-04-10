import React, { useState } from "react";
import { Link } from "react-router-dom";
import { login } from "../api";
import ServiceUnavailable from "../components/ServiceUnavailable";

function LoginPage({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [serviceDown, setServiceDown] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setServiceDown(false);
    setLoading(true);
    try {
      const data = await login(email, password);
      onLogin(data);
    } catch (err) {
      if (err.status === 503) {
        setServiceDown(true);
      } else if (err.status === 401) {
        setError("Email ou mot de passe incorrect");
      } else {
        setError("Erreur de connexion");
      }
    }
    setLoading(false);
  };

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
      <h1>Connexion</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        {error && <p className="error">{error}</p>}

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

        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? "Connexion en cours..." : "Se connecter"}
        </button>
      </form>
      <p className="form-footer">
        Pas encore de compte ? <Link to="/register">Créer un compte</Link>
      </p>
    </div>
  );
}

export default LoginPage;
