/**
 * incidents.js — Renderizado, formularios y acciones de incidentes.
 */

const Incidents = (() => {

    let currentPage  = 0;
    let currentFilters = {};

    // ── Labels helpers ────────────────────────────────────────

    const STATUS_LABELS = {
        OPEN:        'Abierto',
        IN_PROGRESS: 'En progreso',
        RESOLVED:    'Resuelto',
        CLOSED:      'Cerrado',
        REJECTED:    'Rechazado'
    };

    const PRIORITY_LABELS = {
        CRITICAL: 'Crítico',
        HIGH:     'Alto',
        MEDIUM:   'Medio',
        LOW:      'Bajo'
    };

    const formatDate = (iso) => {
        if (!iso) return '—';
        return new Date(iso).toLocaleString('es-UY', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    };

    // ── Render lista de incidentes ────────────────────────────

    const renderList = async (page = 0, filters = {}) => {
        currentPage    = page;
        currentFilters = filters;

        const container = document.getElementById('incidentList');
        const pagination = document.getElementById('incidentPagination');
        container.innerHTML = '<div class="empty-state">Cargando…</div>';

        try {
            const data = await Api.getIncidents({ page, size: 10, ...filters });
            renderCards(container, data.content);
            renderPagination(pagination, data, (p) => renderList(p, currentFilters));
        } catch (err) {
            container.innerHTML = `<div class="empty-state">${err.message}</div>`;
        }
    };

    const renderCards = (container, incidents) => {
        if (!incidents || incidents.length === 0) {
            container.innerHTML = '<div class="empty-state">No hay incidentes que coincidan con los filtros.</div>';
            return;
        }

        container.innerHTML = incidents.map(inc => `
      <div class="incident-card" data-id="${inc.id}">
        <div class="incident-title">#${inc.id} — ${escHtml(inc.title)}</div>
        <div class="incident-meta">
          <span>📁 ${escHtml(inc.category || 'Sin categoría')}</span>
          <span>👤 ${escHtml(inc.reportedBy?.username || '—')}</span>
          <span>🕐 ${formatDate(inc.createdAt)}</span>
          ${inc.assignedTo ? `<span>🔧 ${escHtml(inc.assignedTo.username)}</span>` : ''}
        </div>
        <div class="incident-badges">
          <span class="badge badge-${inc.status}">${STATUS_LABELS[inc.status] || inc.status}</span>
          <span class="badge badge-${inc.priority}">${PRIORITY_LABELS[inc.priority] || inc.priority}</span>
        </div>
      </div>
    `).join('');

        // Click para ver detalle
        container.querySelectorAll('.incident-card').forEach(card => {
            card.addEventListener('click', () => openDetail(Number(card.dataset.id)));
        });
    };

    // ── Detalle de incidente ──────────────────────────────────

    const openDetail = async (id) => {
        openModal('Detalle del Incidente', '<div class="empty-state">Cargando…</div>');
        try {
            const inc = await Api.getIncident(id);
            renderDetail(inc);
        } catch (err) {
            document.getElementById('modalBody').innerHTML =
                `<div class="empty-state">${err.message}</div>`;
        }
    };

    const renderDetail = (inc) => {
        const canEdit    = Auth.isAdmin() || Auth.isAnalyst() ||
            (Auth.getUser()?.id === inc.reportedBy?.id);
        const canDelete  = Auth.isAdmin();
        const canStatus  = Auth.isAdmin() || Auth.isAnalyst();

        document.getElementById('modalTitle').textContent = `#${inc.id} — ${inc.title}`;
        document.getElementById('modalBody').innerHTML = `
      <div class="detail-section">
        <div class="detail-row">
          <label>Descripción</label>
          <p>${escHtml(inc.description)}</p>
        </div>
        <div class="detail-badges">
          <span class="badge badge-${inc.status}">${STATUS_LABELS[inc.status] || inc.status}</span>
          <span class="badge badge-${inc.priority}">${PRIORITY_LABELS[inc.priority] || inc.priority}</span>
        </div>
        <div class="field-row">
          <div class="detail-row">
            <label>Categoría</label>
            <p>${escHtml(inc.category || '—')}</p>
          </div>
          <div class="detail-row">
            <label>Ubicación</label>
            <p>${escHtml(inc.location || '—')}</p>
          </div>
        </div>
        <div class="field-row">
          <div class="detail-row">
            <label>Reportado por</label>
            <p>${escHtml(inc.reportedBy?.username || '—')}</p>
          </div>
          <div class="detail-row">
            <label>Asignado a</label>
            <p>${inc.assignedTo ? escHtml(inc.assignedTo.username) : '—'}</p>
          </div>
        </div>
        ${inc.resolutionNotes ? `
        <div class="detail-row">
          <label>Notas de resolución</label>
          <p>${escHtml(inc.resolutionNotes)}</p>
        </div>` : ''}
        <div class="field-row">
          <div class="detail-row">
            <label>Creado</label>
            <p>${formatDate(inc.createdAt)}</p>
          </div>
          <div class="detail-row">
            <label>Actualizado</label>
            <p>${formatDate(inc.updatedAt)}</p>
          </div>
        </div>
        <div class="detail-actions">
          ${canEdit   ? `<button class="btn-secondary sm" id="editIncidentBtn">Editar</button>` : ''}
          ${canStatus ? `<button class="btn-secondary sm" id="changeStatusBtn">Cambiar estado</button>` : ''}
          ${canDelete ? `<button class="btn-danger" id="deleteIncidentBtn">Eliminar</button>` : ''}
        </div>
      </div>
    `;

        // Acciones
        document.getElementById('editIncidentBtn')
            ?.addEventListener('click', () => openEditForm(inc));
        document.getElementById('changeStatusBtn')
            ?.addEventListener('click', () => openStatusForm(inc));
        document.getElementById('deleteIncidentBtn')
            ?.addEventListener('click', () => confirmDelete(inc.id));
    };

    // ── Formulario nuevo / editar ─────────────────────────────

    const openCreateForm = () => {
        openModal('Nuevo Incidente', buildIncidentForm(null));
        document.getElementById('incidentFormSubmit')
            .addEventListener('click', () => submitIncidentForm(null));
    };

    const openEditForm = (inc) => {
        openModal('Editar Incidente', buildIncidentForm(inc));
        document.getElementById('incidentFormSubmit')
            .addEventListener('click', () => submitIncidentForm(inc.id));
    };

    const buildIncidentForm = (inc) => `
    <div class="field">
      <label>Título *</label>
      <input type="text" id="fTitle" value="${escHtml(inc?.title || '')}" maxlength="150"/>
    </div>
    <div class="field">
      <label>Descripción *</label>
      <textarea id="fDescription" rows="4" maxlength="2000">${escHtml(inc?.description || '')}</textarea>
    </div>
    <div class="field-row">
      <div class="field">
        <label>Prioridad *</label>
        <select id="fPriority">
          ${['CRITICAL','HIGH','MEDIUM','LOW'].map(p =>
        `<option value="${p}" ${inc?.priority === p ? 'selected' : ''}>${PRIORITY_LABELS[p]}</option>`
    ).join('')}
        </select>
      </div>
      <div class="field">
        <label>Categoría</label>
        <input type="text" id="fCategory" value="${escHtml(inc?.category || '')}" maxlength="100"/>
      </div>
    </div>
    <div class="field">
      <label>Ubicación</label>
      <input type="text" id="fLocation" value="${escHtml(inc?.location || '')}" maxlength="255"/>
    </div>
    <div id="formError" class="msg-error hidden"></div>
    <div class="modal-actions">
      <button class="btn-secondary sm" id="cancelFormBtn">Cancelar</button>
      <button class="btn-primary sm" id="incidentFormSubmit">Guardar</button>
    </div>
  `;

    const submitIncidentForm = async (id) => {
        const title       = document.getElementById('fTitle').value.trim();
        const description = document.getElementById('fDescription').value.trim();
        const priority    = document.getElementById('fPriority').value;
        const category    = document.getElementById('fCategory').value.trim();
        const location    = document.getElementById('fLocation').value.trim();
        const errorEl     = document.getElementById('formError');

        if (!title || !description || !priority) {
            errorEl.textContent = 'Título, descripción y prioridad son obligatorios.';
            errorEl.classList.remove('hidden');
            return;
        }

        const payload = { title, description, priority, category, location };

        try {
            if (id) {
                await Api.updateIncident(id, payload);
                showToast('Incidente actualizado correctamente', 'success');
            } else {
                await Api.createIncident(payload);
                showToast('Incidente creado correctamente', 'success');
            }
            closeModal();
            renderList(currentPage, currentFilters);
        } catch (err) {
            errorEl.textContent = err.message;
            errorEl.classList.remove('hidden');
        }
    };

    // ── Formulario cambio de estado ───────────────────────────

    const openStatusForm = (inc) => {
        const allowedStatuses = Auth.isAdmin()
            ? ['OPEN','IN_PROGRESS','RESOLVED','CLOSED','REJECTED']
            : ['OPEN','IN_PROGRESS','RESOLVED'];

        openModal('Cambiar Estado', `
      <div class="field">
        <label>Nuevo estado *</label>
        <select id="fStatus">
          ${allowedStatuses.map(s =>
            `<option value="${s}" ${inc.status === s ? 'selected' : ''}>${STATUS_LABELS[s]}</option>`
        ).join('')}
        </select>
      </div>
      <div class="field">
        <label>Notas de resolución</label>
        <textarea id="fNotes" rows="3" placeholder="Requerido para RESOLVED y CLOSED…">${escHtml(inc.resolutionNotes || '')}</textarea>
      </div>
      <div id="statusError" class="msg-error hidden"></div>
      <div class="modal-actions">
        <button class="btn-secondary sm" id="cancelStatusBtn">Cancelar</button>
        <button class="btn-primary sm" id="submitStatusBtn">Actualizar</button>
      </div>
    `);

        document.getElementById('cancelStatusBtn').addEventListener('click', closeModal);
        document.getElementById('submitStatusBtn').addEventListener('click', async () => {
            const status          = document.getElementById('fStatus').value;
            const resolutionNotes = document.getElementById('fNotes').value.trim();
            const errorEl         = document.getElementById('statusError');

            try {
                await Api.updateStatus(inc.id, { status, resolutionNotes });
                showToast('Estado actualizado correctamente', 'success');
                closeModal();
                renderList(currentPage, currentFilters);
            } catch (err) {
                errorEl.textContent = err.message;
                errorEl.classList.remove('hidden');
            }
        });
    };

    // ── Confirmar eliminación ─────────────────────────────────

    const confirmDelete = (id) => {
        openModal('Confirmar eliminación', `
      <p style="color:var(--text2);line-height:1.6">
        ¿Estás seguro que querés eliminar el incidente <strong>#${id}</strong>?<br/>
        Esta acción es <strong style="color:var(--danger)">irreversible</strong>.
      </p>
      <div class="modal-actions">
        <button class="btn-secondary sm" id="cancelDeleteBtn">Cancelar</button>
        <button class="btn-danger" id="confirmDeleteBtn">Eliminar</button>
      </div>
    `);

        document.getElementById('cancelDeleteBtn').addEventListener('click', closeModal);
        document.getElementById('confirmDeleteBtn').addEventListener('click', async () => {
            try {
                await Api.deleteIncident(id);
                showToast('Incidente eliminado', 'success');
                closeModal();
                renderList(currentPage, currentFilters);
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    };

    // ── Paginación ────────────────────────────────────────────

    const renderPagination = (container, data, onPageChange) => {
        if (data.totalPages <= 1) { container.innerHTML = ''; return; }

        let html = `<button class="page-btn" ${data.first ? 'disabled' : ''} data-page="${data.page - 1}">‹</button>`;

        for (let i = 0; i < data.totalPages; i++) {
            html += `<button class="page-btn ${i === data.page ? 'current' : ''}" data-page="${i}">${i + 1}</button>`;
        }

        html += `<button class="page-btn" ${data.last ? 'disabled' : ''} data-page="${data.page + 1}">›</button>`;
        container.innerHTML = html;

        container.querySelectorAll('.page-btn:not([disabled])').forEach(btn => {
            btn.addEventListener('click', () => onPageChange(Number(btn.dataset.page)));
        });
    };

    // ── Render lista críticos (dashboard) ─────────────────────

    const renderCriticalList = async () => {
        const container = document.getElementById('criticalList');
        container.innerHTML = '<div class="empty-state">Cargando…</div>';
        try {
            const data = await Api.getCritical();
            renderCards(container, data.content);
        } catch {
            container.innerHTML = '<div class="empty-state">No se pudo cargar.</div>';
        }
    };

    // ── Render lista recientes (reports) ─────────────────────

    const renderRecentList = async () => {
        const container = document.getElementById('recentList');
        container.innerHTML = '<div class="empty-state">Cargando…</div>';
        try {
            const data = await Api.getRecent(30);
            renderCards(container, data.content);
        } catch {
            container.innerHTML = '<div class="empty-state">No se pudo cargar.</div>';
        }
    };

    // ── Escape HTML ───────────────────────────────────────────

    const escHtml = (str) => {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    };

    return {
        renderList,
        renderCriticalList,
        renderRecentList,
        openCreateForm
    };

})();