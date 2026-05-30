import { useEffect, useState } from 'react'
import UploadImage from './UploadImage.jsx'

export default function BuyerDashboard({ onLogout }) {
  const [profile, setProfile] = useState({ name: '', email: '', gender: '', height: '', weight: '' })
  const [profileForm, setProfileForm] = useState({ name: '', gender: '', height: '', weight: '' })
  const [frontFile, setFrontFile] = useState(null)
  const [sideFile, setSideFile] = useState(null)
  const [prediction, setPrediction] = useState(null)
  const [history, setHistory] = useState([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const apiBase = import.meta.env.DEV ? 'http://localhost:8081/api' : '/api'
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null

  useEffect(() => {
    if (!token) return
    fetchProfile()
    fetchPredictionHistory()
  }, [])

  async function safeParseResponse(res) {
    const text = await res.text()
    if (!text) return null
    const ct = res.headers.get('content-type') || ''
    if (ct.includes('application/json')) {
      try { return JSON.parse(text) } catch (e) { console.error('Invalid JSON', text); throw new Error('Invalid JSON response') }
    }
    return text
  }

  async function fetchProfile() {
    setError(null)
    try {
      const res = await fetch(`${apiBase}/buyer/profile`, {
        headers: { Authorization: token }
      })
      const data = await safeParseResponse(res)
      if (res.ok && data) {
        setProfile({
          name: data.name || '',
          email: data.email || '',
          gender: data.gender || '',
          height: data.height || '',
          weight: data.weight || ''
        })
        setProfileForm({
          name: data.name || '',
          gender: data.gender || '',
          height: data.height || '',
          weight: data.weight || ''
        })
      } else {
        setError((data && (data.message || data.error)) || `Unable to load profile (${res.status})`)
      }
    } catch (err) {
      setError(err.message)
    }
  }

  async function fetchPredictionHistory() {
    setError(null)
    try {
      const res = await fetch(`${apiBase}/buyer/predictions`, {
        headers: { Authorization: token }
      })
      const data = await safeParseResponse(res)
      if (res.ok && Array.isArray(data)) {
        setHistory(data)
      } else {
        console.warn('Unable to load history', data)
      }
    } catch (err) {
      console.warn(err)
    }
  }

  async function handleProfileSave(e) {
    e.preventDefault()
    setError(null)
    setMessage('')
    setLoading(true)
    try {
      const res = await fetch(`${apiBase}/buyer/profile`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: token
        },
        body: JSON.stringify(profileForm)
      })
      const data = await safeParseResponse(res)
      setLoading(false)
      if (res.ok) {
        setMessage('Profile updated successfully.')
        fetchProfile()
      } else {
        setError((data && (data.message || data.error)) || `Unable to update profile (${res.status})`)
      }
    } catch (err) {
      setLoading(false)
      setError(err.message)
    }
  }

  async function handlePredict() {
    setError(null)
    setMessage('')
    if (!frontFile || !sideFile) {
      setError('Please upload both front and side images before predicting.')
      return
    }
    if (!profileForm.height) {
      setError('Please enter your height before predicting.')
      return
    }

    setLoading(true)
    try {
      const formData = new FormData()
      formData.append('front', frontFile)
      formData.append('side', sideFile)
      formData.append('height_cm', String(profileForm.height))

      const res = await fetch(`${apiBase}/predict`, {
        method: 'POST',
        body: formData
      })
      const data = await safeParseResponse(res)
      setLoading(false)

      if (!res.ok) {
        setError((data && (data.message || data.error)) || `Prediction failed (${res.status})`)
        return
      }

      const saved = {
        topSize: data.topSize || data.top_size || data.top || 'Unknown',
        bottomSize: data.bottomSize || data.bottom_size || data.bottom || 'Unknown',
        fit: data.fit || data.recommendation || data.fitRecommendation || 'Standard',
        confidence: data.confidence || data.confidenceScore || null,
        height: profileForm.height,
        createdAt: new Date().toISOString()
      }

      setPrediction(saved)
      setHistory(prev => [saved, ...prev])
      await savePredictionResult(saved)
      setMessage('Prediction completed and saved to your history.')
    } catch (err) {
      setLoading(false)
      setError(err.message)
    }
  }

  async function savePredictionResult(saved) {
    if (!token) return
    try {
      await fetch(`${apiBase}/buyer/save-prediction`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: token
        },
        body: JSON.stringify(saved)
      })
    } catch (err) {
      console.warn('Failed to persist prediction', err)
    }
  }

  const handleFilesChange = (field, file) => {
    if (field === 'front') setFrontFile(file)
    if (field === 'side') setSideFile(file)
  }

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="dashboard-label">SmartFit Buyer Dashboard</p>
          <h1>Track your measurements, predictions, and fit history.</h1>
        </div>
        <button className="dashboard-logout" onClick={onLogout}>Logout</button>
      </header>

      <div className="dashboard-grid">
        <section className="dashboard-card">
          <h2>Upload images</h2>
          <p>Upload a front and side image, then enter your height to get a tailored size recommendation.</p>
          <UploadImage
            frontFile={frontFile}
            sideFile={sideFile}
            onFileSelect={handleFilesChange}
          />
          <label className="dashboard-field-label" htmlFor="height-input">Height (cm)</label>
          <input
            id="height-input"
            type="number"
            className="dashboard-input"
            value={profileForm.height}
            onChange={e => setProfileForm({ ...profileForm, height: e.target.value })}
            placeholder="Enter your height"
          />
          <button className="dashboard-button" onClick={handlePredict} disabled={loading}>
            {loading ? 'Predicting…' : 'Get Size Prediction'}
          </button>
        </section>

        <section className="dashboard-card dashboard-summary">
          <h2>Latest prediction</h2>
          {prediction ? (
            <div className="prediction-card">
              <div><strong>Top size:</strong> {prediction.topSize}</div>
              <div><strong>Bottom size:</strong> {prediction.bottomSize}</div>
              <div><strong>Fit:</strong> {prediction.fit}</div>
              <div><strong>Confidence:</strong> {prediction.confidence ?? 'N/A'}</div>
              <div><strong>Height:</strong> {prediction.height} cm</div>
            </div>
          ) : (
            <p>No prediction yet. Upload your images and submit to see your first recommendation.</p>
          )}

          <div className="dashboard-metrics">
            <div>
              <p className="metric-label">Profile height</p>
              <p>{profile.height ? `${profile.height} cm` : 'Not set'}</p>
            </div>
            <div>
              <p className="metric-label">Profile weight</p>
              <p>{profile.weight ? `${profile.weight} kg` : 'Not set'}</p>
            </div>
          </div>
          <div className="dashboard-messages">
            {message && <p className="dashboard-success">{message}</p>}
            {error && <p className="dashboard-error">{error}</p>}
          </div>
        </section>

        <section className="dashboard-card dashboard-history">
          <h2>Prediction history</h2>
          {history.length ? (
            <ul className="history-list">
              {history.map((item, index) => (
                <li key={`${item.createdAt}-${index}`}>
                  <div><strong>{new Date(item.createdAt).toLocaleString()}</strong></div>
                  <div>Top: {item.topSize} · Bottom: {item.bottomSize} · Fit: {item.fit}</div>
                </li>
              ))}
            </ul>
          ) : (
            <p>You haven't saved any predictions yet.</p>
          )}
        </section>

        <section className="dashboard-card dashboard-profile">
          <h2>Update profile</h2>
          <form className="profile-form" onSubmit={handleProfileSave}>
            <label htmlFor="profile-name">Name</label>
            <input
              id="profile-name"
              type="text"
              className="dashboard-input"
              value={profileForm.name}
              onChange={e => setProfileForm({ ...profileForm, name: e.target.value })}
              placeholder="Full name"
            />
            <label htmlFor="profile-gender">Gender</label>
            <input
              id="profile-gender"
              type="text"
              className="dashboard-input"
              value={profileForm.gender}
              onChange={e => setProfileForm({ ...profileForm, gender: e.target.value })}
              placeholder="Gender"
            />
            <label htmlFor="profile-weight">Weight (kg)</label>
            <input
              id="profile-weight"
              type="number"
              className="dashboard-input"
              value={profileForm.weight}
              onChange={e => setProfileForm({ ...profileForm, weight: e.target.value })}
              placeholder="Enter your weight"
            />
            <button type="submit" className="dashboard-button" disabled={loading}>
              {loading ? 'Saving…' : 'Save Profile'}
            </button>
          </form>
        </section>
      </div>
    </div>
  )
}
