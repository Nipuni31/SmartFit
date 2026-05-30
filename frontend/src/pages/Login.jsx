import { useState } from 'react'

export default function Login({ onRegister }){
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const [loggedIn, setLoggedIn] = useState(false)

  async function safeParseResponse(res){
    const text = await res.text()
    if(!text) return null
    const ct = res.headers.get('content-type') || ''
    if(ct.includes('application/json')){
      try{ return JSON.parse(text) }
      catch(e){ console.error('Invalid JSON', text); throw new Error('Invalid JSON response') }
    }
    return text
  }

  const authBase = import.meta.env.DEV ? 'http://localhost:8081' : '/api'

  async function handleSubmit(e){
    e.preventDefault()
    setError(null)
    setLoading(true)
    try{
      const res = await fetch(`${authBase}/auth/login`,{
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      })
      const data = await safeParseResponse(res)
      setLoading(false)
      if(res.ok && data && data.token){
        localStorage.setItem('token', data.token)
        if(data.userId || data.username) localStorage.setItem('user', JSON.stringify({ id: data.userId, username: data.username }))
        setLoggedIn(true)
      } else {
        setError((data && (data.message || data.error)) || `Login failed (${res.status})`)
      }
    }catch(err){ setLoading(false); setError(err.message) }
  }

  if(loggedIn){
    return (
      <div className="auth-page">
        <div className="auth-card">
          <h2 className="auth-title">Welcome back!</h2>
          <p className="auth-subtitle">You are signed in successfully. You can close this tab or refresh to continue.</p>
        </div>
      </div>
    )
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
              onChange={e=>setUsername(e.target.value)}
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
              onChange={e=>setPassword(e.target.value)}
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
