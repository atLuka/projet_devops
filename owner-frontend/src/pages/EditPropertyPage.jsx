import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getOwnerProperties,
  updateProperty,
  getPropertyPhotos,
  getUploadUrl,
  uploadFileToPresignedUrl,
  deletePhoto,
} from "../api";
import ServiceUnavailable from "../components/ServiceUnavailable";

export default function EditPropertyPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const ownerId = localStorage.getItem("userId");
  const [form, setForm] = useState(null);
  const [photos, setPhotos] = useState([]);
  const [error, setError] = useState("");
  const [serviceUnavailable, setServiceUnavailable] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadMsg, setUploadMsg] = useState("");

  useEffect(() => {
    loadProperty();
    loadPhotos();
  }, [id]);

  async function loadProperty() {
    setServiceUnavailable(false);
    setError("");
    try {
      const props = await getOwnerProperties(ownerId);
      const property = (props || []).find((p) => String(p.id) === String(id));
      if (property) {
        setForm({
          title: property.title || "",
          type: property.type || "APARTMENT",
          location: property.location || "",
          price: property.price || "",
          description: property.description || "",
        });
      } else {
        setError("Logement introuvable.");
      }
    } catch (err) {
      if (err.status === 503) {
        setServiceUnavailable(true);
      } else {
        setError("Impossible de charger le logement.");
      }
    } finally {
      setLoading(false);
    }
  }

  async function loadPhotos() {
    try {
      const data = await getPropertyPhotos(id);
      setPhotos(data || []);
    } catch {}
  }

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSaving(true);
    try {
      await updateProperty(id, {
        ...form,
        price: parseFloat(form.price),
        ownerId,
      });
      navigate("/");
    } catch {
      setError("Impossible de mettre à jour le logement.");
    } finally {
      setSaving(false);
    }
  }

  async function handlePhotoUpload(e) {
    const files = e.target.files;
    if (!files.length) return;
    setUploading(true);
    setUploadMsg("");
    try {
      for (const file of files) {
        const { uploadUrl } = await getUploadUrl(id);
        await uploadFileToPresignedUrl(uploadUrl, file);
      }
      setUploadMsg(`${files.length} photo(s) ajoutée(s)`);
      loadPhotos();
    } catch (err) {
      if (err.status === 503) {
        setUploadMsg("Service de stockage indisponible, réessayez plus tard.");
      } else {
        setUploadMsg("Échec de l'envoi des photos.");
      }
    } finally {
      setUploading(false);
    }
  }

  if (loading) return <p>Chargement...</p>;
  if (serviceUnavailable)
    return (
      <ServiceUnavailable
        message="Le service des logements est temporairement indisponible."
        onRetry={loadProperty}
      />
    );
  if (!form)
    return <p className="error-msg">{error || "Logement introuvable."}</p>;

  return (
    <div className="form-page">
      <h2>Modifier le logement</h2>
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
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? "Enregistrement..." : "Enregistrer"}
        </button>
      </form>

      <div className="form-card" style={{ marginTop: "1.5rem" }}>
        <h3>Photos ({photos.length})</h3>
        {photos.length > 0 && (
          <div className="photo-grid">
            {photos.map((photo, i) => (
              <div key={i} className="photo-item">
                <img
                  src={photo.downloadUrl}
                  alt={`Photo ${i + 1}`}
                  className="photo-thumb"
                />
                <button
                  className="btn btn-danger btn-sm"
                  onClick={async () => {
                    await deletePhoto(id, photo.objectKey);
                    loadPhotos();
                  }}
                >
                  Supprimer
                </button>
              </div>
            ))}
          </div>
        )}
        <label>Ajouter des photos</label>
        <input
          type="file"
          accept="image/*"
          multiple
          onChange={handlePhotoUpload}
        />
        {uploading && <p>Envoi en cours...</p>}
        {uploadMsg && <p>{uploadMsg}</p>}
      </div>
    </div>
  );
}
