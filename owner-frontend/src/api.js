const API_BASE = "/api";

function authHeaders() {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(path, options = {}) {
  let res;
  try {
    res = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...authHeaders(),
        ...options.headers,
      },
    });
  } catch (e) {
    const err = new Error("Service indisponible");
    err.status = 503;
    throw err;
  }
  if (!res.ok) {
    const text = await res.text();
    const err = new Error(text || `Request failed: ${res.status}`);
    err.status = res.status;
    throw err;
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export async function login(email, password) {
  return request("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export async function register(data) {
  return request("/auth/register", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function getOwnerProperties(ownerId) {
  return request(`/properties/owner/${ownerId}`);
}

export async function createProperty(data) {
  return request("/properties", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updateProperty(id, data) {
  return request(`/properties/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deleteProperty(id) {
  return request(`/properties/${id}`, {
    method: "DELETE",
  });
}

export async function getUploadUrl(propertyId) {
  return request(`/properties/${propertyId}/photos/upload-url`, {
    method: "POST",
  });
}

export async function getPropertyPhotos(propertyId) {
  return request(`/properties/${propertyId}/photos`);
}

export async function deletePhoto(propertyId, objectKey) {
  return request(`/properties/${propertyId}/photos/${objectKey}`, {
    method: "DELETE",
  });
}

export async function uploadFileToPresignedUrl(uploadUrl, file) {
  let res;
  try {
    res = await fetch(uploadUrl, {
      method: "PUT",
      body: file,
      headers: { "Content-Type": file.type },
    });
  } catch (e) {
    const err = new Error("Service de stockage indisponible");
    err.status = 503;
    throw err;
  }
  if (!res.ok) {
    const err = new Error("Upload failed");
    err.status = res.status;
    throw err;
  }
}

export async function getPropertyReservations(propertyId) {
  return request(`/reservations/property/${propertyId}`);
}

export async function getOwnerReservations(ownerId) {
  return request(`/reservations/owner/${ownerId}`);
}

export async function updateReservationStatus(id, status) {
  return request(`/reservations/${id}/status`, {
    method: "PUT",
    body: JSON.stringify({ status }),
  });
}

export async function getMessages(reservationId) {
  return request(`/messages/reservation/${reservationId}`);
}

export async function sendMessage(data) {
  return request('/messages', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function markMessagesRead(reservationId, role) {
  return request(`/messages/reservation/${reservationId}/read?role=${role}`, {
    method: 'PUT',
  });
}

export async function getUnreadCounts(role, reservationIds) {
  const ids = reservationIds.join(',');
  return request(`/messages/unread?role=${role}&reservationIds=${ids}`);
}
