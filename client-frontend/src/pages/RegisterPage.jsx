import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api";
import ServiceUnavailable from "../components/ServiceUnavailable";

function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    phone: "",
    role: "TENANT",
  });
  const [error, setError] = useState("");
  const [serviceDown, setServiceDown] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setServiceDown(false);
    setLoading(true);
    try {
      await register(form);
      navigate("/login");
    } catch (err) {
      if (err.status === 503) {
        setServiceDown(true);
      } else {
        setError("L'inscription a échoué");
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
      <h1>Inscription</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        {error && <p className="error">{error}</p>}

        <label>Prénom</label>
        <input
          type="text"
          name="firstName"
          value={form.firstName}
          onChange={handleChange}
          required
        />

        <label>Nom</label>
        <input
          type="text"
          name="lastName"
          value={form.lastName}
          onChange={handleChange}
          required
        />

        <label>Email</label>
        <input
          type="email"
          name="email"
          value={form.email}
          onChange={handleChange}
          required
        />

        <label>Téléphone</label>
        <input
          type="tel"
          name="phone"
          value={form.phone}
          onChange={handleChange}
        />

        <label>Mot de passe</label>
        <input
          type="password"
          name="password"
          value={form.password}
          onChange={handleChange}
          required
          minLength={6}
        />

        <label>Vous êtes</label>
        <select name="role" value={form.role} onChange={handleChange} required>
          <option value="TENANT">Locataire</option>
          <option value="OWNER">Propriétaire</option>
        </select>

        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? "Inscription en cours..." : "S'inscrire"}
        </button>
      </form>
      <p className="form-footer">
        Déjà un compte ? <Link to="/login">Se connecter</Link>
      </p>
    </div>
  );
}

export default RegisterPage;
