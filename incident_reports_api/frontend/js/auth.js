/**
 * auth.js — Lógica de autenticación del frontend.
 *
 * Maneja login, logout y persistencia de sesión en localStorage.
 */

const Auth = (() => {

    // ── Sesión ────────────────────────────────────────────────

    const saveSession = (jwtResponse) => {
        localStorage.setItem('accessToken',  jwtResponse.accessToken);
        localStorage.setItem('refreshToken', jwtResponse.refreshToken);
        localStorage.setItem('user', JSON.stringify({
            id:       jwtResponse.userId,
            username: jwtResponse.username,
            email:    jwtResponse.email,
            roles:    jwtResponse.roles
        }));
    };

    const getUser = () => {
        const raw = localStorage.getItem('user');
        return raw ? JSON.parse(raw) : null;
    };

    const isLoggedIn = () => !!Api.getToken();

    const hasRole = (role) => {
        const user = getUser();
        return user?.roles?.includes(role) ?? false;
    };

    const isAdmin    = () => hasRole('ROLE_ADMIN');
    const isAnalyst  = () => hasRole('ROLE_ANALYST') || hasRole('ROLE_ADMIN');

    const logout = () => {
        Api.clearToken();
        showLogin();
    };

    // ── Login flow ────────────────────────────────────────────

    const init = () => {
        // Si ya hay sesión activa, ir directo al app
        if (isLoggedIn()) {
            showApp();
            return;
        }
        showLogin();
    };

    const handleLoginSubmit = async (e) => {
        e.preventDefault();

        const usernameOrEmail = document.getElementById('loginUser').value.trim();
        const password        = document.getElementById('loginPass').value;
        const errorEl         = document.getElementById('loginError');
        const btnText         = document.querySelector('#loginBtn .btn-text');
        const btnLoader       = document.querySelector('#loginBtn .btn-loader');

        if (!usernameOrEmail || !password) {
            showLoginError('Completá todos los campos');
            return;
        }

        // Loading state
        btnText.classList.add('hidden');
        btnLoader.classList.remove('hidden');
        errorEl.classList.add('hidden');

        try {
            const response = await Api.login(usernameOrEmail, password);
            saveSession(response);
            showApp();
        } catch (err) {
            showLoginError(err.message || 'Credenciales incorrectas');
        } finally {
            btnText.classList.remove('hidden');
            btnLoader.classList.add('hidden');
        }
    };

    const showLoginError = (msg) => {
        const el = document.getElementById('loginError');
        el.textContent = msg;
        el.classList.remove('hidden');
    };

    // ── Vista helpers ─────────────────────────────────────────

    const showLogin = () => {
        document.getElementById('loginView').classList.remove('hidden');
        document.getElementById('loginView').classList.add('active');
        document.getElementById('registerView').classList.add('hidden');
        document.getElementById('registerView').classList.remove('active');
        document.getElementById('appView').classList.add('hidden');
        document.getElementById('appView').classList.remove('active');
    };

    const showRegister = () => {
        document.getElementById('registerView').classList.remove('hidden');
        document.getElementById('registerView').classList.add('active');
        document.getElementById('loginView').classList.add('hidden');
        document.getElementById('loginView').classList.remove('active');
        document.getElementById('appView').classList.add('hidden');
        document.getElementById('appView').classList.remove('active');
        // Limpiar errores al cambiar de vista
        document.getElementById('registerError').classList.add('hidden');
        document.getElementById('registerSuccess').classList.add('hidden');
    };

    const showApp = () => {
        document.getElementById('loginView').classList.add('hidden');
        document.getElementById('loginView').classList.remove('active');
        document.getElementById('registerView').classList.add('hidden');
        document.getElementById('registerView').classList.remove('active');
        document.getElementById('appView').classList.remove('hidden');
        document.getElementById('appView').classList.add('active');
        setupAppUI();
    };

    // ── Register flow ─────────────────────────────────────────

    const handleRegisterSubmit = async (e) => {
        e.preventDefault();

        const username = document.getElementById('regUsername').value.trim();
        const email    = document.getElementById('regEmail').value.trim();
        const password = document.getElementById('regPassword').value;
        const fullName = document.getElementById('regFullName').value.trim();
        const errorEl  = document.getElementById('registerError');
        const successEl= document.getElementById('registerSuccess');
        const btnText  = document.querySelector('#registerBtn .btn-text');
        const btnLoader= document.querySelector('#registerBtn .btn-loader');

        errorEl.classList.add('hidden');
        successEl.classList.add('hidden');

        if (!username || !email || !password) {
            errorEl.textContent = 'Username, email y contraseña son obligatorios.';
            errorEl.classList.remove('hidden');
            return;
        }

        btnText.classList.add('hidden');
        btnLoader.classList.remove('hidden');

        try {
            await Api.register({ username, email, password, fullName });
            successEl.textContent = '¡Cuenta creada! Podés iniciar sesión.';
            successEl.classList.remove('hidden');
            // Volver al login después de 2 segundos
            setTimeout(() => showLogin(), 2000);
        } catch (err) {
            errorEl.textContent = err.message || 'Error al registrar usuario.';
            errorEl.classList.remove('hidden');
        } finally {
            btnText.classList.remove('hidden');
            btnLoader.classList.add('hidden');
        }
    };

    const setupAppUI = () => {
        const user = getUser();
        if (!user) return;

        // Sidebar user info
        document.getElementById('sidebarUsername').textContent = user.username;
        document.getElementById('sidebarRole').textContent     = user.roles[0]?.replace('ROLE_', '') || '';
        document.getElementById('userAvatar').textContent      = user.username[0].toUpperCase();

        // Mostrar/ocultar items de nav según rol
        document.querySelectorAll('.admin-only').forEach(el => {
            el.classList.toggle('hidden', !isAdmin());
        });
        document.querySelectorAll('.analyst-only').forEach(el => {
            el.classList.toggle('hidden', !isAnalyst());
        });
    };

    // ── Evento token expirado ─────────────────────────────────
    window.addEventListener('auth:expired', () => {
        showToast('Tu sesión expiró. Ingresá nuevamente.', 'error');
        showLogin();
    });

    return {
        init, handleLoginSubmit, handleRegisterSubmit,
        logout, showLogin, showRegister,
        getUser, isLoggedIn, isAdmin, isAnalyst
    };
})();