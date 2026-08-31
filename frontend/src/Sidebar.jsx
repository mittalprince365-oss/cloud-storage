import { Link, useLocation } from 'react-router-dom'

function Sidebar() {
  const location = useLocation()

  const items = [
    { path: '/', label: '📁 My Drive' },
    { path: '/shared', label: '🔗 Shared with me' },
    { path: '/trash', label: '🗑️ Trash' },
  ]

  return (
    <div className="w-56 bg-slate-800 border-r border-slate-700 min-h-screen p-4">
      <div className="space-y-1">
        {items.map((item) => (
          <Link key={item.path} to={item.path}
            className={`block px-3 py-2 rounded text-sm ${
              location.pathname === item.path
                ? 'bg-blue-600 text-white'
                : 'text-slate-300 hover:bg-slate-700'
            }`}>
            {item.label}
          </Link>
        ))}
      </div>
    </div>
  )
}

export default Sidebar