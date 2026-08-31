import { useState, useEffect } from 'react'
import api from './api'
import Navbar from './Navbar'
import Sidebar from './Sidebar'

function SharedWithMe() {
  const [shares, setShares] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/api/shares/with-me')
      .then((res) => setShares(res.data))
      .catch((err) => console.log(err))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="min-h-screen bg-slate-900">
      <Navbar />
      <div className="flex">
        <Sidebar />
        <div className="flex-1 p-8">
          <h1 className="text-2xl font-bold text-white mb-6">Shared with me</h1>

          {loading ? (
            <p className="text-slate-400">Loading...</p>
          ) : shares.length === 0 ? (
            <div className="bg-slate-800 border border-slate-700 rounded-lg p-8 text-center">
              <p className="text-slate-400">Nothing shared with you yet.</p>
            </div>
          ) : (
            <div className="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-slate-900 text-slate-400">
                  <tr>
                    <th className="text-left px-4 py-3">Name</th>
                    <th className="text-left px-4 py-3">Access</th>
                  </tr>
                </thead>
                <tbody>
                  {shares.map((s) => (
                    <tr key={s.shareId} className="border-t border-slate-700">
                      <td className="px-4 py-3 text-white">📄 {s.fileName}</td>
                      <td className="px-4 py-3">
                        <span className={`text-xs px-2 py-1 rounded ${
                          s.role === 'EDITOR'
                            ? 'bg-purple-900 text-purple-300'
                            : 'bg-slate-700 text-slate-300'
                        }`}>
                          {s.role}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default SharedWithMe