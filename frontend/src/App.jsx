import { useState } from 'react'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import BuyerDashboard from './pages/BuyerDashboard.jsx'
import TailorDashboard from './pages/TailorDashboard.jsx'
import './App.css'

const VALID_ROLES = new Set(['BUYER', 'TAILOR'])

function App() {
  const getStoredRole = () => {
    if (typeof globalThis === 'undefined' || !globalThis.localStorage) return null
    const role = globalThis.localStorage.getItem('userRole')
    return VALID_ROLES.has(role) ? role : null
  }

  const getPageForRole = role => {
    if (role === 'TAILOR') return 'tailor'
    if (role === 'BUYER') return 'buyer'
    return 'login'
  }

  const [page, setPage] = useState(() => {
    if (typeof globalThis === 'undefined' || !globalThis.localStorage) return 'login'
    const token = globalThis.localStorage.getItem('token')
    if (!token) return 'login'
    return getPageForRole(getStoredRole())
  })

  const getDashboardPage = () => getPageForRole(getStoredRole())

  const handleLoginSuccess = () => {
    const nextPage = getDashboardPage()
    if (nextPage === 'login') {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('userRole')
      setPage('login')
    } else {
      setPage(nextPage)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('userRole')
    setPage('login')
  }

  if (page === 'register') {
    return <Register onLogin={() => setPage('login')} />
  }

  if (page === 'tailor') {
    return <TailorDashboard onLogout={handleLogout} />
  }

  if (page === 'buyer') {
    return <BuyerDashboard onLogout={handleLogout} />
  }

  return <Login onRegister={() => setPage('register')} onLoginSuccess={handleLoginSuccess} />
}

export default App
