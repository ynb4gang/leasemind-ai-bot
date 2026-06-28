const state = {
  me: null,
  analytics: null,
  insights: null,
  tables: []
};

const loginView = document.getElementById('loginView');
const appView = document.getElementById('appView');
const loginError = document.getElementById('loginError');
const userName = document.getElementById('userName');
const userRoles = document.getElementById('userRoles');
const profileAvatar = document.getElementById('profileAvatar');

async function api(url, options = {}) {
  const res = await fetch(url, {
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || 'Request failed');
  return data;
}

function showLoggedOut() {
  loginView.classList.remove('hidden');
  appView.classList.add('hidden');
}

function showLoggedIn(me) {
  state.me = me;
  userName.textContent = me.displayName || me.username;
  userRoles.textContent = (me.roles || []).join(', ');
  profileAvatar.textContent = (me.displayName || me.username || 'U').slice(0, 1).toUpperCase();

  document.querySelectorAll('.admin-only').forEach(el => {
    el.style.display = me.roles.includes('ADMIN') ? '' : 'none';
  });

  loginView.classList.add('hidden');
  appView.classList.remove('hidden');
}

function activateTab(name) {
  document.querySelectorAll('.tab').forEach(btn => btn.classList.toggle('active', btn.dataset.tab === name));
  document.querySelectorAll('.view').forEach(view => view.classList.remove('active'));
  document.getElementById(`${name}Tab`).classList.add('active');
}

document.querySelectorAll('.tab').forEach(btn => btn.addEventListener('click', () => activateTab(btn.dataset.tab)));

async function bootstrap() {
  try {
    const me = await api('/dashboard/api/auth/me');
    showLoggedIn(me);
    await loadAll();
    if (me.roles.includes('ADMIN')) await loadUsers();
  } catch {
    showLoggedOut();
  }
}

async function loadAll() {
  await Promise.all([
    loadAnalytics(),
    loadInsights(),
    loadTables()
  ]);
}

document.getElementById('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  loginError.textContent = '';
  const form = new FormData(e.target);

  try {
    const me = await api('/dashboard/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        username: form.get('username'),
        password: form.get('password')
      })
    });
    showLoggedIn(me);
    await loadAll();
    if (me.roles.includes('ADMIN')) await loadUsers();
  } catch (err) {
    loginError.textContent = err.message;
  }
});

document.getElementById('logoutBtn').addEventListener('click', async () => {
  try {
    await api('/dashboard/api/auth/logout', { method: 'POST' });
  } finally {
    showLoggedOut();
  }
});

document.getElementById('refreshOverviewBtn').addEventListener('click', loadAnalytics);
document.getElementById('refreshInsightsBtn').addEventListener('click', loadInsights);

async function loadAnalytics() {
  const data = await api('/dashboard/api/analytics/summary');
  state.analytics = data;

  const kpis = data.kpis || {};
  setText('kpiTotalMessages', formatNumber(kpis.totalMessages));
  setText('kpiTotalUsers', formatNumber(kpis.totalUsers));
  setText('kpiTotalChats', formatNumber(kpis.totalChats));
  setText('kpiMessages24h', formatNumber(kpis.messages24h));
  setText('kpiMessages7d', formatNumber(kpis.messages7d));
  setText('kpiAnswerCoverage', `${formatNumber(kpis.answerCoverage)}%`);
  setText('kpiPeakHour', kpis.peakHour || '—');
  setText('kpiAvgQuestionLength', formatNumber(kpis.avgQuestionLength));
  setText('kpiAvgAnswerLength', formatNumber(kpis.avgAnswerLength));
  setText('kpiLatestActivity', kpis.latestActivity || '—');

  renderBarChart(document.getElementById('dailyTrendChart'), data.dailyTrend || []);
  renderStackList(document.getElementById('channelBreakdown'), data.channelBreakdown || [], 'channel');
  renderStackList(document.getElementById('topUsersBox'), data.topUsers || [], 'username');
  renderStackList(document.getElementById('repeatedTopicsBox'), data.repeatedTopics || [], 'topic');
  renderSimpleInsights(document.getElementById('heuristicInsights'), data.heuristicInsights || []);
  renderSimpleInsights(document.getElementById('qualityFlags'), data.qualityFlags || []);
  renderRecentQa(document.getElementById('recentQa'), data.recentMessages || []);
}

document.getElementById('askAnalyticsBtn').addEventListener('click', async () => {
  const question = document.getElementById('analyticsQuestion').value.trim();
  if (!question) return;

  const data = await api('/dashboard/api/analytics/ask', {
    method: 'POST',
    body: JSON.stringify({ question, locale: 'ru' })
  });

  document.getElementById('analyticsAnswer').textContent = data.answer || '';
  await loadAnalytics();
});

async function loadInsights() {
  const data = await api('/dashboard/api/insights');
  state.insights = data;

  document.getElementById('insightsBox').textContent = data.insights || '';
  document.getElementById('qualitySummaryBox').textContent = data.qualitySummary || '';
  document.getElementById('insightSampleSize').textContent = `sample: ${data.sampleSize || 0}`;

  const snapshot = data.analyticsSnapshot?.kpis || {};
  const snapshotEntries = [
    `Всего диалогов: ${formatNumber(snapshot.totalMessages)}`,
    `Уникальных пользователей: ${formatNumber(snapshot.totalUsers)}`,
    `Уникальных чатов: ${formatNumber(snapshot.totalChats)}`,
    `Каналов: ${formatNumber(snapshot.totalChannels)}`,
    `24ч: ${formatNumber(snapshot.messages24h)}`,
    `7д: ${formatNumber(snapshot.messages7d)}`,
    `30д: ${formatNumber(snapshot.messages30d)}`,
    `Покрытие ответами: ${formatNumber(snapshot.answerCoverage)}%`,
    `Пиковый час: ${snapshot.peakHour || '—'}`
  ];

  renderSimpleInsights(document.getElementById('insightsSnapshot'), snapshotEntries);
}

document.getElementById('runSqlBtn').addEventListener('click', async () => {
  const sql = document.getElementById('sqlText').value;
  const limit = Number(document.getElementById('sqlLimit').value || 100);

  const data = await api('/dashboard/api/sql/query', {
    method: 'POST',
    body: JSON.stringify({ sql, limit })
  });

  document.getElementById('sqlMeta').textContent = `rows: ${data.rowCount}, limit: ${data.limit}`;
  renderTable(document.getElementById('sqlResult'), data.columns, data.rows);
});

async function loadTables() {
  const data = await api('/dashboard/api/sql/tables');
  state.tables = data || [];
  const select = document.getElementById('tableSelect');
  select.innerHTML = state.tables
    .map(t => `<option value="${t.table_schema}.${t.table_name}">${t.table_schema}.${t.table_name}</option>`)
    .join('');
}

function selectedTable() {
  const value = document.getElementById('tableSelect').value;
  const [schema, table] = value.split('.');
  return { schema, table };
}

document.getElementById('loadColumnsBtn').addEventListener('click', async () => {
  const { schema, table } = selectedTable();
  const data = await api(`/dashboard/api/sql/columns?schema=${encodeURIComponent(schema)}&table=${encodeURIComponent(table)}`);

  document.getElementById('columnsBox').innerHTML = data.map(c => `
    <div class="stack-item">
      <div class="stack-main">${escapeHtml(c.column_name)}</div>
      <div class="stack-sub">${escapeHtml(c.data_type)} · nullable=${escapeHtml(c.is_nullable)}</div>
    </div>
  `).join('');
});

document.getElementById('previewBtn').addEventListener('click', async () => {
  const { schema, table } = selectedTable();
  const data = await api(`/dashboard/api/sql/preview?schema=${encodeURIComponent(schema)}&table=${encodeURIComponent(table)}&limit=25`);
  renderTable(document.getElementById('previewBox'), (data.columns || []).map(c => c.column_name), data.rows || []);
});

async function loadUsers() {
  const users = await api('/dashboard/api/admin/users');
  const box = document.getElementById('usersBox');
  box.innerHTML = '';

  users.forEach(user => {
    const div = document.createElement('div');
    div.className = 'timeline-item';
    div.innerHTML = `
      <div class="timeline-head">
        <div class="timeline-title">${escapeHtml(user.username)}</div>
        <div class="timeline-meta">${escapeHtml((user.roles || []).join(', '))}</div>
      </div>
      <div class="timeline-body">${escapeHtml(user.displayName || '')}</div>
    `;
    box.appendChild(div);
  });
}

document.getElementById('createUserForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const form = new FormData(e.target);
  const roles = form.getAll('roles');

  const data = await api('/dashboard/api/admin/users', {
    method: 'POST',
    body: JSON.stringify({
      username: form.get('username'),
      displayName: form.get('displayName'),
      password: form.get('password'),
      roles
    })
  });

  document.getElementById('adminMessage').textContent = `Пользователь ${data.username} создан`;
  e.target.reset();
  await loadUsers();
});

function renderBarChart(container, items) {
  if (!items.length) {
    container.innerHTML = `<div class="muted">Нет данных</div>`;
    return;
  }

  const max = Math.max(...items.map(x => Number(x.value || 0)), 1);

  container.innerHTML = items.map(item => {
    const value = Number(item.value || 0);
    const percent = Math.max(8, Math.round((value / max) * 100));
    return `
      <div class="bar-row">
        <div class="bar-label">${escapeHtml(item.label)}</div>
        <div class="bar-track">
          <div class="bar-fill" style="width:${percent}%"></div>
        </div>
        <div class="bar-value">${escapeHtml(value)}</div>
      </div>
    `;
  }).join('');
}

function renderStackList(container, items, keyField) {
  if (!items.length) {
    container.innerHTML = `<div class="muted">Нет данных</div>`;
    return;
  }

  const max = Math.max(...items.map(x => Number(x.value || 0)), 1);

  container.innerHTML = items.map(item => {
    const value = Number(item.value || 0);
    const percent = Math.max(6, Math.round((value / max) * 100));
    return `
      <div class="stack-item">
        <div class="stack-head">
          <div class="stack-main">${escapeHtml(item[keyField])}</div>
          <div class="stack-value">${escapeHtml(value)}</div>
        </div>
        <div class="mini-track">
          <div class="mini-fill" style="width:${percent}%"></div>
        </div>
      </div>
    `;
  }).join('');
}

function renderSimpleInsights(container, items) {
  if (!items.length) {
    container.innerHTML = `<div class="muted">Нет данных</div>`;
    return;
  }

  container.innerHTML = items.map(item => `
    <div class="insight-item">
      <div class="insight-dot"></div>
      <div>${escapeHtml(item)}</div>
    </div>
  `).join('');
}

function renderRecentQa(container, items) {
  if (!items.length) {
    container.innerHTML = `<div class="muted">Записей пока нет</div>`;
    return;
  }

  container.innerHTML = items.map(item => `
    <div class="timeline-item">
      <div class="timeline-head">
        <div class="timeline-title">${escapeHtml(item.channel || 'unknown')}</div>
        <div class="timeline-meta">${escapeHtml(item.username || '-')}</div>
      </div>
      <div class="timeline-body"><b>Q:</b> ${escapeHtml(item.sanitized_question || item.question_text || '')}</div>
      <div class="timeline-body"><b>A:</b> ${escapeHtml(item.sanitized_answer || item.answer_text || '')}</div>
    </div>
  `).join('');
}

function renderTable(container, columns, rows) {
  if (!columns?.length) {
    container.innerHTML = '<div class="muted">Нет столбцов</div>';
    return;
  }

  const thead = `<thead><tr>${columns.map(c => `<th>${escapeHtml(c)}</th>`).join('')}</tr></thead>`;
  const tbody = rows.length
    ? `<tbody>${rows.map(r => `<tr>${columns.map(c => `<td>${escapeHtml(formatValue(r[c]))}</td>`).join('')}</tr>`).join('')}</tbody>`
    : '<tbody><tr><td colspan="999">Нет данных</td></tr></tbody>';

  container.innerHTML = `<table>${thead}${tbody}</table>`;
}

function setText(id, value) {
  document.getElementById(id).textContent = value ?? '';
}

function formatNumber(v) {
  if (v === null || v === undefined || v === '') return '0';
  return String(v);
}

function formatValue(v) {
  if (v === null || v === undefined) return '';
  if (typeof v === 'object') return JSON.stringify(v);
  return String(v);
}

function escapeHtml(str) {
  return String(str)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

bootstrap();