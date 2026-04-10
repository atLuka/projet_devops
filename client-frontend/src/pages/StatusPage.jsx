import React, { useState, useEffect, useCallback } from "react";

const SERVICES = [
  {
    name: "API Gateway",
    check: async () => {
      const res = await fetch("/api/properties", {
        signal: AbortSignal.timeout(3000),
      });
      return true;
    },
  },
  {
    name: "Auth Service",
    check: async () => {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: "{}",
        signal: AbortSignal.timeout(3000),
      });
      return res.status !== 503;
    },
  },
  {
    name: "User Service",
    check: async () => {
      const res = await fetch("/api/users", {
        signal: AbortSignal.timeout(3000),
      });
      return res.status !== 503;
    },
  },
  {
    name: "Property Service",
    check: async () => {
      const res = await fetch("/api/properties", {
        signal: AbortSignal.timeout(3000),
      });
      return res.status !== 503;
    },
  },
  {
    name: "Messaging Service",
    check: async () => {
      const res = await fetch("/api/messages/reservation/0", {
        signal: AbortSignal.timeout(3000),
      });
      return res.status !== 503;
    },
  },
  {
    name: "MinIO (Stockage)",
    check: async () => {
      const res = await fetch("http://localhost:9000/minio/health/live", {
        signal: AbortSignal.timeout(3000),
      });
      return res.ok;
    },
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
