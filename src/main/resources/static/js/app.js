import { api, requireAuth, getCurrentUser, removeToken, showToast, setLoading, openModal, closeModal } from '/js/api.js';

// ── State ─────────────────────────────────────────────────────────────────────
let allStudents  = [];
let allDepts     = [];
let currentUser  = null;
let isAdmin      = false;
let deleteTargetId = null;

// ── Bootstrap ─────────────────────────────────────────────────────────────────
if (!requireAuth()) throw new Error('Not authenticated');

currentUser = getCurrentUser();
isAdmin = currentUser?.role === 'ADMIN';

initUserUI();
await Promise.all([loadStudents(), loadDepartments()]);
hideLoader();

// ── User Info UI ──────────────────────────────────────────────────────────────
function initUserUI() {
  const email  = currentUser?.email || 'Unknown';
  const role   = currentUser?.role  || '—';
  const letter = email[0]?.toUpperCase() || 'U';

  document.getElementById('user-avatar').textContent = letter;
  document.getElementById('user-email').textContent  = email;
  document.getElementById('user-role').textContent   = role;

  const roleBadge = document.getElementById('role-badge');
  roleBadge.textContent  = role;
  roleBadge.className    = `badge ${isAdmin ? 'badge-admin' : 'badge-student'}`;

  // Show admin-only action buttons
  if (isAdmin) {
    document.getElementById('btn-add-student').style.display = 'inline-flex';
  }
}

// ── Section Switching ─────────────────────────────────────────────────────────
window.switchSection = function(name) {
  // Update nav items
  document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
  document.getElementById(`nav-${name}s`).classList.add('active');

  // Update sections
  document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
  document.getElementById(`section-${name}s`).classList.add('active');

  // Update top-bar title
  const titles = {
    students:    ['Students',    'Manage all student records'],
    departments: ['Departments', 'View and manage academic departments'],
  };
  const [title, sub] = titles[name] || ['Dashboard', ''];
  document.getElementById('page-title').textContent    = title;
  document.getElementById('page-subtitle').textContent = sub;

  // Show correct action button
  document.getElementById('btn-add-student').style.display = (name === 'students' && isAdmin) ? 'inline-flex' : 'none';
  document.getElementById('btn-add-dept').style.display    = (name === 'departments' && isAdmin) ? 'inline-flex' : 'none';
};

// ── Loader ────────────────────────────────────────────────────────────────────
function hideLoader() {
  const overlay = document.getElementById('loading-overlay');
  overlay.classList.add('hidden');
  setTimeout(() => overlay.style.display = 'none', 400);
}

// ── Logout ────────────────────────────────────────────────────────────────────
window.logout = function() {
  removeToken();
  window.location.href = '/index.html';
};

// ════════════════════════════════════════════════════════════════════════════
// STUDENTS
// ════════════════════════════════════════════════════════════════════════════

async function loadStudents() {
  try {
    allStudents = await api.get('/api/v1/students');
    renderStudents(allStudents);
    updateStudentStats(allStudents);
    document.getElementById('nav-student-count').textContent = allStudents.length;
  } catch (err) {
    document.getElementById('students-tbody').innerHTML = `
      <tr><td colspan="7">
        <div class="empty-state">
          <p>Failed to load students: ${err.message}</p>
        </div>
      </td></tr>`;
  }
}

function updateStudentStats(students) {
  document.getElementById('stat-total-students').textContent = students.length;
  const courses   = new Set(students.map(s => s.course).filter(Boolean));
  const countries = new Set(students.map(s => s.address?.country).filter(Boolean));
  document.getElementById('stat-courses').textContent   = courses.size || '—';
  document.getElementById('stat-countries').textContent = countries.size || '—';
}

function renderStudents(students) {
  const tbody = document.getElementById('students-tbody');
  if (!students.length) {
    tbody.innerHTML = `
      <tr><td colspan="7">
        <div class="empty-state">
          <p>No students found. ${isAdmin ? 'Click <b>+ Add Student</b> to create one.' : ''}</p>
        </div>
      </td></tr>`;
    return;
  }

  tbody.innerHTML = students.map(s => {
    const dept     = s.department?.name || '—';
    const location = s.address
      ? [s.address.city, s.address.state, s.address.country].filter(Boolean).join(', ')
      : '—';
    const actionBtns = isAdmin
      ? `<button class="btn btn-outline btn-sm" title="Edit"   onclick="openEditModal(${s.id})">Edit</button>
         <button class="btn btn-danger  btn-sm" title="Delete" onclick="openDeleteModal(${s.id}, '${escHtml(s.name)}')">Delete</button>`
      : `<button class="btn btn-outline btn-sm" title="View"   onclick="openEditModal(${s.id})">View</button>`;

    return `
      <tr>
        <td><span style="color:var(--text-muted);font-size:12px;">#${s.id}</span></td>
        <td class="td-primary">${escHtml(s.name)}</td>
        <td><a href="mailto:${escHtml(s.email)}" style="color:var(--accent-light)">${escHtml(s.email)}</a></td>
        <td><span class="badge badge-student">${escHtml(s.course || '—')}</span></td>
        <td><span class="badge badge-dept">${escHtml(dept)}</span></td>
        <td style="max-width:180px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"
            title="${escHtml(location)}">${escHtml(location)}</td>
        <td><div class="td-actions">${actionBtns}</div></td>
      </tr>`;
  }).join('');
}

// Search / Filter
window.filterStudents = function(query) {
  const q = query.toLowerCase();
  const filtered = allStudents.filter(s =>
    s.name?.toLowerCase().includes(q) ||
    s.email?.toLowerCase().includes(q) ||
    s.course?.toLowerCase().includes(q) ||
    s.department?.name?.toLowerCase().includes(q)
  );
  renderStudents(filtered);
};

// ── Add Student Modal ─────────────────────────────────────────────────────────
window.openStudentModal = function() {
  document.getElementById('student-modal-title').textContent = 'Add Student';
  document.getElementById('edit-student-id').value = '';
  document.getElementById('student-form').reset();
  document.getElementById('student-form-error').style.display = 'none';
  document.getElementById('save-student-btn').querySelector('.btn-text').textContent = 'Save Student';
  populateDeptSelect('s-dept');
  openModal('student-modal-overlay');
};

// ── Edit Student Modal ────────────────────────────────────────────────────────
window.openEditModal = async function(id) {
  document.getElementById('student-form-error').style.display = 'none';
  populateDeptSelect('s-dept');

  // Find student from cache
  const s = allStudents.find(x => x.id === id);
  if (!s) return;

  document.getElementById('student-modal-title').textContent = isAdmin ? 'Edit Student' : 'View Student';
  document.getElementById('edit-student-id').value = s.id;
  document.getElementById('s-name').value    = s.name    || '';
  document.getElementById('s-email').value   = s.email   || '';
  document.getElementById('s-course').value  = s.course  || '';
  document.getElementById('s-dept').value    = s.department?.id || '';
  document.getElementById('s-city').value    = s.address?.city    || '';
  document.getElementById('s-state').value   = s.address?.state   || '';
  document.getElementById('s-country').value = s.address?.country || '';

  // Make read-only if not admin
  const fields = ['s-name','s-email','s-course','s-dept','s-city','s-state','s-country'];
  fields.forEach(id => document.getElementById(id).disabled = !isAdmin);

  document.getElementById('save-student-btn').style.display = isAdmin ? 'inline-flex' : 'none';
  document.getElementById('save-student-btn').querySelector('.btn-text').textContent = 'Update Student';
  openModal('student-modal-overlay');
};

window.closeStudentModal = function() {
  closeModal('student-modal-overlay');
  // Re-enable all fields for next open
  ['s-name','s-email','s-course','s-dept','s-city','s-state','s-country']
    .forEach(id => document.getElementById(id).disabled = false);
  document.getElementById('save-student-btn').style.display = 'inline-flex';
};

// ── Save Student (create or update) ──────────────────────────────────────────
window.saveStudent = async function() {
  const btn = document.getElementById('save-student-btn');
  const errDiv = document.getElementById('student-form-error');
  errDiv.style.display = 'none';

  const id         = document.getElementById('edit-student-id').value;
  const name       = document.getElementById('s-name').value.trim();
  const email      = document.getElementById('s-email').value.trim();
  const course     = document.getElementById('s-course').value.trim();
  const deptId     = document.getElementById('s-dept').value;
  const city       = document.getElementById('s-city').value.trim();
  const state      = document.getElementById('s-state').value.trim();
  const country    = document.getElementById('s-country').value.trim();

  if (!name || !email || !course || !deptId) {
    showFormError(errDiv, 'Name, email, course and department are required.');
    return;
  }

  const body = {
    name, email, course,
    departmentId: parseInt(deptId),
    addressRequestDto: city ? { city, state, country } : null,
  };

  setLoading(btn, true);
  try {
    if (id) {
      await api.put(`/api/v1/students/${id}`, body);
      showToast('Student updated successfully!', 'success');
    } else {
      await api.post('/api/v1/students', body);
      showToast('Student created successfully!', 'success');
    }
    closeStudentModal();
    await loadStudents();
  } catch (err) {
    showFormError(errDiv, err.message || 'Operation failed.');
  } finally {
    setLoading(btn, false);
  }
};

// ── Delete Student ─────────────────────────────────────────────────────────────
window.openDeleteModal = function(id, name) {
  deleteTargetId = id;
  document.getElementById('delete-student-name').textContent = name;
  openModal('delete-modal-overlay');
};

window.closeDeleteModal = function() {
  deleteTargetId = null;
  closeModal('delete-modal-overlay');
};

window.confirmDelete = async function() {
  if (!deleteTargetId) return;
  const btn = document.getElementById('confirm-delete-btn');
  setLoading(btn, true);
  try {
    await api.delete(`/api/v1/students/${deleteTargetId}`);
    showToast('Student deleted.', 'success');
    closeDeleteModal();
    await loadStudents();
  } catch (err) {
    showToast(err.message || 'Delete failed.', 'error');
    closeDeleteModal();
  } finally {
    setLoading(btn, false);
  }
};

// ════════════════════════════════════════════════════════════════════════════
// DEPARTMENTS
// ════════════════════════════════════════════════════════════════════════════

async function loadDepartments() {
  try {
    allDepts = await api.get('/api/departments');
    renderDepartments(allDepts);
    document.getElementById('nav-dept-count').textContent    = allDepts.length;
    document.getElementById('stat-total-depts').textContent  = allDepts.length;
    document.getElementById('stat-dept-count2').textContent  = allDepts.length;
  } catch (err) {
    document.getElementById('dept-grid').innerHTML =
      `<p style="color:var(--text-muted);padding:24px 22px;">Failed to load departments.</p>`;
  }
}

function renderDepartments(depts) {
  const grid = document.getElementById('dept-grid');

  const cards = depts.map((d) => `
    <div class="dept-card">
      <div class="dept-card-name">${escHtml(d.name)}</div>
      <div class="dept-card-id">ID #${d.id}</div>
    </div>`).join('');

  const addCard = isAdmin ? `
    <div class="dept-add-card" onclick="openDeptModal()">
      <div class="dept-add-text">+ Add Department</div>
    </div>` : '';

  grid.innerHTML = cards + addCard;

  if (!depts.length && !isAdmin) {
    grid.innerHTML = `<p style="color:var(--text-muted);padding:24px 22px;">No departments found.</p>`;
  }
}

function populateDeptSelect(selectId) {
  const select = document.getElementById(selectId);
  const current = select.value;
  select.innerHTML = '<option value="">Select department…</option>';
  allDepts.forEach(d => {
    const opt = document.createElement('option');
    opt.value = d.id;
    opt.textContent = d.name;
    if (d.id == current) opt.selected = true;
    select.appendChild(opt);
  });
}

// ── Add Department Modal ──────────────────────────────────────────────────────
window.openDeptModal = function() {
  document.getElementById('dept-name').value = '';
  document.getElementById('dept-form-error').style.display = 'none';
  openModal('dept-modal-overlay');
};

window.closeDeptModal = function() {
  closeModal('dept-modal-overlay');
};

window.saveDepartment = async function() {
  const btn     = document.getElementById('save-dept-btn');
  const errDiv  = document.getElementById('dept-form-error');
  const name    = document.getElementById('dept-name').value.trim();
  errDiv.style.display = 'none';

  if (!name) {
    showFormError(errDiv, 'Department name is required.');
    return;
  }

  setLoading(btn, true);
  try {
    await api.post('/api/departments', { name });
    showToast(`Department "${name}" created!`, 'success');
    closeDeptModal();
    await loadDepartments();
    populateDeptSelect('s-dept'); // refresh student form too
  } catch (err) {
    showFormError(errDiv, err.message || 'Failed to create department.');
  } finally {
    setLoading(btn, false);
  }
};

// ── Close modals on overlay click ─────────────────────────────────────────────
['student-modal-overlay', 'dept-modal-overlay', 'delete-modal-overlay'].forEach(id => {
  document.getElementById(id)?.addEventListener('click', function(e) {
    if (e.target === this) {
      if (id === 'student-modal-overlay') closeStudentModal();
      else if (id === 'dept-modal-overlay') closeDeptModal();
      else closeDeleteModal();
    }
  });
});

// ── Keyboard: Escape closes modals ────────────────────────────────────────────
document.addEventListener('keydown', e => {
  if (e.key !== 'Escape') return;
  closeStudentModal();
  closeDeptModal();
  closeDeleteModal();
});

// ── Utility ───────────────────────────────────────────────────────────────────
function escHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g,'&amp;').replace(/</g,'&lt;')
    .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function showFormError(errDiv, msg) {
  errDiv.querySelector('span:last-child').textContent = msg;
  errDiv.style.display = 'flex';
}
