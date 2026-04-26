/**
 * app.js — Orquestador principal de la SPA.
 *
 * Responsabilidades:
 * - Inicializar la app
 * - Manejar navegación entre tabs
 * - Cargar datos según el tab activo
 * - Renderizar dashboard, stats, usuarios
 * - Toast y modal globales
 */

// ── Modal global ──────────────────────────────────────────

const openModal = (title, bodyHtml) => {
    document.getElementById('modalTitle').textContent = title;
    document.getElementById('modalBody').innerHTML    = bodyHtml;
    const modal = document.getElementById('modal');
    modal.classList.remove('hidden');
    modal.classList.add('active');

    // Cancelar desde botón dinámico
    document.getElementById('cancelFormBtn')
        ?.addEventListener('click', closeModal);
};

const closeModal = () => {
    const modal = document.getElementById('modal');
    modal.classList.add('hidden');
    modal.classList.remove('active');
};

// ── Toast global ──────────────────────────────────────────

const showToast = (message, type = 'info') => {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className   = `toast ${type}`;
    toast.classList.remove('hidden');
    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => toast.classList.add('hidden'), 3500);
};

// ── Dashboard ─────────────────────────────────────────────

const loadDashboard = async () => {
    // Fecha actual
    document.getElementById('pageDate').textContent =
        new Date().toLocaleDateString('es-UY', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });

    // Stats (solo para ADMIN y ANALYST)
    if (Auth.isAnalyst()) {
        try {
            const stats = await Api.getStats();
            renderDashboardStats(stats);
        } catch {
            document.getElementById('statsGrid').innerHTML =
                '<div class="stat-card loading">Sin permisos para ver estadísticas.</div>';
        }
    } else {
        document.getElementById('statsGrid').innerHTML =
            '<div class="stat-card loading">Vista disponible para ANALYST y ADMIN.</div>';
    }

    // Incidentes críticos
    await Incidents.renderCriticalList();
};

const renderDashboardStats = (stats) => {
    const grid = document.getElementById('statsGrid');
    grid.innerHTML = `
    <div class="stat-card">
      <div class="stat-label">Total</div>
      <div class="stat-value">${stats.totalIncidents}</div>
    </div>
    <div class="stat-card accent">
      <div class="stat-label">Abiertos</div>
      <div class="stat-value">${stats.openIncidents}</div>
    </div>
    <div class="stat-card warn">
      <div class="stat-label">En progreso</div>
      <div class="stat-value">${stats.inProgressIncidents}</div>
    </div>
    <div class="stat-card success">
      <div class="stat-label">Resueltos</div>
      <div class="stat-value">${stats.resolvedIncidents}</div>
    </div>
    <div class="stat-card">
      <div class="stat-label">Cerrados</div>
      <div class="stat-value">${stats.closedIncidents}</div>
    </div>
    <div class="stat-card accent">
      <div class="stat-label">Últimos 30 días</div>
      <div class="stat-value">${stats.incidentsLast30Days}</div>
    </div>
  `;
};

// ── Reports tab ───────────────────────────────────────────

const loadReports = async () => {
    try {
        const stats = await Api.getStats();
        renderReportStats(stats);
    } catch {
        document.getElementById('reportStatsGrid').innerHTML =
            '<div class="stat-card loading">Error al cargar estadísticas.</div>';
    }
    await Incidents.renderRecentList();
};

const renderReportStats = (stats) => {
    const priorities = stats.incidentsByPriority || {};
    const categories = stats.incidentsByCategory || {};

    const catHtml = Object.entries(categories)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 4)
        .map(([cat, count]) => `
      <div class="stat-card">
        <div class="stat-label">${cat}</div>
        <div class="stat-value" style="font-size:1.4rem">${count}</div>
      </div>
    `).join('');

    document.getElementById('reportStatsGrid').innerHTML = `
    <div class="stat-card danger">
      <div class="stat-label">Críticos</div>
      <div class="stat-value">${priorities['CRITICAL'] || 0}</div>
    </div>
    <div class="stat-card warn">
      <div class="stat-label">Alta prioridad</div>
      <div class="stat-value">${priorities['HIGH'] || 0}</div>
    </div>
    <div class="stat-card accent">
      <div class="stat-label">Media prioridad</div>
      <div class="stat-value">${priorities['MEDIUM'] || 0}</div>
    </div>
    <div class="stat-card">
      <div class="stat-label">Baja prioridad</div>
      <div class="stat-value">${priorities['LOW'] || 0}</div>
    </div>
    ${catHtml}
  `;
};

// ── Users tab ─────────────────────────────────────────────

let usersCurrentPage = 0;

const loadUsers = async (page = 0) => {
    usersCurrentPage = page;
    const container  = document.getElementById('userList');
    const pagination = document.getElementById('userPagination');
    container.innerHTML = '<div class="empty-state">Cargando…</div>';

    try {
        const data = await Api.getUsers({ page, size: 10 });
        renderUserCards(container, data.content);
        renderUserPagination(pagination, data);
    } catch (err) {
        container.innerHTML = `<div class="empty-state">${err.message}</div>`;
    }
};

const renderUserCards = (container, users) => {
    if (!users || users.length === 0) {
        container.innerHTML = '<div class="empty-state">No hay usuarios.</div>';
        return;
    }

    container.innerHTML = users.map(u => `
    <div class="user-card">
      <div class="user-card-avatar">${u.username[0].toUpperCase()}</div>
      <div class="user-card-info">
        <strong>${escHtml(u.username)}</strong>
        <small>${escHtml(u.email)} · ${escHtml(u.fullName || '—')}</small>
      </div>
      <div class="user-card-badges">
        ${u.roles.map(r => `<span class="badge badge-MEDIUM">${r.replace('ROLE_', '')}</span>`).join('')}
        <span class="badge ${u.enabled ? 'badge-enabled' : 'badge-disabled'}">
          ${u.enabled ? 'Activo' : 'Inactivo'}
        </span>
      </div>
      <div class="user-card-actions">
        ${u.enabled
        ? `<button class="btn-secondary sm" data-disable="${u.id}">Deshabilitar</button>`
        : ''}
        <button class="btn-danger" data-delete="${u.id}">Eliminar</button>
      </div>
    </div>
  `).join('');

    // Deshabilitar usuario
    container.querySelectorAll('[data-disable]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = Number(btn.dataset.disable);
            try {
                await Api.disableUser(id);
                showToast('Usuario deshabilitado', 'success');
                loadUsers(usersCurrentPage);
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    });

    // Eliminar usuario
    container.querySelectorAll('[data-delete]').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = Number(btn.dataset.delete);
            openModal('Confirmar eliminación', `
        <p style="color:var(--text2);line-height:1.6">
          ¿Eliminás permanentemente al usuario <strong>#${id}</strong>?<br/>
          Esta acción es <strong style="color:var(--danger)">irreversible</strong>.
        </p>
        <div class="modal-actions">
          <button class="btn-secondary sm" id="cancelDeleteUserBtn">Cancelar</button>
          <button class="btn-danger" id="confirmDeleteUserBtn">Eliminar</button>
        </div>
      `);
            document.getElementById('cancelDeleteUserBtn').addEventListener('click', closeModal);
            document.getElementById('confirmDeleteUserBtn').addEventListener('click', async () => {
                try {
                    await Api.deleteUser(id);
                    showToast('Usuario eliminado', 'success');
                    closeModal();
                    loadUsers(usersCurrentPage);
                } catch (err) {
                    showToast(err.message, 'error');
                }
            });
        });
    });
};

const renderUserPagination = (container, data) => {
    if (data.totalPages <= 1) { container.innerHTML = ''; return; }
    let html = `<button class="page-btn" ${data.first ? 'disabled' : ''} data-page="${data.page - 1}">‹</button>`;
    for (let i = 0; i < data.totalPages; i++) {
        html += `<button class="page-btn ${i === data.page ? 'current' : ''}" data-page="${i}">${i + 1}</button>`;
    }
    html += `<button class="page-btn" ${data.last ? 'disabled' : ''} data-page="${data.page + 1}">›</button>`;
    container.innerHTML = html;
    container.querySelectorAll('.page-btn:not([disabled])').forEach(btn => {
        btn.addEventListener('click', () => loadUsers(Number(btn.dataset.page)));
    });
};

// ── Escape HTML ───────────────────────────────────────────

const escHtml = (str) => {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
};

// ── Navegación entre tabs ─────────────────────────────────

const switchTab = (tabName) => {
    // Nav items
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.toggle('active', item.dataset.tab === tabName);
    });

    // Tab contents
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
        tab.classList.add('hidden');
    });

    const activeTab = document.getElementById(`tab${capitalize(tabName)}`);
    if (activeTab) {
        activeTab.classList.remove('hidden');
        activeTab.classList.add('active');
    }

    // Cargar datos del tab
    switch (tabName) {
        case 'dashboard': loadDashboard();                        break;
        case 'incidents': Incidents.renderList();                 break;
        case 'reports':   loadReports();                          break;
        case 'users':     loadUsers();                            break;
    }
};

const capitalize = (str) => str.charAt(0).toUpperCase() + str.slice(1);

// ── Bootstrap ─────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {

    // Login form
    document.getElementById('loginForm')
        .addEventListener('submit', Auth.handleLoginSubmit);

    // Register form
    document.getElementById('registerForm')
        .addEventListener('submit', Auth.handleRegisterSubmit);

    // Navegación login ↔ register
    document.getElementById('goToRegister')
        ?.addEventListener('click', (e) => { e.preventDefault(); Auth.showRegister(); });
    document.getElementById('goToLogin')
        ?.addEventListener('click', (e) => { e.preventDefault(); Auth.showLogin(); });

    // Logout
    document.getElementById('logoutBtn')
        .addEventListener('click', Auth.logout);

    // Navegación sidebar
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', () => switchTab(item.dataset.tab));
    });

    // Cerrar modal con X o click fuera
    document.getElementById('modalClose')
        .addEventListener('click', closeModal);
    document.getElementById('modal')
        .addEventListener('click', (e) => { if (e.target.id === 'modal') closeModal(); });

    // Botón nuevo incidente
    document.getElementById('newIncidentBtn')
        ?.addEventListener('click', Incidents.openCreateForm);

    // Filtros de incidentes
    document.getElementById('applyFiltersBtn')
        ?.addEventListener('click', () => {
            const filters = {
                keyword:  document.getElementById('filterKeyword').value.trim(),
                status:   document.getElementById('filterStatus').value,
                priority: document.getElementById('filterPriority').value
            };
            Incidents.renderList(0, filters);
        });

    // Enter en el campo de búsqueda
    document.getElementById('filterKeyword')
        ?.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') document.getElementById('applyFiltersBtn').click();
        });

    // Iniciar autenticación
    Auth.init();

    // Si ya estaba logueado, cargar dashboard directamente
    if (Auth.isLoggedIn()) {
        switchTab('dashboard');
    }
});