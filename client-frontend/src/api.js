export const API_BASE = "/api";

function getAuthHeaders() {
  const token = localStorage.getItem("token");
  const headers = { "Content-Type": "application/json" };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return headers;
}

async function request(url, options = {}) {
  let res;
  try {
    res = await fetch(url, {
      ...options,
      headers: { ...getAuthHeaders(), ...options.headers },
    });
  } catch (e) {
    const err = new Error("Service indisponible");
    err.status = 503;
    throw err;
  }
  if (!res.ok) {
    let message = `Erreur ${res.status}`;
    try {
      const body = await res.json();
      if (body.message) message = body.message;
    } catch {}
    const err = new Error(message);
    err.status = res.status;
    throw err;
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export async function login(email, password) {
  return request(`${API_BASE}/auth/login`, {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export async function register(data) {
  return request(`${API_BASE}/auth/register`, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function getProperties(filters = {}) {
  const params = new URLSearchParams();
  if (filters.type) params.append("type", filters.type);
  if (filters.location) params.append("location", filters.location);
  if (filters.maxPrice) params.append("maxPrice", filters.maxPrice);
  if (filters.startDate) params.append("startDate", filters.startDate);
  if (filters.endDate) params.append("endDate", filters.endDate);
  const query = params.toString();
  return request(`${API_BASE}/properties${query ? "?" + query : ""}`);
}

export async function getProperty(id) {
  return request(`${API_BASE}/properties/${id}`);
}

export async function getPropertyPhotos(id) {
  return request(`${API_BASE}/properties/${id}/photos`);
}

export async function createReservation(data) {
  return request(`${API_BASE}/reservations`, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function getMyReservations(tenantId) {
  return request(`${API_BASE}/reservations/tenant/${tenantId}`);
}

export async function requestCancellation(reservationId) {
  return request(`${API_BASE}/reservations/${reservationId}/status`, {
    method: "PUT",
    body: JSON.stringify({ status: "CANCELLATION_REQUESTED" }),
  });
}

export async function getReservedDates(propertyId) {
  return request(`${API_BASE}/properties/${propertyId}/reserved-dates`);
}

export async function getMessages(reservationId) {
  return request(`${API_BASE}/messages/reservation/${reservationId}`);
}

export async function sendMessage(data) {
  return request(`${API_BASE}/messages`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function markMessagesRead(reservationId, role) {
  return request(`${API_BASE}/messages/reservation/${reservationId}/read?role=${role}`, {
    method: 'PUT',
  });
}
