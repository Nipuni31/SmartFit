/* eslint-disable react/prop-types */
import { useEffect, useState } from 'react'

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

export default function TailorDashboard({ onLogout }) {
  const [profileForm, setProfileForm] = useState({ shopName: '', shopAddress: '', specialization: '' })
  const [customers, setCustomers] = useState([])
  const [selectedCustomer, setSelectedCustomer] = useState(null)
  const [customerHistory, setCustomerHistory] = useState({ measurements: [], predictions: [] })
  const [orders, setOrders] = useState([])
  const [newMeasurement, setNewMeasurement] = useState({ height: '', shoulder: '', waist: '', hip: '', chestDepth: '', hipDepth: '' })
  const [orderDetails, setOrderDetails] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const apiBase = import.meta.env.DEV ? 'http://localhost:8081/api' : '/api'
  const token = typeof globalThis !== 'undefined' && globalThis.localStorage ? localStorage.getItem('token') : null

  useEffect(() => {
    if (!token) return
    fetchTailorProfile()
    fetchCustomers()
    fetchOrders()
  }, [])

  async function fetchTailorProfile() {
    setError(null)
    try {
      const res = await fetch(`${apiBase}/tailor/profile`, { headers: { Authorization: token } })
      const data = await safeParseResponse(res)
      if (res.ok && data) {
        setProfileForm({ shopName: data.shopName || '', shopAddress: data.shopAddress || '', specialization: data.specialization || '' })
      } else {
        setError((data && (data.message || data.error)) || `Unable to load profile (${res.status})`)
      }
    } catch (err) {
      setError(err.message)
    }
  }

  async function fetchCustomers() {
    setError(null)
    try {
      const res = await fetch(`${apiBase}/tailor/customers`, { headers: { Authorization: token } })
      const data = await safeParseResponse(res)
      if (res.ok && Array.isArray(data)) {
        setCustomers(data)
      } else {
        setError((data && (data.message || data.error)) || `Unable to load customers (${res.status})`)
      }
    } catch (err) {
      setError(err.message)
    }
  }

  async function fetchOrders() {
    setError(null)
    try {
      const res = await fetch(`${apiBase}/tailor/orders`, { headers: { Authorization: token } })
      const data = await safeParseResponse(res)
      if (res.ok && Array.isArray(data)) {
        setOrders(data)
      } else {
        setError((data && (data.message || data.error)) || `Unable to load orders (${res.status})`)
      }
    } catch (err) {
      setError(err.message)
    }
  }

  async function fetchCustomerHistory(customerId) {
    setError(null)
    setCustomerHistory({ measurements: [], predictions: [] })
    try {
      const res = await fetch(`${apiBase}/tailor/customer/${customerId}/history`, { headers: { Authorization: token } })
      const data = await safeParseResponse(res)
      if (res.ok && data) {
        setCustomerHistory({
          measurements: Array.isArray(data.measurements) ? data.measurements : [],
          predictions: Array.isArray(data.predictions) ? data.predictions : []
        })
      } else {
        setError((data && (data.message || data.error)) || `Unable to load customer history (${res.status})`)
      }
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleProfileSave(e) {
    e.preventDefault()
    setError(null)
    setMessage('')
    setLoading(true)
    try {
      const res = await fetch(`${apiBase}/tailor/profile`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', Authorization: token },
        body: JSON.stringify(profileForm)
      })
      const data = await safeParseResponse(res)
      setLoading(false)
      if (res.ok) {
        setMessage('Tailor profile updated successfully.')
        fetchTailorProfile()
      } else {
        setError((data && (data.message || data.error)) || `Unable to update profile (${res.status})`)
      }
    } catch (err) {
      setLoading(false)
      setError(err.message)
    }
  }

  async function handleAddMeasurement(e) {
    e.preventDefault()
    setError(null)
    setMessage('')
    if (!selectedCustomer) {
      setError('Select a customer first.')
      return
    }
    setLoading(true)
    try {
      const res = await fetch(`${apiBase}/tailor/customer/${selectedCustomer.id}/measurements`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: token },
        body: JSON.stringify(newMeasurement)
      })
      const data = await safeParseResponse(res)
      setLoading(false)
      if (res.ok) {
        setMessage('Measurement added successfully.')
        fetchCustomerHistory(selectedCustomer.id)
      } else {
        setError((data && (data.message || data.error)) || `Unable to save measurement (${res.status})`)
      }
    } catch (err) {
      setLoading(false)
      setError(err.message)
    }
  }

  async function handleCreateOrder(e) {
    e.preventDefault()
    setError(null)
    setMessage('')
    if (!selectedCustomer) {
      setError('Select a customer before creating an order.')
      return
    }
    setLoading(true)
    try {
      const res = await fetch(`${apiBase}/tailor/order/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: token },
        body: JSON.stringify({ customerId: selectedCustomer.id, orderDetails })
      })
      const data = await safeParseResponse(res)
      setLoading(false)
      if (res.ok) {
        setMessage('Order created successfully.')
        setOrderDetails('')
        fetchOrders()
      } else {
        setError((data && (data.message || data.error)) || `Unable to create order (${res.status})`)
      }
    } catch (err) {
      setLoading(false)
      setError(err.message)
    }
  }

  async function handleUpdateOrderStatus(orderId, status) {
    setError(null)
    setMessage('')
    try {
      const res = await fetch(`${apiBase}/tailor/order/${orderId}/status`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: token },
        body: JSON.stringify({ status })
      })
      const data = await safeParseResponse(res)
      if (res.ok) {
        setMessage('Order status updated.')
        fetchOrders()
      } else {
        setError((data && (data.message || data.error)) || `Unable to update order (${res.status})`)
      }
    } catch (err) {
      setError(err.message)
    }
  }

  const handleCustomerSelect = (customer) => {
    setSelectedCustomer(customer)
    fetchCustomerHistory(customer.id)
  }

  const measurementFields = [
    { label: 'Height (cm)', key: 'height' },
    { label: 'Shoulder (cm)', key: 'shoulder' },
    { label: 'Waist (cm)', key: 'waist' },
    { label: 'Hip (cm)', key: 'hip' },
    { label: 'Chest depth', key: 'chestDepth' },
    { label: 'Hip depth', key: 'hipDepth' }
  ]

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="dashboard-label">Tailor Dashboard</p>
          <h1>Manage customer records, orders and tailored predictions.</h1>
        </div>
        <button className="dashboard-logout" onClick={onLogout}>Logout</button>
      </header>

      <div className="dashboard-grid">
        <section className="dashboard-card">
          <h2>My tailor profile</h2>
          <p>Keep your shop, location and specialization up to date.</p>
          <form className="profile-form" onSubmit={handleProfileSave}>
            <label htmlFor="tailor-shopName">Shop name</label>
            <input
              id="tailor-shopName"
              type="text"
              className="dashboard-input"
              value={profileForm.shopName}
              onChange={e => setProfileForm({ ...profileForm, shopName: e.target.value })}
              placeholder="Shop or studio name"
            />
            <label htmlFor="tailor-shopAddress">Shop address</label>
            <input
              id="tailor-shopAddress"
              type="text"
              className="dashboard-input"
              value={profileForm.shopAddress}
              onChange={e => setProfileForm({ ...profileForm, shopAddress: e.target.value })}
              placeholder="Shop address"
            />
            <label htmlFor="tailor-specialization">Specialization</label>
            <input
              id="tailor-specialization"
              type="text"
              className="dashboard-input"
              value={profileForm.specialization}
              onChange={e => setProfileForm({ ...profileForm, specialization: e.target.value })}
              placeholder="Formal, casual, custom fittings"
            />
            <button type="submit" className="dashboard-button" disabled={loading}>
              {loading ? 'Saving…' : 'Save Profile'}
            </button>
          </form>
        </section>

        <section className="dashboard-card dashboard-summary">
          <h2>Customer list</h2>
          <p>Select a customer to view measurements and prediction history.</p>
          {customers.length ? (
            <ul className="history-list">
              {customers.map(customer => (
                <li key={customer.id}>
                  <button
                    type="button"
                    className="auth-link"
                    style={{ display: 'block', textAlign: 'left', width: '100%', opacity: selectedCustomer?.id === customer.id ? 1 : 0.85 }}
                    onClick={() => handleCustomerSelect(customer)}
                  >
                    <strong>{customer.name || customer.username}</strong>
                    <div>{customer.email}</div>
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p>No registered customers yet.</p>
          )}
        </section>

        <section className="dashboard-card dashboard-history">
          <h2>Selected customer</h2>
          {selectedCustomer ? (
            <>
              <div className="prediction-card">
                <div><strong>Name:</strong> {selectedCustomer.name || selectedCustomer.username}</div>
                <div><strong>Email:</strong> {selectedCustomer.email}</div>
                <div><strong>Height:</strong> {customerHistory.height ?? 'N/A'} cm</div>
                <div><strong>Weight:</strong> {customerHistory.weight ?? 'N/A'} kg</div>
              </div>

              <div style={{ marginTop: '20px' }}>
                <h3>Past measurements</h3>
                {customerHistory.measurements.length ? (
                  <ul className="history-list">
                    {customerHistory.measurements.map((m, index) => (
                      <li key={`${m.id}-${index}`}>
                        <strong>{new Date(m.createdAt).toLocaleString()}</strong>
                        <div>Height: {m.height ?? '—'} cm</div>
                        <div>Shoulder: {m.shoulder ?? '—'} cm · Waist: {m.waist ?? '—'} cm</div>
                        <div>Hip: {m.hip ?? '—'} cm · Chest depth: {m.chestDepth ?? '—'} cm</div>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p>No manual measurements recorded for this customer.</p>
                )}
              </div>

              <div style={{ marginTop: '20px' }}>
                <h3>AI prediction history</h3>
                {customerHistory.predictions.length ? (
                  <ul className="history-list">
                    {customerHistory.predictions.map((item, index) => (
                      <li key={`${item.id}-${index}`}>
                        <strong>{new Date(item.createdAt).toLocaleString()}</strong>
                        <div>Top: {item.topSize || 'N/A'} · Bottom: {item.bottomSize || 'N/A'}</div>
                        <div>Fit: {item.fit || 'N/A'} · Confidence: {item.confidence ?? 'N/A'}</div>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p>No AI predictions available for this customer.</p>
                )}
              </div>

              <div style={{ marginTop: '20px' }}>
                <h3>Add manual measurement</h3>
                <form className="profile-form" onSubmit={handleAddMeasurement}>
                  {measurementFields.map(field => (
                    <div key={field.key}>
                      <label htmlFor={`measurement-${field.key}`}>{field.label}</label>
                      <input
                        id={`measurement-${field.key}`}
                        type="number"
                        step="0.1"
                        className="dashboard-input"
                        value={newMeasurement[field.key]}
                        onChange={e => setNewMeasurement({ ...newMeasurement, [field.key]: e.target.value })}
                        placeholder={field.label}
                      />
                    </div>
                  ))}
                  <button type="submit" className="dashboard-button" disabled={loading}>
                    {loading ? 'Saving…' : 'Save Measurement'}
                  </button>
                </form>
              </div>

              <div style={{ marginTop: '20px' }}>
                <h3>Create tailoring order</h3>
                <form className="profile-form" onSubmit={handleCreateOrder}>
                  <label htmlFor="order-details">Order details</label>
                  <textarea
                    id="order-details"
                    className="dashboard-input"
                    rows="4"
                    value={orderDetails}
                    onChange={e => setOrderDetails(e.target.value)}
                    placeholder="Enter tailoring request or order notes"
                  />
                  <button type="submit" className="dashboard-button" disabled={loading}>
                    {loading ? 'Creating…' : 'Create Order'}
                  </button>
                </form>
              </div>
            </>
          ) : (
            <p>Select a customer from the list to manage measurements and orders.</p>
          )}
        </section>

        <section className="dashboard-card dashboard-summary">
          <h2>Order tracker</h2>
          {orders.length ? (
            <ul className="history-list">
              {orders.map(order => (
                <li key={order.id}>
                  <div><strong>Order ID:</strong> {order.id}</div>
                  <div><strong>Customer:</strong> {order.buyerId}</div>
                  <div><strong>Status:</strong> {order.status}</div>
                  <div><strong>Details:</strong> {order.details}</div>
                  <div style={{ marginTop: '10px', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                    {['Pending', 'Accepted', 'In Progress', 'Completed'].map(status => (
                      <button
                        key={status}
                        type="button"
                        className="dashboard-button"
                        style={{ width: 'auto', padding: '10px 14px' }}
                        onClick={() => handleUpdateOrderStatus(order.id, status)}
                      >
                        {status}
                      </button>
                    ))}
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p>No tailoring orders yet.</p>
          )}
        </section>
      </div>

      <div className="dashboard-messages">
        {message && <p className="dashboard-success">{message}</p>}
        {error && <p className="dashboard-error">{error}</p>}
      </div>
    </div>
  )
}

