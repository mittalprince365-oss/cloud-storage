import { useState, useEffect } from 'react'
import api from './api'
import Navbar from './Navbar'
import Sidebar from './Sidebar'

function MyDrive() {
  const [files, setFiles] = useState([])
  const [folders, setFolders] = useState([])
  const [loading, setLoading] = useState(true)

  const fetchData = async () => {
    setLoading(true)
    try {
      const [filesRes, foldersRes] = await Promise.all([
        api.get('/api/files'),
        api.get('/api/folders'),
      ])
      setFiles(filesRes.data)
      setFolders(foldersRes.data)
    } catch (err) {
      console.log(err)
    }
    setLoading(false)
  }

  useEffect(() => {
    fetchData()
  }, [])

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
          <h1 className="text-2xl font-bold text-white mb-6">My Drive</h1>

          {loading ? (
            <p className="text-slate-400">Loading...</p>
          ) : (
            <>
              {/* FOLDERS */}
              {folders.length > 0 && (
                <div className="mb-6">
                  <p className="text-slate-400 text-sm mb-3">Folders</p>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                    {folders.map((f) => (
                      <div key={f.id}
                        className="bg-slate-800 border border-slate-700 rounded-lg p-4 hover:border-blue-600 cursor-pointer">
                        <p className="text-white text-sm">📁 {f.name}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* FILES */}
              <p className="text-slate-400 text-sm mb-3">Files</p>
              {files.length === 0 && folders.length === 0 ? (
                <div className="bg-slate-800 border border-slate-700 rounded-lg p-8 text-center">
                  <p className="text-slate-400">No files yet. Upload something!</p>
                </div>
              ) : files.length === 0 ? (
                <p className="text-slate-500 text-sm">No files</p>
              ) : (
                <div className="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden">
                  <table className="w-full text-sm">
                    <thead className="bg-slate-900 text-slate-400">
                      <tr>
                        <th className="text-left px-4 py-3">Name</th>
                        <th className="text-left px-4 py-3">Type</th>
                        <th className="text-left px-4 py-3">Size</th>
                      </tr>
                    </thead>
                    <tbody>
                      {files.map((file) => (
                        <tr key={file.id} className="border-t border-slate-700">
                          <td className="px-4 py-3 text-white">📄 {file.name}</td>
                          <td className="px-4 py-3 text-slate-400">{file.fileType || '-'}</td>
                          <td className="px-4 py-3 text-slate-400">{formatSize(file.fileSize)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default MyDrive