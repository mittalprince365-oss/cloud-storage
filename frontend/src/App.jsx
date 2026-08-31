import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Login from './Login'
import Register from './Register'

function Home() {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (!user) return <Navigate to="/login" />

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    window.location.href = '/login'
  }

  return (
    <div className="min-h-screen bg-slate-900 text-white p-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">☁️ Cloud Storage</h1>
        <button onClick={logout}
          className="bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded text-sm">
          Logout
        </button>
      </div>
      <div className="bg-slate-800 rounded-lg p-6">
        <p className="text-lg">Welcome, <span className="font-bold">{user.name}</span>! 👋</p>
        <p className="text-slate-400 mt-2">Email: {user.email}</p>
        <p className="text-slate-400">Role: {user.role}</p>
      </div>
    </div>
  )
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App