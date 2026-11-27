import React, { useCallback, useEffect, useMemo, useState } from 'react';

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const EMPTY_STUDENT = { name: '', email: '', department: 'CSE', cgpa: '', password: '' };
const EMPTY_JOB = { company: '', title: '', requiredSkills: '', minimumCgpa: '6', location: '' };
const STATUSES = ['APPLIED', 'ASSESSMENT', 'INTERVIEW', 'SELECTED', 'REJECTED'];

function readSession() {
  try { return JSON.parse(sessionStorage.getItem('placementAuth') || 'null'); }
  catch { sessionStorage.removeItem('placementAuth'); return null; }
}

export default function App() {
  const [auth, setAuth] = useState(readSession);
  const [mode, setMode] = useState('login');
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [registration, setRegistration] = useState(EMPTY_STUDENT);
  const [students, setStudents] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [applications, setApplications] = useState([]);
  const [job, setJob] = useState(EMPTY_JOB);
  const [search, setSearch] = useState('');
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const [busy, setBusy] = useState(false);

  const notify = (text, error = false) => { setMessage(text); setIsError(error); };
  const logout = useCallback((reason = '') => {
    sessionStorage.removeItem('placementAuth');
    setAuth(null); setStudents([]); setApplications([]); setJobs([]);
    if (reason) notify(reason, true);
  }, []);

  const call = useCallback(async (path, options = {}) => {
    const headers = { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(auth?.token ? { Authorization: `Bearer ${auth.token}` } : {}), ...(options.headers || {}) };
    let response;
    try { response = await fetch(`${API}${path}`, { ...options, headers }); }
    catch { throw new Error('Cannot reach the backend. Check that Spring Boot is running on port 8080.'); }
    if (response.status === 401) { logout('Your session has expired. Please sign in again.'); throw new Error('Session expired. Please sign in again.'); }
    if (!response.ok) {
      let detail = `Request failed (${response.status})`;
      const raw = await response.text();
      if (raw) { try { const body = JSON.parse(raw); detail = body.message || body.detail || body.error || detail; } catch { detail = raw; } }
      throw new Error(detail);
    }
    if (response.status === 204) return null;
    const text = await response.text();
    return text ? JSON.parse(text) : null;
  }, [auth, logout]);

  const refresh = useCallback(async () => {
    if (!auth) return;
    try {
      const jobRows = await call('/jobs'); setJobs(jobRows || []);
      if (auth.role === 'ADMIN') {
        const [studentRows, applicationRows] = await Promise.all([call('/students'), call('/applications')]);
        setStudents(studentRows || []); setApplications(applicationRows || []);
      } else if (auth.role === 'STUDENT' && auth.studentId) {
        setApplications(await call(`/applications/student/${auth.studentId}`) || []);
      }
    } catch (error) { notify(error.message, true); }
  }, [auth, call]);

  useEffect(() => { refresh(); }, [refresh]);

  async function authenticate(event) {
    event.preventDefault(); setBusy(true); setMessage('');
    try {
      const payload = mode === 'login' ? credentials : { ...registration, cgpa: Number(registration.cgpa) };
      const response = await fetch(`${API}/auth/${mode === 'login' ? 'login' : 'register'}`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
      });
      const raw = await response.text(); let body = {};
      try { body = raw ? JSON.parse(raw) : {}; } catch { body = { message: raw }; }
      if (!response.ok) throw new Error(body.message || body.detail || `Authentication failed (${response.status})`);
      sessionStorage.setItem('placementAuth', JSON.stringify(body)); setAuth(body); notify(`Welcome, ${body.name}.`);
      setCredentials({ email: '', password: '' }); setRegistration(EMPTY_STUDENT);
    } catch (error) { notify(error.message, true); }
    finally { setBusy(false); }
  }

  async function addJob(event) {
    event.preventDefault(); setBusy(true);
    try { await call('/jobs', { method: 'POST', body: JSON.stringify({ ...job, minimumCgpa: Number(job.minimumCgpa) }) }); setJob(EMPTY_JOB); notify('Job opportunity published.'); await refresh(); }
    catch (error) { notify(error.message, true); } finally { setBusy(false); }
  }

  async function deleteJob(id) {
    if (!window.confirm('Delete this job opportunity? Jobs with applications cannot be deleted.')) return;
    setBusy(true);
    try { await call(`/jobs/${id}`, { method: 'DELETE' }); notify('Job deleted.'); await refresh(); }
    catch (error) { notify(error.message, true); } finally { setBusy(false); }
  }

  async function apply(jobId) {
    if (!auth?.studentId) { notify('No student profile is linked to this account.', true); return; }
    setBusy(true);
    try { await call(`/applications?studentId=${auth.studentId}&jobId=${jobId}`, { method: 'POST' }); notify('Application submitted successfully.'); await refresh(); }
    catch (error) { notify(error.message, true); } finally { setBusy(false); }
  }

  async function changeStatus(id, status) {
    setBusy(true);
    try { await call(`/applications/${id}/status?status=${encodeURIComponent(status)}`, { method: 'PATCH' }); notify('Application status updated.'); await refresh(); }
    catch (error) { notify(error.message, true); } finally { setBusy(false); }
  }

  const filteredJobs = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return jobs;
    return jobs.filter(item => [item.company, item.title, item.requiredSkills, item.location].some(value => String(value || '').toLowerCase().includes(term)));
  }, [jobs, search]);

  if (!auth) return <main className="auth-page"><section className="auth-card">
    <div className="brand-mark">SP</div><p className="eyebrow">CAREER DEVELOPMENT PLATFORM</p><h1>Smart Placement Portal</h1>
    <p className="muted">A single workspace for students and placement administrators.</p>
    <div className="tabs"><button className={mode === 'login' ? 'tab active' : 'tab'} type="button" onClick={() => { setMode('login'); setMessage(''); }}>Sign in</button><button className={mode === 'register' ? 'tab active' : 'tab'} type="button" onClick={() => { setMode('register'); setMessage(''); }}>Student registration</button></div>
    <form onSubmit={authenticate}>
      {mode === 'register' && <><label>Full name<input required maxLength="100" value={registration.name} onChange={e => setRegistration({ ...registration, name: e.target.value })} placeholder="Your full name" /></label><label>Department<input required maxLength="80" value={registration.department} onChange={e => setRegistration({ ...registration, department: e.target.value })} /></label><label>CGPA<input required type="number" min="0" max="10" step="0.01" value={registration.cgpa} onChange={e => setRegistration({ ...registration, cgpa: e.target.value })} placeholder="0.00 – 10.00" /></label></>}
      <label>Email address<input required type="email" maxLength="150" value={mode === 'login' ? credentials.email : registration.email} onChange={e => mode === 'login' ? setCredentials({ ...credentials, email: e.target.value }) : setRegistration({ ...registration, email: e.target.value })} placeholder="name@example.com" /></label>
      <label>Password<input required type="password" minLength="8" maxLength="72" value={mode === 'login' ? credentials.password : registration.password} onChange={e => mode === 'login' ? setCredentials({ ...credentials, password: e.target.value }) : setRegistration({ ...registration, password: e.target.value })} placeholder="At least 8 characters" /></label>
      <button className="primary full" disabled={busy}>{busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create student account'}</button>
    </form>
    {message && <p className={isError ? 'notice error' : 'notice success'} role="status">{message}</p>}
    <p className="auth-foot">Administrator accounts are provisioned by the system administrator.</p>
  </section></main>;

  const isAdmin = auth.role === 'ADMIN';
  return <main className="app-shell">
    <header className="topbar"><div className="brand"><div className="brand-mark small">SP</div><div><p className="eyebrow">CAREER DEVELOPMENT</p><h1>Smart Placement Portal</h1></div></div><div className="account"><div><strong>{auth.name}</strong><span>{isAdmin ? 'Administrator' : 'Student'} · {auth.email}</span></div><button className="outline" onClick={() => logout()}>Sign out</button></div></header>
    {message && <p className={isError ? 'notice error' : 'notice success'} role="status">{message}</p>}
    <section className="welcome"><div><p className="eyebrow">{isAdmin ? 'ADMIN CONSOLE' : 'STUDENT WORKSPACE'}</p><h2>{isAdmin ? 'Placement overview' : 'Find your next opportunity'}</h2><p className="muted">{isAdmin ? 'Manage opportunities, student records and recruitment progress.' : 'Explore opportunities that match your academic eligibility and track your applications.'}</p></div><button className="outline" onClick={refresh} disabled={busy}>↻ Refresh data</button></section>
    <section className="stats"><article><span>Open opportunities</span><strong>{jobs.length}</strong></article><article><span>{isAdmin ? 'Total applications' : 'My applications'}</span><strong>{applications.length}</strong></article>{isAdmin && <article><span>Registered students</span><strong>{students.length}</strong></article>}</section>
    {isAdmin && <section className="panel"><div className="section-heading"><div><p className="eyebrow">RECRUITMENT</p><h2>Publish a job opportunity</h2></div></div><form className="job-form" onSubmit={addJob}><label>Company<input required maxLength="120" value={job.company} onChange={e => setJob({ ...job, company: e.target.value })} placeholder="Company name" /></label><label>Job title<input required maxLength="120" value={job.title} onChange={e => setJob({ ...job, title: e.target.value })} placeholder="e.g. Java Developer" /></label><label>Required skills<input required maxLength="1000" value={job.requiredSkills} onChange={e => setJob({ ...job, requiredSkills: e.target.value })} placeholder="Java, SQL, OOP" /></label><label>Minimum CGPA<input required type="number" min="0" max="10" step="0.01" value={job.minimumCgpa} onChange={e => setJob({ ...job, minimumCgpa: e.target.value })} /></label><label>Location<input maxLength="120" value={job.location} onChange={e => setJob({ ...job, location: e.target.value })} placeholder="Pune" /></label><button className="primary" disabled={busy}>Publish opportunity</button></form></section>}
    <section className="panel"><div className="section-heading"><div><p className="eyebrow">OPPORTUNITIES</p><h2>{isAdmin ? 'Manage job opportunities' : 'Available opportunities'}</h2></div><input className="search" value={search} onChange={e => setSearch(e.target.value)} placeholder="Search company, role, skill…" /></div>
      {filteredJobs.length === 0 ? <p className="muted empty">No matching job opportunities.</p> : <div className="job-list">{filteredJobs.map(item => { const application = applications.find(a => a.job?.id === item.id); const eligible = !isAdmin && Number(auth.cgpa ?? students.find(s => s.id === auth.studentId)?.cgpa ?? 0) >= Number(item.minimumCgpa); return <article className="job-card" key={item.id}><div className="job-info"><span className="company">{item.company}</span><h3>{item.title}</h3><p>{item.location || 'Location not specified'} <span>·</span> Minimum CGPA {Number(item.minimumCgpa).toFixed(2)}</p><div className="skills">{item.requiredSkills}</div></div><div className="job-actions">{isAdmin ? <button className="danger-outline" disabled={busy} onClick={() => deleteJob(item.id)}>Delete</button> : <><span className="eligibility">{application ? application.status : eligible ? 'Eligible' : 'CGPA requirement not met'}</span><button className="primary" disabled={busy || !!application || !eligible} onClick={() => apply(item.id)}>{application ? 'Applied' : 'Apply now'}</button></>}</div></article>; })}</div>}
    </section>
    {isAdmin && <section className="panel"><div className="section-heading"><div><p className="eyebrow">STUDENT RECORDS</p><h2>Student directory</h2></div></div>{students.length === 0 ? <p className="muted empty">No registered students yet.</p> : <div className="table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Department</th><th>CGPA</th></tr></thead><tbody>{students.map(s => <tr key={s.id}><td>{s.name}</td><td>{s.email}</td><td>{s.department}</td><td>{Number(s.cgpa).toFixed(2)}</td></tr>)}</tbody></table></div>}</section>}
    <section className="panel"><div className="section-heading"><div><p className="eyebrow">TRACKING</p><h2>{isAdmin ? 'Application management' : 'My applications'}</h2></div></div>{applications.length === 0 ? <p className="muted empty">No applications to display yet.</p> : <div className="table-wrap"><table><thead><tr>{isAdmin && <th>Student</th>}<th>Company</th><th>Role</th><th>Applied on</th><th>Status</th>{isAdmin && <th>Update status</th>}</tr></thead><tbody>{applications.map(a => <tr key={a.id}>{isAdmin && <td>{a.student?.name}<small className="cell-sub">{a.student?.email}</small></td>}<td>{a.job?.company}</td><td>{a.job?.title}</td><td>{a.appliedAt ? new Date(a.appliedAt).toLocaleString() : '—'}</td><td><span className="status-pill">{a.status}</span></td>{isAdmin && <td><select value={a.status} disabled={busy} onChange={e => changeStatus(a.id, e.target.value)}>{STATUSES.map(status => <option key={status} value={status}>{status}</option>)}</select></td>}</tr>)}</tbody></table></div>}</section>
    <footer>Smart Placement Management Portal · Academic project prototype · Use test data only.</footer>
  </main>;
}
