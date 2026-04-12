import React, { useState, useEffect, useCallback } from "react";

const checkHealth = async (url) => {
  const res = await fetch(url, { signal: AbortSignal.timeout(3000) });
  return res.ok;
};

const SERVICES = [
  {
    name: "API Gateway",
    check: () => checkHealth("/api/properties/health"),
  },
  {
    name: "Auth Service",
    check: () => checkHealth("/api/auth/health"),
  },
  {
    name: "User Service",
    check: () => checkHealth("/api/users/health"),
  },
  {
    name: "Property Service",
    check: () => checkHealth("/api/properties/health"),
  },
  {
    name: "Messaging Service",
    check: () => checkHealth("/api/messages/health"),
  },
  {
    name: "MinIO (Stockage)",
    check: () => checkHealth("http://localhost:9000/minio/health/live"),
  },
];

export default function StatusPage() {
  const [statuses, setStatuses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [lastCheck, setLastCheck] = useState(null);

  const checkAll = useCallback(async () => {
    setLoading(true);
    const results = await Promise.all(
      SERVICES.map(async (svc) => {
        try {
          const up = await svc.check();
          return { name: svc.name, up };
        } catch {
          return { name: svc.name, up: false };
        }
      }),
    );
    setStatuses(results);
    setLastCheck(new Date().toLocaleTimeString("fr-FR"));
    setLoading(false);
  }, []);

  useEffect(() => {
    checkAll();
    const interval = setInterval(checkAll, 10000);
    return () => clearInterval(interval);
  }, [checkAll]);

  const upCount = statuses.filter((s) => s.up).length;
  const total = statuses.length;

  return (
    <div className="status-page">
      <h1>Statut des services</h1>
      <p className="status-subtitle">
        {total > 0 && (
          <span>
            {upCount}/{total} services opérationnels —{" "}
          </span>
        )}
        Rafraîchissement toutes les 10s
        {lastCheck && <span> — {lastCheck}</span>}
      </p>

      <div className="status-grid">
        {statuses.map((svc) => (
          <div
            key={svc.name}
            className={`status-card ${svc.up ? "status-up" : "status-down"}`}
          >
            <div className="status-indicator">●</div>
            <div className="status-info">
              <div className="status-name">{svc.name}</div>
              <div className="status-label">
                {svc.up ? "Opérationnel" : "Indisponible"}
              </div>
            </div>
          </div>
        ))}
      </div>

      {loading && statuses.length === 0 && <p>Vérification en cours...</p>}

      <button
        className="btn-retry"
        onClick={checkAll}
        style={{ marginTop: "1.5rem" }}
      >
        Vérifier maintenant
      </button>
    </div>
  );
}
