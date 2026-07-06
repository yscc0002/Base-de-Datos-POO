


const state = {
    user: null,
    currentView: 'dashboard',
    pacientes: [],
    medicos: [],
    citas: [],
    consultas: [],
    vacunas: [],
    enfermedades: [],
    respaldos: []
};


document.addEventListener('DOMContentLoaded', () => {
    initClock();
    checkSession();
    setupEventListeners();
});


function initClock() {
    const clockText = document.getElementById('clock-text');
    const welcomeDateText = document.getElementById('welcome-date-text');
    
    const updateTime = () => {
        const now = new Date();
        clockText.textContent = now.toLocaleTimeString('es-ES');
    };
    
    
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    welcomeDateText.textContent = new Date().toLocaleDateString('es-ES', options);
    
    setInterval(updateTime, 1000);
    updateTime();
}


async function checkSession() {
    try {
        const response = await fetch('/api/usuarios/me');
        if (response.ok) {
            const user = await response.json();
            loginSuccess(user);
        } else {
            showScreen('login-screen');
        }
    } catch (error) {
        console.error('Error checking session:', error);
        showScreen('login-screen');
    }
}


function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.getElementById(screenId).classList.add('active');
    lucide.createIcons();
}


function showView(viewId) {
    state.currentView = viewId;
    
    
    document.querySelectorAll('.nav-item').forEach(item => {
        if (item.getAttribute('data-view') === viewId) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });
    
    
    const viewTitleMap = {
        'dashboard': 'Panel General',
        'pacientes': 'Gestión de Pacientes',
        'citas': 'Control de Citas Médicas',
        'consultas': 'Historial de Consultas Médicas',
        'medicos': 'Directorio de Médicos',
        'vacunas': 'Catálogo de Vacunas',
        'enfermedades': 'Enfermedades Vigiladas',
        'respaldos': 'Respaldos de Base de Datos'
    };
    document.getElementById('current-view-title').textContent = viewTitleMap[viewId] || 'Panel';

    
    document.querySelectorAll('.view-pane').forEach(pane => {
        if (pane.id === `view-${viewId}`) {
            pane.classList.add('active');
        } else {
            pane.classList.remove('active');
        }
    });

    
    loadViewData(viewId);
}


function loadViewData(viewId) {
    switch (viewId) {
        case 'dashboard':
            loadDashboardStats();
            break;
        case 'pacientes':
            fetchPacientes();
            break;
        case 'citas':
            fetchCitas();
            break;
        case 'consultas':
            fetchConsultas();
            break;
        case 'medicos':
            fetchMedicos();
            break;
        case 'vacunas':
            fetchVacunas();
            break;
        case 'enfermedades':
            fetchEnfermedades();
            break;
        case 'respaldos':
            fetchRespaldos();
            break;
    }
}


function setupEventListeners() {
    
    const togglePasswordBtn = document.getElementById('toggle-password');
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.getElementById('toggle-icon');
    
    togglePasswordBtn.addEventListener('click', () => {
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);
        toggleIcon.setAttribute('data-lucide', type === 'password' ? 'eye' : 'eye-off');
        lucide.createIcons();
    });

    
    document.getElementById('login-form').addEventListener('submit', handleLoginSubmit);

    
    document.getElementById('logout-btn').addEventListener('click', handleLogout);

    
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', () => {
            const viewId = item.getAttribute('data-view');
            showView(viewId);
        });
    });

    
    document.querySelectorAll('.close-modal, .btn-cancel').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const modal = e.target.closest('.modal');
            if (modal) modal.classList.remove('active');
        });
    });

    
    document.getElementById('btn-nuevo-paciente').addEventListener('click', () => openPacienteModal());
    document.getElementById('btn-nueva-cita').addEventListener('click', () => openCitaModal());
    document.getElementById('btn-nueva-consulta').addEventListener('click', () => openConsultaModal());
    document.getElementById('btn-nuevo-medico').addEventListener('click', () => openMedicoModal());
    document.getElementById('btn-nueva-vacuna').addEventListener('click', () => openVacunaModal());
    document.getElementById('btn-nueva-enfermedad').addEventListener('click', () => openEnfermedadModal());
    document.getElementById('btn-crear-respaldo').addEventListener('click', handleCreateBackup);

    
    document.getElementById('form-paciente').addEventListener('submit', handlePacienteSubmit);
    document.getElementById('form-cita').addEventListener('submit', handleCitaSubmit);
    document.getElementById('form-posponer-cita').addEventListener('submit', handlePosponerCitaSubmit);
    document.getElementById('form-consulta').addEventListener('submit', handleConsultaSubmit);
    document.getElementById('form-medico').addEventListener('submit', handleMedicoSubmit);
    document.getElementById('form-vacuna').addEventListener('submit', handleVacunaSubmit);
    document.getElementById('form-enfermedad').addEventListener('submit', handleEnfermedadSubmit);

    
    setupSearchFilter('search-paciente', 'pacientes-table-body', filterPacienteRow);
    setupSearchFilter('search-cita', 'citas-table-body', filterCitaRow);
    setupSearchFilter('search-consulta', 'consultas-table-body', filterConsultaRow);
    setupSearchFilter('search-medico', 'medicos-table-body', filterMedicoRow);
    setupSearchFilter('search-vacuna', 'vacunas-table-body', filterCatalogRow);
    setupSearchFilter('search-enfermedad', 'enfermedades-table-body', filterCatalogRow);

    
    document.getElementById('btn-buscar-cita-paciente').addEventListener('click', handleCitaPatientSearch);
    document.getElementById('btn-buscar-cons-paciente').addEventListener('click', handleConsultaPatientSearch);

    
    document.getElementById('cita-fecha').addEventListener('change', handleCitaDateChange);

    
    document.getElementById('btn-aplicar-vacuna-trigger').addEventListener('click', handleApplyVaccine);

    
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const tabsContainer = e.target.closest('.details-tabs-container');
            tabsContainer.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            tabsContainer.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
            
            e.target.classList.add('active');
            const targetPaneId = e.target.getAttribute('data-tab');
            document.getElementById(targetPaneId).classList.add('active');
        });
    });
}





async function handleLoginSubmit(e) {
    e.preventDefault();
    const errorAlert = document.getElementById('login-error');
    const errorText = document.getElementById('login-error-text');
    errorAlert.classList.add('hide');

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/api/usuarios/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const user = await response.json();
            loginSuccess(user);
            showToast('Sesión iniciada correctamente', 'success');
        } else {
            const data = await response.json();
            errorText.textContent = data.message || 'Usuario o contraseña incorrectos.';
            errorAlert.classList.remove('hide');
        }
    } catch (error) {
        console.error('Error logging in:', error);
        errorText.textContent = 'Error de conexión con el servidor.';
        errorAlert.classList.remove('hide');
    }
}

function loginSuccess(user) {
    state.user = user;
    
    
    document.getElementById('user-display-name').textContent = user.nombreUsuario;
    document.getElementById('user-display-role').textContent = user.rol;
    document.getElementById('info-role-badge').textContent = user.rol;
    document.getElementById('user-avatar-char').textContent = user.nombreUsuario.charAt(0).toUpperCase();

    
    if (user.rol.toLowerCase() === 'administrador') {
        document.querySelectorAll('.admin-only').forEach(el => el.classList.remove('hide'));
    } else {
        document.querySelectorAll('.admin-only').forEach(el => el.classList.add('hide'));
    }

    
    showScreen('app-screen');
    showView('dashboard');
}

async function handleLogout() {
    try {
        await fetch('/api/usuarios/logout', { method: 'POST' });
        state.user = null;
        document.getElementById('login-form').reset();
        showScreen('login-screen');
        showToast('Sesión cerrada con éxito', 'info');
    } catch (error) {
        console.error('Error logging out:', error);
        showToast('Error al cerrar sesión', 'error');
    }
}





async function loadDashboardStats() {
    try {
        
        const [pacRes, citRes, medRes, enfRes, conRes] = await Promise.all([
            fetch('/api/pacientes'),
            fetch('/api/citas'),
            fetch('/api/medicos'),
            fetch('/api/enfermedades'),
            fetch('/api/consultas')
        ]);

        const patients = await pacRes.json();
        const appointments = await citRes.json();
        const doctors = await medRes.json();
        const diseases = await enfRes.json();
        const consultations = await conRes.json();

        
        document.getElementById('stat-patients-count').textContent = patients.length;
        document.getElementById('stat-appointments-count').textContent = appointments.length;
        document.getElementById('stat-doctors-count').textContent = doctors.length;
        document.getElementById('stat-diseases-count').textContent = diseases.length;
        document.getElementById('info-consultas-count').textContent = consultations.length;

        
        const tableBody = document.getElementById('dashboard-citas-table');
        tableBody.innerHTML = '';

        const upcomingCitas = appointments.slice(0, 5);
        if (upcomingCitas.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="4" class="empty-row">No hay citas programadas.</td></tr>';
        } else {
            upcomingCitas.forEach(cita => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${cita.nombre} ${cita.apellido}</td>
                    <td>${cita.cedula}</td>
                    <td>Dr. ${cita.medico.nombre} ${cita.medico.apellido}</td>
                    <td><span class="status-badge warning">${cita.fecha}</span></td>
                `;
                tableBody.appendChild(tr);
            });
        }

    } catch (error) {
        console.error('Error loading dashboard stats:', error);
    }
}





async function fetchPacientes() {
    try {
        const res = await fetch('/api/pacientes');
        state.pacientes = await res.json();
        renderPacientes();
    } catch (error) {
        showToast('Error al cargar pacientes', 'error');
    }
}

function renderPacientes() {
    const tbody = document.getElementById('pacientes-table-body');
    tbody.innerHTML = '';

    if (state.pacientes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="empty-row">No hay pacientes registrados.</td></tr>';
        return;
    }

    state.pacientes.forEach(p => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${p.id}</td>
            <td>${p.cedula}</td>
            <td style="font-weight:600">${p.nombre} ${p.apellido}</td>
            <td>${p.edad} años</td>
            <td>${p.sexo}</td>
            <td>${p.telefono}</td>
            <td><span class="status-badge active" style="background-color:rgba(239, 68, 68, 0.1); color:#fca5a5">${p.tipoSangre}</span></td>
            <td>
                <div class="btn-action-wrapper">
                    <button class="btn-action view" onclick="viewPacienteDetails('${p.id}')" title="Ver Expediente"><i data-lucide="eye"></i></button>
                    <button class="btn-action edit" onclick="openPacienteModal('${p.id}')" title="Editar"><i data-lucide="edit-3"></i></button>
                    <button class="btn-action delete" onclick="deletePaciente('${p.id}')" title="Eliminar"><i data-lucide="trash-2"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
    lucide.createIcons();
}

function openPacienteModal(id = null) {
    const form = document.getElementById('form-paciente');
    form.reset();
    document.getElementById('paciente-id').value = id || '';
    document.getElementById('modal-paciente-title').textContent = id ? 'Editar Paciente' : 'Nuevo Paciente';

    if (id) {
        const p = state.pacientes.find(item => item.id === id);
        if (p) {
            document.getElementById('pac-cedula').value = p.cedula;
            document.getElementById('pac-nombre').value = p.nombre;
            document.getElementById('pac-apellido').value = p.apellido;
            document.getElementById('pac-edad').value = p.edad;
            document.getElementById('pac-sexo').value = p.sexo;
            document.getElementById('pac-sangre').value = p.tipoSangre;
            document.getElementById('pac-peso').value = p.peso;
            document.getElementById('pac-estatura').value = p.estatura;
            document.getElementById('pac-telefono').value = p.telefono;
            document.getElementById('pac-direccion').value = p.direccion;
        }
    }
    
    document.getElementById('modal-paciente').classList.add('active');
    lucide.createIcons();
}

async function handlePacienteSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('paciente-id').value;
    
    const body = {
        cedula: document.getElementById('pac-cedula').value.trim(),
        nombre: document.getElementById('pac-nombre').value.trim(),
        apellido: document.getElementById('pac-apellido').value.trim(),
        edad: parseInt(document.getElementById('pac-edad').value),
        sexo: document.getElementById('pac-sexo').value,
        tipoSangre: document.getElementById('pac-sangre').value,
        peso: parseFloat(document.getElementById('pac-peso').value),
        estatura: parseFloat(document.getElementById('pac-estatura').value),
        telefono: document.getElementById('pac-telefono').value.trim(),
        direccion: document.getElementById('pac-direccion').value.trim()
    };

    const url = id ? `/api/pacientes/${id}` : '/api/pacientes';
    const method = id ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (response.ok) {
            showToast(id ? 'Paciente actualizado con éxito' : 'Paciente registrado con éxito', 'success');
            document.getElementById('modal-paciente').classList.remove('active');
            fetchPacientes();
        } else {
            const data = await response.json();
            showToast(data.message || 'Error al guardar paciente', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}

async function deletePaciente(id) {
    if (!confirm('¿Está seguro de que desea eliminar este paciente? Sus consultas e historial permanecerán en los registros de forma interna.')) return;
    try {
        const response = await fetch(`/api/pacientes/${id}`, { method: 'DELETE' });
        if (response.ok) {
            showToast('Paciente desactivado con éxito', 'success');
            fetchPacientes();
        } else {
            showToast('Error al desactivar paciente', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}





async function viewPacienteDetails(id) {
    try {
        const response = await fetch(`/api/pacientes/${id}`);
        if (!response.ok) return showToast('Paciente no encontrado', 'error');
        const p = await response.json();

        
        document.getElementById('det-avatar').textContent = p.nombre.charAt(0).toUpperCase();
        document.getElementById('det-nombre-completo').textContent = `${p.nombre} ${p.apellido}`;
        document.getElementById('det-id-badge').textContent = p.id;
        document.getElementById('det-cedula').textContent = p.cedula;
        document.getElementById('det-edad-sexo').textContent = `${p.edad} años / ${p.sexo}`;
        document.getElementById('det-sangre').textContent = p.tipoSangre;
        document.getElementById('det-peso-estatura').textContent = `${p.peso} kg / ${p.estatura} m`;
        document.getElementById('det-telefono').textContent = p.telefono;
        document.getElementById('det-direccion').textContent = p.direccion;

        
        const modal = document.getElementById('modal-detalle-paciente');
        modal.dataset.pacienteId = p.id;

        
        await renderPatientVaccines(p);

        
        await renderPatientHistory(p.id);

        
        modal.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        modal.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
        modal.querySelector('[data-tab="tab-historial"]').classList.add('active');
        document.getElementById('tab-historial').classList.add('active');

        modal.classList.add('active');
        lucide.createIcons();
    } catch (error) {
        showToast('Error al cargar expediente', 'error');
    }
}

async function renderPatientVaccines(patient) {
    const listContainer = document.getElementById('paciente-vacunas-list');
    listContainer.innerHTML = '';

    
    if (!patient.vacunas || patient.vacunas.length === 0) {
        listContainer.innerHTML = '<p class="empty-info" style="grid-column: 1/-1">No hay vacunas aplicadas en este paciente.</p>';
    } else {
        patient.vacunas.forEach(v => {
            const card = document.createElement('div');
            card.className = 'vaccine-badge-card';
            card.innerHTML = `
                <i data-lucide="syringe"></i>
                <div class="vaccine-badge-info">
                    <h5>${v.nombre}</h5>
                    <p>${v.fabricante} (${v.dosis} ml)</p>
                </div>
            `;
            listContainer.appendChild(card);
        });
    }

    
    const select = document.getElementById('select-aplicar-vacuna');
    select.innerHTML = '<option value="">Seleccione una vacuna...</option>';
    
    
    const vRes = await fetch('/api/vacunas');
    const catalog = await vRes.json();

    
    const appliedIds = (patient.vacunas || []).map(v => v.id);
    const available = catalog.filter(v => !appliedIds.includes(v.id));

    available.forEach(v => {
        const option = document.createElement('option');
        option.value = v.id;
        option.textContent = `${v.nombre} - ${v.fabricante}`;
        select.appendChild(option);
    });
}

async function renderPatientHistory(pacienteId) {
    const timeline = document.getElementById('paciente-consulta-timeline');
    timeline.innerHTML = '';

    const response = await fetch(`/api/consultas/paciente/${pacienteId}`);
    const history = await response.json();

    if (history.length === 0) {
        timeline.innerHTML = '<p class="empty-info">No se registran consultas médicas previas en el historial.</p>';
        return;
    }

    
    history.reverse().forEach(c => {
        const item = document.createElement('div');
        item.className = `timeline-item ${c.esImportante ? 'important' : ''}`;
        
        item.innerHTML = `
            <div class="timeline-header">
                <h4>Dr. ${c.medico.nombre} ${c.medico.apellido}</h4>
                <span>Consulta #${c.id}</span>
            </div>
            <div class="timeline-body">
                <p><strong>Motivo:</strong> ${c.sintomas}</p>
                <p class="diagnostico"><strong>Diagnóstico:</strong> ${c.diagnostico}</p>
                ${c.enfermedadVigilada ? `<p style="margin-top:6px;"><span class="status-badge warning" style="font-size:10px; padding: 2px 8px;"><i data-lucide="shield-alert" style="width:10px; height:10px; margin-right:4px;"></i>Caso Vigilado: ${c.enfermedadVigilada.nombre} (${c.enfermedadVigilada.gravedad})</span></p>` : ''}
            </div>
        `;
        timeline.appendChild(item);
    });
}

async function handleApplyVaccine() {
    const modal = document.getElementById('modal-detalle-paciente');
    const pacienteId = modal.dataset.pacienteId;
    const select = document.getElementById('select-aplicar-vacuna');
    const vacunaId = select.value;

    if (!vacunaId) return showToast('Seleccione una vacuna para aplicar', 'warning');

    try {
        const response = await fetch(`/api/pacientes/${pacienteId}/vacunas`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ vacunaId })
        });

        if (response.ok) {
            const data = await response.json();
            showToast('Vacuna registrada con éxito', 'success');
            
            
            renderPatientVaccines(data.paciente);
        } else {
            const data = await response.json();
            showToast(data.message || 'Error al aplicar vacuna', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}





async function fetchCitas() {
    try {
        const res = await fetch('/api/citas');
        state.citas = await res.json();
        renderCitas();
    } catch (error) {
        showToast('Error al cargar citas', 'error');
    }
}

function renderCitas() {
    const tbody = document.getElementById('citas-table-body');
    tbody.innerHTML = '';

    if (state.citas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-row">No hay citas programadas en los registros.</td></tr>';
        return;
    }

    state.citas.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${c.id}</td>
            <td>${c.cedula}</td>
            <td style="font-weight:600">${c.nombre} ${c.apellido}</td>
            <td>Dr. ${c.medico.nombre} ${c.medico.apellido} (${c.medico.especialidad})</td>
            <td><span class="status-badge warning" style="font-weight:600">${c.fecha}</span></td>
            <td><span class="status-badge active">Agendada</span></td>
            <td>
                <div class="btn-action-wrapper">
                    <button class="btn-action view" onclick="openPosponerCitaModal('${c.id}')" title="Posponer / Reagendar"><i data-lucide="clock"></i></button>
                    <button class="btn-action delete" onclick="cancelCita('${c.id}')" title="Cancelar Cita"><i data-lucide="calendar-x"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
    lucide.createIcons();
}

async function handleCitaPatientSearch() {
    const cedula = document.getElementById('cita-cedula').value.trim();
    if (!cedula) return showToast('Ingrese una cédula para buscar', 'warning');

    const feedback = document.getElementById('cita-paciente-check');
    feedback.textContent = 'Buscando...';

    try {
        const res = await fetch(`/api/pacientes/cedula/${cedula}`);
        if (res.ok) {
            const p = await res.json();
            document.getElementById('cita-nombre').value = p.nombre;
            document.getElementById('cita-apellido').value = p.apellido;
            feedback.textContent = `Paciente encontrado: ${p.nombre} ${p.apellido}`;
            feedback.style.color = 'var(--success)';
        } else {
            feedback.textContent = 'Paciente nuevo (no registrado)';
            feedback.style.color = 'var(--warning)';
        }
    } catch (e) {
        feedback.textContent = '';
    }
}

async function handleCitaDateChange() {
    const fecha = document.getElementById('cita-fecha').value;
    const select = document.getElementById('cita-medico');
    select.innerHTML = '<option value="">(Primero seleccione una fecha)...</option>';
    select.disabled = true;

    if (!fecha) return;

    try {
        const response = await fetch(`/api/citas/disponibilidad-medicos?fecha=${fecha}`);
        const medicosDisponibles = await response.json();
        
        select.innerHTML = '<option value="">Seleccione médico...</option>';
        if (medicosDisponibles.length === 0) {
            select.innerHTML = '<option value="">No hay médicos disponibles para esta fecha</option>';
        } else {
            medicosDisponibles.forEach(m => {
                const option = document.createElement('option');
                option.value = m.id;
                option.textContent = `Dr. ${m.nombre} ${m.apellido} - ${m.especialidad}`;
                select.appendChild(option);
            });
            select.disabled = false;
        }
    } catch (error) {
        showToast('Error al consultar disponibilidad médica', 'error');
    }
}

function openCitaModal() {
    const form = document.getElementById('form-cita');
    form.reset();
    document.getElementById('cita-paciente-check').textContent = '';
    document.getElementById('cita-medico').innerHTML = '<option value="">(Primero seleccione una fecha)...</option>';
    document.getElementById('cita-medico').disabled = true;

    
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('cita-fecha').setAttribute('min', today);

    document.getElementById('modal-cita').classList.add('active');
    lucide.createIcons();
}

async function handleCitaSubmit(e) {
    e.preventDefault();
    
    const body = {
        cedula: document.getElementById('cita-cedula').value.trim(),
        nombre: document.getElementById('cita-nombre').value.trim(),
        apellido: document.getElementById('cita-apellido').value.trim(),
        medicoId: document.getElementById('cita-medico').value,
        fecha: document.getElementById('cita-fecha').value
    };

    try {
        const response = await fetch('/api/citas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (response.ok) {
            showToast('Cita agendada correctamente', 'success');
            document.getElementById('modal-cita').classList.remove('active');
            fetchCitas();
        } else {
            const data = await response.json();
            showToast(data.message || 'Error al agendar cita', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}

function openPosponerCitaModal(id) {
    const cita = state.citas.find(c => c.id === id);
    if (!cita) return;

    document.getElementById('posponer-cita-id').value = id;
    document.getElementById('posponer-cita-medico-name').value = `Dr. ${cita.medico.nombre} ${cita.medico.apellido} (${cita.medico.especialidad})`;
    
    
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('posponer-cita-fecha').setAttribute('min', today);
    document.getElementById('posponer-cita-fecha').value = cita.fecha;

    document.getElementById('modal-posponer-cita').classList.add('active');
    lucide.createIcons();
}

async function handlePosponerCitaSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('posponer-cita-id').value;
    const fecha = document.getElementById('posponer-cita-fecha').value;

    try {
        const response = await fetch(`/api/citas/${id}/posponer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fecha })
        });

        if (response.ok) {
            showToast('Fecha de cita reprogramada con éxito', 'success');
            document.getElementById('modal-posponer-cita').classList.remove('active');
            fetchCitas();
        } else {
            const data = await response.json();
            showToast(data.message || 'Error al reagendar cita', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}

async function cancelCita(id) {
    if (!confirm('¿Está seguro de que desea cancelar esta cita médica?')) return;
    try {
        const response = await fetch(`/api/citas/${id}`, { method: 'DELETE' });
        if (response.ok) {
            showToast('Cita cancelada con éxito', 'success');
            fetchCitas();
        } else {
            showToast('Error al cancelar cita', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}





async function fetchConsultas() {
    try {
        const res = await fetch('/api/consultas');
        state.consultas = await res.json();
        renderConsultas();
    } catch (error) {
        showToast('Error al cargar consultas', 'error');
    }
}

function renderConsultas() {
    const tbody = document.getElementById('consultas-table-body');
    tbody.innerHTML = '';

    if (state.consultas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="empty-row">No hay registros de consultas en la clínica.</td></tr>';
        return;
    }

    state.consultas.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${c.id}</td>
            <td style="font-weight:600">${c.paciente.nombre} ${c.paciente.apellido}</td>
            <td>Dr. ${c.medico.nombre} ${c.medico.apellido}</td>
            <td class="text-truncate" style="max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${c.sintomas}">${c.sintomas}</td>
            <td class="text-truncate" style="max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${c.diagnostico}">${c.diagnostico}</td>
            <td>
                ${c.enfermedadVigilada ? `<span class="status-badge warning">${c.enfermedadVigilada.nombre}</span>` : '<span class="status-badge inactive" style="background:none; border:none; color:var(--text-muted)">-</span>'}
            </td>
            <td>
                ${c.esImportante ? '<span class="status-badge active" style="background-color:rgba(239, 68, 68, 0.15); color:#fca5a5">Importante</span>' : '<span class="status-badge active" style="background-color:rgba(148, 163, 184, 0.1); color:var(--text-secondary)">No</span>'}
            </td>
            <td>
                <div class="btn-action-wrapper">
                    <button class="btn-action view" onclick="viewConsultaDetails('${c.id}')" title="Ver Consulta Completa"><i data-lucide="eye"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
    lucide.createIcons();
}

async function handleConsultaPatientSearch() {
    const cedula = document.getElementById('cons-cedula').value.trim();
    if (!cedula) return showToast('Ingrese una cédula de paciente', 'warning');

    const displayInput = document.getElementById('cons-paciente-display');
    const idInput = document.getElementById('cons-paciente-id');

    displayInput.value = 'Buscando...';

    try {
        const res = await fetch(`/api/pacientes/cedula/${cedula}`);
        if (res.ok) {
            const p = await res.json();
            displayInput.value = `${p.nombre} ${p.apellido} (${p.id})`;
            idInput.value = p.id;
        } else {
            displayInput.value = '';
            idInput.value = '';
            showToast('Paciente no registrado. Por favor créelo primero.', 'error');
        }
    } catch (e) {
        displayInput.value = '';
    }
}

async function openConsultaModal() {
    const form = document.getElementById('form-consulta');
    form.reset();
    document.getElementById('cons-paciente-id').value = '';

    
    const medSelect = document.getElementById('cons-medico');
    medSelect.innerHTML = '<option value="">Cargando médicos...</option>';
    const mRes = await fetch('/api/medicos');
    const medicos = await mRes.json();
    medSelect.innerHTML = '<option value="">Seleccione médico...</option>';
    medicos.forEach(m => {
        const option = document.createElement('option');
        option.value = m.id;
        option.textContent = `Dr. ${m.nombre} ${m.apellido} - ${m.especialidad}`;
        medSelect.appendChild(option);
    });

    
    const enfSelect = document.getElementById('cons-enfermedad');
    enfSelect.innerHTML = '<option value="">Ninguno / Sin reportar</option>';
    const eRes = await fetch('/api/enfermedades');
    const enfermedades = await eRes.json();
    enfermedades.forEach(e => {
        const option = document.createElement('option');
        option.value = e.id;
        option.textContent = `${e.nombre} (${e.gravedad})`;
        enfSelect.appendChild(option);
    });

    document.getElementById('modal-consulta').classList.add('active');
    lucide.createIcons();
}

async function handleConsultaSubmit(e) {
    e.preventDefault();
    const pacienteId = document.getElementById('cons-paciente-id').value;
    
    if (!pacienteId) {
        return showToast('Por favor busque y valide la cédula del paciente primero', 'warning');
    }

    const body = {
        pacienteId: pacienteId,
        medicoId: document.getElementById('cons-medico').value,
        sintomas: document.getElementById('cons-sintomas').value.trim(),
        diagnostico: document.getElementById('cons-diagnostico').value.trim(),
        enfermedadId: document.getElementById('cons-enfermedad').value,
        esImportante: document.getElementById('cons-importante').checked
    };

    try {
        const response = await fetch('/api/consultas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (response.ok) {
            showToast('Consulta guardada en el expediente con éxito', 'success');
            document.getElementById('modal-consulta').classList.remove('active');
            fetchConsultas();
        } else {
            const data = await response.json();
            showToast(data.message || 'Error al guardar consulta', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}

function viewConsultaDetails(id) {
    const c = state.consultas.find(item => item.id === id);
    if (!c) return;

    
    
    viewPacienteDetails(c.paciente.id);
}





async function fetchMedicos() {
    try {
        const res = await fetch('/api/medicos');
        state.medicos = await res.json();
        renderMedicos();
    } catch (error) {
        showToast('Error al cargar médicos', 'error');
    }
}

function renderMedicos() {
    const tbody = document.getElementById('medicos-table-body');
    tbody.innerHTML = '';

    if (state.medicos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="empty-row">No hay médicos registrados.</td></tr>';
        return;
    }

    state.medicos.forEach(m => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${m.id}</td>
            <td>${m.cedula}</td>
            <td style="font-weight:600">Dr. ${m.nombre} ${m.apellido}</td>
            <td>${m.edad} años</td>
            <td>${m.sexo}</td>
            <td><span class="status-badge active" style="background-color:var(--primary-light); color:#a5b4fc">${m.especialidad}</span></td>
            <td style="padding-left:35px">${m.maxCitas}</td>
            <td>
                <div class="btn-action-wrapper">
                    <button class="btn-action edit" onclick="openMedicoModal('${m.id}')" title="Editar"><i data-lucide="edit-3"></i></button>
                    <button class="btn-action delete" onclick="deleteMedico('${m.id}')" title="Eliminar"><i data-lucide="trash-2"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
    lucide.createIcons();
}

function openMedicoModal(id = null) {
    const form = document.getElementById('form-medico');
    form.reset();
    document.getElementById('medico-id').value = id || '';
    document.getElementById('modal-medico-title').textContent = id ? 'Editar Médico' : 'Nuevo Médico';
    
    const notice = document.getElementById('medico-user-notice');
    if (id) {
        notice.classList.add('hide');
        const m = state.medicos.find(item => item.id === id);
        if (m) {
            document.getElementById('med-cedula').value = m.cedula;
            document.getElementById('med-nombre').value = m.nombre;
            document.getElementById('med-apellido').value = m.apellido;
            document.getElementById('med-edad').value = m.edad;
            document.getElementById('med-sexo').value = m.sexo;
            document.getElementById('med-especialidad').value = m.especialidad;
            document.getElementById('med-maxcitas').value = m.maxCitas;
        }
    } else {
        notice.classList.remove('hide');
    }

    document.getElementById('modal-medico').classList.add('active');
    lucide.createIcons();
}

async function handleMedicoSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('medico-id').value;

    const body = {
        cedula: document.getElementById('med-cedula').value.trim(),
        nombre: document.getElementById('med-nombre').value.trim(),
        apellido: document.getElementById('med-apellido').value.trim(),
        edad: parseInt(document.getElementById('med-edad').value),
        sexo: document.getElementById('med-sexo').value,
        especialidad: document.getElementById('med-especialidad').value.trim(),
        maxCitas: parseInt(document.getElementById('med-maxcitas').value)
    };

    const url = id ? `/api/medicos/${id}` : '/api/medicos';
    const method = id ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (response.ok) {
            const data = await response.json();
            
            if (method === 'POST') {
                alert(`Médico y usuario creados exitosamente.\n\nUsuario asignado: ${data.usernameCreado}\nClave temporal: ${data.claveTemporal}\n\nPor favor anote estas credenciales.`);
            } else {
                showToast('Médico actualizado con éxito', 'success');
            }

            document.getElementById('modal-medico').classList.remove('active');
            fetchMedicos();
        } else {
            const data = await response.json();
            showToast(data.message || 'Error al guardar médico', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}

async function deleteMedico(id) {
    if (!confirm('¿Está seguro de que desea eliminar este médico? Su cuenta de usuario y registros internos serán dados de baja.')) return;
    try {
        const response = await fetch(`/api/medicos/${id}`, { method: 'DELETE' });
        if (response.ok) {
            showToast('Médico eliminado con éxito', 'success');
            fetchMedicos();
        } else {
            showToast('Error al eliminar médico', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}





async function fetchVacunas() {
    try {
        const res = await fetch('/api/vacunas');
        state.vacunas = await res.json();
        renderVacunas();
    } catch (error) {
        showToast('Error al cargar vacunas', 'error');
    }
}

function renderVacunas() {
    const tbody = document.getElementById('vacunas-table-body');
    tbody.innerHTML = '';

    if (state.vacunas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="empty-row">No hay vacunas en el catálogo.</td></tr>';
        return;
    }

    state.vacunas.forEach(v => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${v.id}</td>
            <td style="font-weight:600">${v.nombre}</td>
            <td>${v.fabricante}</td>
            <td>${v.dosis} ml</td>
            <td>${v.descripcion || '-'}</td>
            <td>
                <div class="btn-action-wrapper">
                    <button class="btn-action edit" onclick="openVacunaModal('${v.id}')" title="Editar"><i data-lucide="edit-3"></i></button>
                    <button class="btn-action delete" onclick="deleteVacuna('${v.id}')" title="Eliminar"><i data-lucide="trash-2"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
    lucide.createIcons();
}

function openVacunaModal(id = null) {
    const form = document.getElementById('form-vacuna');
    form.reset();
    document.getElementById('vacuna-id').value = id || '';
    document.getElementById('modal-vacuna-title').textContent = id ? 'Editar Vacuna' : 'Nueva Vacuna';

    if (id) {
        const v = state.vacunas.find(item => item.id === id);
        if (v) {
            document.getElementById('vac-nombre').value = v.nombre;
            document.getElementById('vac-fabricante').value = v.fabricante;
            document.getElementById('vac-dosis').value = v.dosis;
            document.getElementById('vac-descripcion').value = v.descripcion;
        }
    }

    document.getElementById('modal-vacuna').classList.add('active');
    lucide.createIcons();
}

async function handleVacunaSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('vacuna-id').value;

    const body = {
        nombre: document.getElementById('vac-nombre').value.trim(),
        fabricante: document.getElementById('vac-fabricante').value.trim(),
        dosis: parseFloat(document.getElementById('vac-dosis').value),
        descripcion: document.getElementById('vac-descripcion').value.trim()
    };

    const url = id ? `/api/vacunas/${id}` : '/api/vacunas';
    const method = id ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (response.ok) {
            showToast(id ? 'Vacuna actualizada con éxito' : 'Vacuna añadida al catálogo', 'success');
            document.getElementById('modal-vacuna').classList.remove('active');
            fetchVacunas();
        } else {
            showToast('Error al guardar vacuna', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}

async function deleteVacuna(id) {
    if (!confirm('¿Está seguro de que desea eliminar esta vacuna del catálogo?')) return;
    try {
        const response = await fetch(`/api/vacunas/${id}`, { method: 'DELETE' });
        if (response.ok) {
            showToast('Vacuna eliminada del catálogo', 'success');
            fetchVacunas();
        } else {
            showToast('Error al eliminar vacuna', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}





async function fetchEnfermedades() {
    try {
        const res = await fetch('/api/enfermedades');
        state.enfermedades = await res.json();
        renderEnfermedades();
    } catch (error) {
        showToast('Error al cargar enfermedades', 'error');
    }
}

function renderEnfermedades() {
    const tbody = document.getElementById('enfermedades-table-body');
    tbody.innerHTML = '';

    if (state.enfermedades.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-row">No hay patologías en vigilancia.</td></tr>';
        return;
    }

    state.enfermedades.forEach(e => {
        let gravityColor = 'success';
        if (e.gravedad.toLowerCase() === 'moderada') gravityColor = 'warning';
        if (e.gravedad.toLowerCase() === 'grave') gravityColor = 'inactive'; 

        tbody.innerHTML += `
            <tr>
                <td>${e.id}</td>
                <td style="font-weight:600">${e.nombre}</td>
                <td><span class="status-badge ${gravityColor}">${e.gravedad}</span></td>
                <td>${e.descripcion || '-'}</td>
                <td>
                    <div class="btn-action-wrapper">
                        <button class="btn-action edit" onclick="openEnfermedadModal('${e.id}')" title="Editar"><i data-lucide="edit-3"></i></button>
                        <button class="btn-action delete" onclick="deleteEnfermedad('${e.id}')" title="Eliminar"><i data-lucide="trash-2"></i></button>
                    </div>
                </td>
            </tr>
        `;
    });
    lucide.createIcons();
}

function openEnfermedadModal(id = null) {
    const form = document.getElementById('form-enfermedad');
    form.reset();
    document.getElementById('enfermedad-id').value = id || '';
    document.getElementById('modal-enfermedad-title').textContent = id ? 'Editar Patología' : 'Registrar Patología';

    if (id) {
        const e = state.enfermedades.find(item => item.id === id);
        if (e) {
            document.getElementById('enf-nombre').value = e.nombre;
            document.getElementById('enf-gravedad').value = e.gravedad;
            document.getElementById('enf-descripcion').value = e.descripcion;
        }
    }

    document.getElementById('modal-enfermedad').classList.add('active');
    lucide.createIcons();
}

async function handleEnfermedadSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('enfermedad-id').value;

    const body = {
        nombre: document.getElementById('enf-nombre').value.trim(),
        gravedad: document.getElementById('enf-gravedad').value,
        descripcion: document.getElementById('enf-descripcion').value.trim()
    };

    const url = id ? `/api/enfermedades/${id}` : '/api/enfermedades';
    const method = id ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (response.ok) {
            showToast(id ? 'Patología actualizada con éxito' : 'Nueva patología en vigilancia registrada', 'success');
            document.getElementById('modal-enfermedad').classList.remove('active');
            fetchEnfermedades();
        } else {
            showToast('Error al guardar patología', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}

async function deleteEnfermedad(id) {
    if (!confirm('¿Está seguro de que desea retirar esta patología del monitoreo de vigilancia epidemiológica?')) return;
    try {
        const response = await fetch(`/api/enfermedades/${id}`, { method: 'DELETE' });
        if (response.ok) {
            showToast('Patología retirada del monitoreo', 'success');
            fetchEnfermedades();
        } else {
            showToast('Error al retirar patología', 'error');
        }
    } catch (error) {
        showToast('Error de red', 'error');
    }
}





async function fetchRespaldos() {
    try {
        const res = await fetch('/api/database/backups');
        if (!res.ok) return;
        state.respaldos = await res.json();
        renderRespaldos();
    } catch (error) {
        showToast('Error al cargar respaldos de BD', 'error');
    }
}

function renderRespaldos() {
    const tbody = document.getElementById('respaldos-table-body');
    tbody.innerHTML = '';

    if (state.respaldos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-row">No hay archivos de respaldo generados en la carpeta /backups.</td></tr>';
        return;
    }

    state.respaldos.forEach(b => {
        const dateStr = new Date(b.lastModified).toLocaleString('es-ES');
        const sizeKb = (b.size / 1024).toFixed(2);
        
        tbody.innerHTML += `
            <tr>
                <td style="font-family: monospace; font-weight: 600">${b.filename}</td>
                <td>${sizeKb} KB</td>
                <td>${dateStr}</td>
                <td>
                    <div class="btn-action-wrapper">
                        <button class="btn btn-outline" style="padding: 6px 12px; font-size:11px" onclick="handleRestoreDatabase('${b.filename}')" title="Restaurar Base de Datos">
                            <i data-lucide="rotate-ccw" style="width:12px; height:12px"></i>
                            <span>Restaurar</span>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    });
    lucide.createIcons();
}

async function handleCreateBackup() {
    showToast('Generando respaldo SQL de base de datos...', 'info');
    try {
        const res = await fetch('/api/database/backup', { method: 'POST' });
        if (res.ok) {
            const data = await res.json();
            showToast(data.message, 'success');
            fetchRespaldos();
        } else {
            showToast('Error al generar respaldo de BD', 'error');
        }
    } catch (e) {
        showToast('Error de red', 'error');
    }
}

async function handleRestoreDatabase(filename) {
    if (!confirm(`¡CUIDADO! Está a punto de restaurar la base de datos completa a partir del archivo '${filename}'.\n\nEsta operación reemplazará todas las tablas, pacientes, médicos, citas y consultas actuales con los datos de este respaldo. ¿Desea proceder?`)) {
        return;
    }

    showToast('Restaurando base de datos...', 'info');
    try {
        const res = await fetch('/api/database/restore', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ filename })
        });

        if (res.ok) {
            const data = await res.json();
            alert(data.message + '\n\nSe recargarán los datos generales.');
            
            showView('dashboard');
        } else {
            const data = await res.json();
            showToast(data.message || 'Error al restaurar base de datos', 'error');
        }
    } catch (e) {
        showToast('Error de red', 'error');
    }
}





function setupSearchFilter(inputId, tbodyId, filterFn) {
    const input = document.getElementById(inputId);
    input.addEventListener('keyup', () => {
        const query = input.value.trim().toLowerCase();
        const tbody = document.getElementById(tbodyId);
        const rows = tbody.getElementsByTagName('tr');

        for (let row of rows) {
            
            if (row.cells.length === 1 && row.cells[0].classList.contains('empty-row')) continue;
            
            const isMatch = filterFn(row, query);
            row.style.display = isMatch ? '' : 'none';
        }
    });
}


function filterPacienteRow(row, query) {
    const cedula = row.cells[1].textContent.toLowerCase();
    const nombre = row.cells[2].textContent.toLowerCase();
    const tel = row.cells[5].textContent.toLowerCase();
    return cedula.includes(query) || nombre.includes(query) || tel.includes(query);
}

function filterCitaRow(row, query) {
    const cedula = row.cells[1].textContent.toLowerCase();
    const paciente = row.cells[2].textContent.toLowerCase();
    const medico = row.cells[3].textContent.toLowerCase();
    return cedula.includes(query) || paciente.includes(query) || medico.includes(query);
}

function filterConsultaRow(row, query) {
    const paciente = row.cells[1].textContent.toLowerCase();
    const medico = row.cells[2].textContent.toLowerCase();
    const sintomas = row.cells[3].textContent.toLowerCase();
    const diag = row.cells[4].textContent.toLowerCase();
    return paciente.includes(query) || medico.includes(query) || sintomas.includes(query) || diag.includes(query);
}

function filterMedicoRow(row, query) {
    const cedula = row.cells[1].textContent.toLowerCase();
    const nombre = row.cells[2].textContent.toLowerCase();
    const esp = row.cells[5].textContent.toLowerCase();
    return cedula.includes(query) || nombre.includes(query) || esp.includes(query);
}

function filterCatalogRow(row, query) {
    const name = row.cells[1].textContent.toLowerCase();
    const desc = row.cells[3].textContent.toLowerCase();
    return name.includes(query) || desc.includes(query);
}


function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const iconsMap = {
        'success': 'check-circle',
        'error': 'x-circle',
        'info': 'info',
        'warning': 'alert-triangle'
    };
    
    toast.innerHTML = `
        <i data-lucide="${iconsMap[type] || 'info'}"></i>
        <span>${message}</span>
    `;
    
    container.appendChild(toast);
    lucide.createIcons();

    
    setTimeout(() => {
        toast.style.animation = 'toastIn 0.3s cubic-bezier(0.4, 0, 0.2, 1) reverse forwards';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}
