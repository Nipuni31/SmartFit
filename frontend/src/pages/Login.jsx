/* eslint-disable react/prop-types */
import { useState } from 'react'

const VALID_ROLES = new Set(['BUYER', 'TAILOR'])

async function safeParseResponse(res) {
  const text = await res.text()
  if (!text) return null
  const ct = res.headers.get('content-type') || ''
  if (ct.includes('application/json')) {
    try {
      return JSON.parse(text)
    } catch (e) {
      console.error('Invalid JSON', e)
      throw new Error('Invalid JSON response')
    }
  }
  return text
}

export default function Login({ onRegister, onLoginSuccess }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const authBase = import.meta.env.DEV ? 'http://localhost:8081' : '/api'

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const res = await fetch(`${authBase}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      })
      const data = await safeParseResponse(res)
      setLoading(false)
      if (res.ok && data?.token) {
        if (!VALID_ROLES.has(data.role)) {
          setError('Unauthorized login: invalid user role.')
          return
        }
        localStorage.setItem('token', data.token)
        if (data.userId || data.username) localStorage.setItem('user', JSON.stringify({ id: data.userId, username: data.username }))
        localStorage.setItem('userRole', data.role)
        if (typeof onLoginSuccess === 'function') onLoginSuccess()
      } else {
        setError((data && (data.message || data.error)) || `Login failed (${res.status})`)
      }
    } catch (err) {
      setLoading(false)
      setError(err.message)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">SmartFit Login</h1>
        <p className="auth-subtitle">Sign in to access your personal AI fitting assistant and upload your images for size prediction.</p>
        <form className="auth-form" onSubmit={handleSubmit}>
          <div>
            <label htmlFor="username">Username</label>
            <input
              id="username"
              className="auth-input"
              value={username}
              onChange={e => setUsername(e.target.value)}
              placeholder="Enter your username"
              autoComplete="username"
            />
          </div>
          <div>
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              className="auth-input"
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="Enter your password"
              autoComplete="current-password"
            />
          </div>
          <button type="submit" className="auth-button" disabled={loading}>
            {loading ? 'Signing in...' : 'Login'}
          </button>
          {error && <div className="auth-error">{error}</div>}
        </form>
        <div className="auth-footer">
          <span>Don’t have an account?</span>
          <button type="button" className="auth-link" onClick={onRegister}>Register</button>
        </div>
      </div>
    </div>
  )
}
