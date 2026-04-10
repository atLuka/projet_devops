import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getOwnerProperties, deleteProperty, getPropertyPhotos } from '../api';
import ServiceUnavailable from '../components/ServiceUnavailable';

export default function DashboardPage() {
  const navigate = useNavigate();
  const [properties, setProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [serviceUnavailable, setServiceUnavailable] = useState(false);
  const token = localStorage.getItem('token');
  const ownerId = localStorage.getItem('userId');

  useEffect(() => {
    if (!token) return;
    loadProperties();
  }, [token]);

  async function loadProperties() {
    setLoading(true);
    setServiceUnavailable(false);
    setError('');
    try {
      const props = await getOwnerProperties(ownerId);
      const propsWithPhotos = await Promise.all(
        (props || []).map(async (p) => {
          try {
            const photos = await getPropertyPhotos(p.id);
            return { ...p, photoCount: photos ? photos.length : 0, firstPhoto: photos?.[0]?.downloadUrl || null };
          } catch {
            return { ...p, photoCount: 0, firstPhoto: null };
          }
        })
      );
      setProperties(propsWithPhotos);
    } catch (err) {
      if (err.status === 503) setServiceUnavailable(true);
      else setError('Impossible de charger les logements.');
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Êtes-vous sûr de vouloir supprimer ce logement ?')) return;
    try {
      await deleteProperty(id);
      setProperties(properties.filter(p => p.id !== id));
    } catch {
      alert('Impossible de supprimer le logement.');
    }
  }

  if (!token) {
    return (
      <div className="form-page">
        <h2>Bienvenue sur l'Espace Propriétaire</h2>
        <p>Veuillez vous <Link to="/login">connecter</Link> pour gérer vos logements.</p>
      </div>
    );
  }

  if (loading) return <p>Chargement...</p>;
  if (serviceUnavailable) return <ServiceUnavailable message="Le service des logements est temporairement indisponible." onRetry={loadProperties} />;
  if (error) return <p className="error-msg">{error}</p>;

  const typeLabels = { APARTMENT: 'Appartement', HOUSE: 'Maison', STUDIO: 'Studio' };

  return (
    <div>
      <div className="page-header">
        <h2>Mes logements</h2>
        <Link to="/properties/new" className="btn btn-primary">+ Ajouter un logement</Link>
      </div>
      {properties.length === 0 ? (
        <p>Aucun logement. Ajoutez votre premier logement !</p>
      ) : (
        <div className="card-grid">
          {properties.map(p => (
            <div key={p.id} className="card">
              {p.firstPhoto ? (
                <img src={p.firstPhoto} alt={p.title} className="card-image" />
              ) : (
                <div className="card-placeholder"><i className="fas fa-home"></i></div>
              )}
              <div className="card-body">
                <span className="card-type">{typeLabels[p.type] || p.type}</span>
                <div className="card-title">{p.title}</div>
                <div className="card-location">{p.location}</div>
                <div className="card-price">{p.price} € / nuit</div>
                <div className="card-photos">{p.photoCount} photo(s)</div>
                <div className="card-actions">
                  <button onClick={() => navigate(`/properties/${p.id}/edit`)} className="btn btn-small btn-primary">Modifier</button>
                  <button onClick={() => navigate(`/properties/${p.id}/reservations`)} className="btn btn-small btn-outline">Réservations</button>
                  <button onClick={() => handleDelete(p.id)} className="btn btn-small btn-danger">Supprimer</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
