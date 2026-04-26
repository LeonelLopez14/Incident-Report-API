/**
 * api.js — Cliente HTTP centralizado.
 *
 * Todas las llamadas a la API pasan por aquí.
 * Adjunta el token JWT automáticamente y maneja errores globales.
 */

const API_BASE = 'http://localhost:8080';

const Api = (() => {

    // ── Token ─────────────────────────────────────────────────

    const getToken = ()    => localStorage.getItem('accessToken');
    const setToken = (t)   => localStorage.setItem('accessToken', t);
    const clearToken = ()  => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
    };

    // ── Request base ──────────────────────────────────────────

    const request = async (method, path, body = null) => {
        const headers = { 'Content-Type': 'application/json' };
        const token   = getToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const options = { method, headers };
        if (body) options.body = JSON.stringify(body);

        const response = await fetch(`${API_BASE}${path}`, options);

        // Token expirado → redirigir al login
        if (response.status === 401) {
            clearToken();
            window.dispatchEvent(new CustomEvent('auth:expired'));
            throw new Error('Session expired. Please log in again.');
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            const message = data?.message || data?.error || `Error ${response.status}`;
            throw new Error(message);
        }

        return data;
    };

    // ── Métodos HTTP ──────────────────────────────────────────

    const get    = (path)         => request('GET',    path);
    const post   = (path, body)   => request('POST',   path, body);
    const put    = (path, body)   => request('PUT',    path, body);
    const patch  = (path, body)   => request('PATCH',  path, body);
    const del    = (path)         => request('DELETE', path);

    // ── Auth endpoints ────────────────────────────────────────

    const login  = (usernameOrEmail, password) =>
        post('/api/auth/login', { usernameOrEmail, password });

    const register = (data) => post('/api/auth/register', data);

    // ── Incident endpoints ────────────────────────────────────

    const getIncidents = (params = {}) => {
        const q = new URLSearchParams();
        Object.entries(params).forEach(([k, v]) => { if (v !== '' && v != null) q.set(k, v); });
        return get(`/api/incidents?${q}`);
    };

    const getIncident    = (id)         => get(`/api/incidents/${id}`);
    const createIncident = (data)       => post('/api/incidents', data);
    const updateIncident = (id, data)   => put(`/api/incidents/${id}`, data);
    const updateStatus   = (id, data)   => patch(`/api/incidents/${id}/status`, data);
    const deleteIncident = (id)         => del(`/api/incidents/${id}`);

    // ── User endpoints ────────────────────────────────────────

    const getMyProfile = ()             => get('/api/users/me');
    const getUsers     = (params = {})  => {
        const q = new URLSearchParams(params);
        return get(`/api/users?${q}`);
    };
    const disableUser  = (id)           => patch(`/api/users/${id}/disable`, {});
    const deleteUser   = (id)           => del(`/api/users/${id}`);

    // ── Report endpoints ──────────────────────────────────────

    const getStats   = ()               => get('/api/reports/stats');
    const getCritical = (page = 0)      => get(`/api/reports/critical?page=${page}`);
    const getRecent   = (days = 30)     => get(`/api/reports/recent?days=${days}`);

    return {
        getToken, setToken, clearToken,
        login, register,
        getIncidents, getIncident, createIncident, updateIncident, updateStatus, deleteIncident,
        getMyProfile, getUsers, disableUser, deleteUser,
        getStats, getCritical, getRecent
    };
})();