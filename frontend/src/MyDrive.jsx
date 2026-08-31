import { useState, useEffect, useRef } from 'react'
import api from './api'
import Navbar from './Navbar'
import Sidebar from './Sidebar'
import ShareModal from './ShareModal'

function MyDrive() {
  const [files, setFiles] = useState([])
  const [folders, setFolders] = useState([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [showFolderInput, setShowFolderInput] = useState(false)
  const [folderName, setFolderName] = useState('')
  const [shareFile, setShareFile] = useState(null)
  const [search, setSearch] = useState('')
  const fileInputRef = useRef(null)

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

  // SEARCH (debounced)
  useEffect(() => {
    const timer = setTimeout(async () => {
      if (search.trim()) {
        try {
          const res = await api.get(`/api/files/search?query=${search}&page=0&size=50`)
          setFiles(res.data.files)
        } catch (err) {
          console.log(err)
        }
      } else {
        fetchData()
      }
    }, 400)
    return () => clearTimeout(timer)
  }, [search])

  const handleUpload = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    setUploading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      await api.post('/api/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      fetchData()
    } catch (err) {
      alert('Upload failed: ' + (err.response?.data?.error || err.message))
    }
    setUploading(false)
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const handleCreateFolder = async () => {
    if (!folderName.trim()) return
    try {
      await api.post('/api/folders', { name: folderName })
      setFolderName('')
      setShowFolderInput(false)
      fetchData()
    } catch (err) {
      alert('Could not create folder')
    }
  }

  const downloadFile = async (id) => {
    try {
      const res = await api.get(`/api/files/${id}/download`)
      window.open(res.data.url, '_blank')
    } catch (err) {
      alert('Could not download')
    }
  }

  const deleteFile = async (id) => {
    if (!confirm('Move this file to trash?')) return
    try {
      await api.delete(`/api/files/${id}`)
      fetchData()
    } catch (err) {
      alert('Could not delete')
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
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-white">My Drive</h1>
            <div className="flex gap-3">
              <button
                onClick={() => setShowFolderInput(!showFolderInput)}
                className="bg-slate-700 hover:bg-slate-600 text-white px-4 py-2 rounded text-sm">
                + New Folder
              </button>
              <button
                onClick={() => fileInputRef.current?.click()}
                disabled={uploading}
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded text-sm disabled:opacity-50">
                {uploading ? 'Uploading...' : '⬆️ Upload File'}
              </button>
              <input type="file" ref={fileInputRef} onChange={handleUpload} className="hidden" />
            </div>
          </div>

          <input
            type="text"
            placeholder="🔍 Search files..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-slate-800 text-white border border-slate-700 rounded px-3 py-2 text-sm mb-6"
          />

          {showFolderInput && (
            <div className="bg-slate-800 border border-slate-700 rounded-lg p-4 mb-6 flex gap-3">
              <input
                type="text"
                placeholder="Folder name"
                value={folderName}
                onChange={(e) => setFolderName(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleCreateFolder()}
                className="flex-1 bg-slate-700 text-white rounded p-2 text-sm"
                autoFocus
              />
              <button onClick={handleCreateFolder}
                className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded text-sm">
                Create
              </button>
            </div>
          )}

          {loading ? (
            <p className="text-slate-400">Loading...</p>
          ) : (
            <>
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

              <p className="text-slate-400 text-sm mb-3">Files</p>
              {files.length === 0 && folders.length === 0 ? (
                <div className="bg-slate-800 border border-slate-700 rounded-lg p-8 text-center">
                  <p className="text-slate-400">No files yet. Upload something!</p>
                </div>
              ) : files.length === 0 ? (
                <p className="text-slate-500 text-sm">No files found</p>
              ) : (
                <div className="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden">
                  <table className="w-full text-sm">
                    <thead className="bg-slate-900 text-slate-400">
                      <tr>
                        <th className="text-left px-4 py-3">Name</th>
                        <th className="text-left px-4 py-3">Type</th>
                        <th className="text-left px-4 py-3">Size</th>
                        <th className="text-left px-4 py-3">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {files.map((file) => (
                        <tr key={file.id} className="border-t border-slate-700">
                          <td className="px-4 py-3 text-white">📄 {file.name}</td>
                          <td className="px-4 py-3 text-slate-400">{file.fileType || '-'}</td>
                          <td className="px-4 py-3 text-slate-400">{formatSize(file.fileSize)}</td>
                          <td className="px-4 py-3">
                            <div className="flex gap-2">
                              <button onClick={() => downloadFile(file.id)}
                                className="text-blue-400 hover:text-blue-300 text-xs">Download</button>
                              <button onClick={() => setShareFile(file)}
                                className="text-green-400 hover:text-green-300 text-xs">Share</button>
                              <button onClick={() => deleteFile(file.id)}
                                className="text-red-400 hover:text-red-300 text-xs">Delete</button>
                            </div>
                          </td>
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

      {shareFile && <ShareModal file={shareFile} onClose={() => setShareFile(null)} />}
    </div>
  )
}

export default MyDrive