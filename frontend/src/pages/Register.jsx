import { useState } from 'react'

export default function Register({ onLogin }){
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState('BUYER')
  const [shopName, setShopName] = useState('')
  const [shopAddress, setShopAddress] = useState('')
  const [specialization, setSpecialization] = useState('')
  const [phone, setPhone] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)

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
      const res = await fetch(`${authBase}/auth/register`,{
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username,
          email,
          password,
          role,
          shopName,
          shopAddress,
          specialization,
          phone,
          description
        })
      })
      const data = await safeParseResponse(res)
      setLoading(false)
      if(res.ok){
        setSuccess(true)
      } else {
        setError((data && (data.message || data.error)) || `Registration failed (${res.status})`)
      }
    }catch(err){ setLoading(false); setError(err.message) }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        {success ? (
          <>
            <h1 className="auth-title">Account Created</h1>
            <p className="auth-subtitle">Your account was successfully registered. Please sign in to continue.</p>
            <button type="button" className="auth-button" onClick={onLogin}>Go to Login</button>
          </>
        ) : (
          <>
            <h1 className="auth-title">Create your SmartFit account</h1>
            <p className="auth-subtitle">Register so you can upload images, get size predictions, and save your activity.</p>
            <form className="auth-form" onSubmit={handleSubmit}>
              <div>
                <label htmlFor="reg-username">Username</label>
                <input
                  id="reg-username"
                  className="auth-input"
                  value={username}
                  onChange={e=>setUsername(e.target.value)}
                  placeholder="Choose a username"
                  autoComplete="username"
                />
              </div>
              <div>
                <label htmlFor="reg-email">Email</label>
                <input
                  id="reg-email"
                  type="email"
                  className="auth-input"
                  value={email}
                  onChange={e=>setEmail(e.target.value)}
                  placeholder="your@email.com"
                  autoComplete="email"
                />
              </div>
              <div>
                <label htmlFor="reg-role">Role</label>
                <select
                  id="reg-role"
                  className="auth-input"
                  value={role}
                  onChange={e => setRole(e.target.value)}
                >
                  <option value="BUYER">Buyer</option>
                  <option value="TAILOR">Tailor</option>
                </select>
              </div>
              {role === 'TAILOR' && (
                <>
                  <div>
                    <label htmlFor="reg-shopName">Shop Name</label>
                    <input
                      id="reg-shopName"
                      className="auth-input"
                      value={shopName}
                      onChange={e=>setShopName(e.target.value)}
                      placeholder="Enter your shop or studio name"
                    />
                  </div>
                  <div>
                    <label htmlFor="reg-shopAddress">Shop Address</label>
                    <input
                      id="reg-shopAddress"
                      className="auth-input"
                      value={shopAddress}
                      onChange={e=>setShopAddress(e.target.value)}
                      placeholder="Enter your shop address"
                    />
                  </div>
                  <div>
                    <label htmlFor="reg-specialization">Specialization</label>
                    <input
                      id="reg-specialization"
                      className="auth-input"
                      value={specialization}
                      onChange={e=>setSpecialization(e.target.value)}
                      placeholder="E.g. formal, casual, traditional"
                    />
                  </div>
                  <div>
                    <label htmlFor="reg-phone">Phone</label>
                    <input
                      id="reg-phone"
                      className="auth-input"
                      value={phone}
                      onChange={e=>setPhone(e.target.value)}
                      placeholder="Contact phone"
                    />
                  </div>
                  <div>
                    <label htmlFor="reg-description">Description</label>
                    <input
                      id="reg-description"
                      className="auth-input"
                      value={description}
                      onChange={e=>setDescription(e.target.value)}
                      placeholder="Brief description of your tailoring services"
                    />
                  </div>
                </>
              )}
              <div>
                <label htmlFor="reg-password">Password</label>
                <input
                  id="reg-password"
                  type="password"
                  className="auth-input"
                  value={password}
                  onChange={e=>setPassword(e.target.value)}
                  placeholder="Create a secure password"
                  autoComplete="new-password"
                />
              </div>
              <button type="submit" className="auth-button" disabled={loading}>
                {loading ? 'Registering...' : 'Register'}
              </button>
              {error && <div className="auth-error">{error}</div>}
            </form>
            <div className="auth-footer">
              <span>Already registered?</span>
              <button type="button" className="auth-link" onClick={onLogin}>Login</button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
