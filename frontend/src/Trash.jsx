import { useState, useEffect } from 'react'
import api from './api'
import Navbar from './Navbar'
import Sidebar from './Sidebar'

function Trash() {
  const [files, setFiles] = useState([])
  const [loading, setLoading] = useState(true)

  const fetchTrash = async () => {
    setLoading(true)
    try {
      const res = await api.get('/api/files/trash')
      setFiles(res.data)
    } catch (err) {
      console.log(err)
    }
    setLoading(false)
  }

  useEffect(() => {
    fetchTrash()
  }, [])

  const restoreFile = async (id) => {
    try {
      await api.post(`/api/files/${id}/restore`)
      fetchTrash()
    } catch (err) {
      alert('Could not restore')
    }
  }

  const formatSize = (bytes) => {
    if (!bytes) return '-'
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  }

  return (
    <div className="min-h-screen bg-slate-900">
      <Navbar />
      <div className="flex">
        <Sidebar />
        <div className="flex-1 p-8">
          <h1 className="text-2xl font-bold text-white mb-2">🗑️ Trash</h1>
          <p className="text-slate-400 text-sm mb-6">Deleted files can be restored here.</p>

          {loading ? (
            <p className="text-slate-400">Loading...</p>
          ) : files.length === 0 ? (
            <div className="bg-slate-800 border border-slate-700 rounded-lg p-8 text-center">
              <p className="text-slate-400">Trash is empty.</p>
            </div>
          ) : (
            <div className="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-slate-900 text-slate-400">
                  <tr>
                    <th className="text-left px-4 py-3">Name</th>
                    <th className="text-left px-4 py-3">Size</th>
                    <th className="text-left px-4 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {files.map((file) => (
                    <tr key={file.id} className="border-t border-slate-700">
                      <td className="px-4 py-3 text-white">📄 {file.name}</td>
                      <td className="px-4 py-3 text-slate-400">{formatSize(file.fileSize)}</td>
                      <td className="px-4 py-3">
                        <button onClick={() => restoreFile(file.id)}
                          className="text-green-400 hover:text-green-300 text-xs">
                          ♻️ Restore
                        </button>
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

export default Trash