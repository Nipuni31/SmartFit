import { useState } from 'react'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import './App.css'

function App() {
  const [page, setPage] = useState('login')

  return page === 'login' ? (
    <Login onRegister={() => setPage('register')} />
  ) : (
    <Register onLogin={() => setPage('login')} />
  )
}

export default App
