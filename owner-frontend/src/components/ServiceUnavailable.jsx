import React from "react";

export default function ServiceUnavailable({ message, onRetry }) {
  return (
    <div className="service-unavailable">
      <div className="service-unavailable-icon">
        <i className="fas fa-exclamation-triangle"></i>
      </div>
      <h2>Service temporairement indisponible</h2>
      <p>
        {message ||
          "Le service est momentanément inaccessible. Veuillez réessayer dans quelques instants."}
      </p>
      {onRetry && (
        <button className="btn-retry" onClick={onRetry}>
          Réessayer
        </button>
      )}
    </div>
  );
}
