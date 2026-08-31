import { useNavigate } from 'react-router-dom'

function Navbar() {
  const navigate = useNavigate()
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    navigate('/login')
  }

  return (
    <nav className="bg-slate-800 border-b border-slate-700 px-6 py-3 flex justify-between items-center">
      <h1 className="text-white font-bold text-lg">☁️ Cloud Storage</h1>
      <div className="flex items-center gap-3">
        <span className="text-slate-400 text-sm">{user?.name}</span>
        <button onClick={logout}
          className="bg-slate-700 hover:bg-slate-600 text-white px-4 py-2 rounded text-sm">
          Logout
        </button>
      </div>
    </nav>
  )
}

export default Navbar