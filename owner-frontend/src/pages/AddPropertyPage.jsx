import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createProperty, getUploadUrl, uploadFileToPresignedUrl } from "../api";
import ServiceUnavailable from "../components/ServiceUnavailable";

export default function AddPropertyPage() {
  const navigate = useNavigate();
  const ownerId = localStorage.getItem("userId");
  const [form, setForm] = useState({
    title: "",
    type: "APARTMENT",
    location: "",
    price: "",
    description: "",
  });
  const [error, setError] = useState("");
  const [serviceDown, setServiceDown] = useState(false);
  const [loading, setLoading] = useState(false);
  const [createdId, setCreatedId] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadMsg, setUploadMsg] = useState("");

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setServiceDown(false);
    setLoading(true);
    try {
      const property = await createProperty({
        ...form,
        price: parseFloat(form.price),
        ownerId,
      });
      setCreatedId(property.id);
    } catch (err) {
      if (err.status === 503) {
        setServiceDown(true);
      } else {
        setError("Impossible de créer le logement.");
      }
    } finally {
      setLoading(false);
    }
  }

  async function handlePhotoUpload(e) {
    const files = e.target.files;
    if (!files.length) return;
    setUploading(true);
    setUploadMsg("");
    try {
      for (const file of files) {
        const { uploadUrl } = await getUploadUrl(createdId);
        await uploadFileToPresignedUrl(uploadUrl, file);
      }
      setUploadMsg(`${files.length} photo(s) ajoutée(s)`);
    } catch (err) {
      if (err.status === 503) {
        setUploadMsg("Service de stockage indisponible.");
      } else {
        setUploadMsg("Échec de l'envoi des photos.");
      }
    } finally {
      setUploading(false);
    }
  }

  if (serviceDown) {
    return (
      <ServiceUnavailable
        message="Le service des logements est temporairement indisponible."
        onRetry={() => setServiceDown(false)}
      />
    );
  }

  if (createdId) {
    return (
      <div className="form-page">
        <h2>Logement créé !</h2>
        <div className="form-card">
          <p>Votre logement a été créé avec succès.</p>
          <label>Ajouter des photos</label>
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={handlePhotoUpload}
          />
          {uploading && <p>Envoi en cours...</p>}
          {uploadMsg && <p>{uploadMsg}</p>}
          <div style={{ marginTop: "1rem" }}>
            <button onClick={() => navigate("/")} className="btn btn-primary">
              Retour au tableau de bord
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="form-page">
      <h2>Nouveau logement</h2>
      <form onSubmit={handleSubmit} className="form-card">
        {error && <div className="error-msg">{error}</div>}
        <label>Titre</label>
        <input
          name="title"
          value={form.title}
          onChange={handleChange}
          required
        />
        <label>Type</label>
        <select name="type" value={form.type} onChange={handleChange}>
          <option value="APARTMENT">Appartement</option>
          <option value="HOUSE">Maison</option>
          <option value="STUDIO">Studio</option>
        </select>
        <label>Localisation</label>
        <input
          name="location"
          value={form.location}
          onChange={handleChange}
          required
        />
        <label>Prix (€ / nuit)</label>
        <input
          name="price"
          type="number"
          min="1"
          step="0.01"
          value={form.price}
          onChange={handleChange}
          required
        />
        <label>Description</label>
        <textarea
          name="description"
          value={form.description}
          onChange={handleChange}
          rows={4}
        />
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? "Création en cours..." : "Créer le logement"}
        </button>
      </form>
    </div>
  );
}
