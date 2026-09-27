// ── Token Management ──────────────────────────────────────────────────────────
const TOKEN_KEY = 'sms_token';

export const getToken  = () => localStorage.getItem(TOKEN_KEY);
export const setToken  = (t) => localStorage.setItem(TOKEN_KEY, t);
export const removeToken = () => localStorage.removeItem(TOKEN_KEY);

// ── JWT Decode (payload only, no signature check — server validates) ───────────
export function decodeJwt(token) {
  try {
    const b64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(b64));
  } catch { return null; }
}

export function getCurrentUser() {
  const token = getToken();
  if (!token) return null;
  const payload = decodeJwt(token);
  if (!payload) return null;
  return { email: payload.sub, role: payload.role };
}

// ── Auth Guard ────────────────────────────────────────────────────────────────
export function requireAuth() {
  if (!getToken()) { window.location.href = '/index.html'; return false; }
  return true;
}

export function requireGuest() {
  if (getToken()) { window.location.href = '/dashboard.html'; }
}

// ── API Base Fetch ────────────────────────────────────────────────────────────
const BASE = '';   // same origin — served by Spring Boot

export async function apiCall(method, endpoint, body = null) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const options = { method, headers };
  if (body) options.body = JSON.stringify(body);

  const res = await fetch(BASE + endpoint, options);

  // Unauthorised → redirect to login
  if (res.status === 401) {
    removeToken();
    window.location.href = '/index.html';
    throw new Error('Unauthorized');
  }

  // No-content responses
  if (res.status === 204) return null;

  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = text; }

  if (!res.ok) {
    // Structured validation error from GlobalExceptionHandler
    const msg = data?.message || data?.errors
      ? (data.message || Object.values(data.errors).join(', '))
      : (typeof data === 'string' ? data : `HTTP ${res.status}`);
    throw new Error(msg);
  }
  return data;
}

// Shorthand helpers
export const api = {
  get:    (url)       => apiCall('GET',    url),
  post:   (url, body) => apiCall('POST',   url, body),
  put:    (url, body) => apiCall('PUT',    url, body),
  patch:  (url, body) => apiCall('PATCH',  url, body),
  delete: (url)       => apiCall('DELETE', url),
};

// ── Toast Notifications ───────────────────────────────────────────────────────
let toastContainer;
function getToastContainer() {
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.className = 'toast-container';
    document.body.appendChild(toastContainer);
  }
  return toastContainer;
}

const TOAST_ICONS = { success: '✅', error: '❌', info: 'ℹ️' };

export function showToast(message, type = 'info', duration = 3500) {
  const container = getToastContainer();
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span class="toast-icon">${TOAST_ICONS[type]}</span><span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.classList.add('removing');
    toast.addEventListener('animationend', () => toast.remove(), { once: true });
  }, duration);
}

// ── Modal Helpers ─────────────────────────────────────────────────────────────
export function openModal(overlayId) {
  document.getElementById(overlayId)?.classList.add('open');
}
export function closeModal(overlayId) {
  document.getElementById(overlayId)?.classList.remove('open');
}

// ── Button Loading State ──────────────────────────────────────────────────────
export function setLoading(btn, loading) {
  btn.disabled = loading;
  btn.classList.toggle('loading', loading);
}

// ── Form Helpers ──────────────────────────────────────────────────────────────
export function getFormData(formId) {
  const form = document.getElementById(formId);
  const data = {};
  new FormData(form).forEach((v, k) => { data[k] = v.trim(); });
  return data;
}

export function clearForm(formId) {
  document.getElementById(formId)?.reset();
}
