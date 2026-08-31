import { useState } from 'react'
import api from './api'

function ShareModal({ file, onClose }) {
  const [email, setEmail] = useState('')
  const [role, setRole] = useState('VIEWER')
  const [msg, setMsg] = useState('')
  const [publicLink, setPublicLink] = useState('')

  const shareWithUser = async () => {
    setMsg('')
    if (!email.trim()) return setMsg('Enter an email')
    try {
      await api.post('/api/shares', { fileId: file.id, email, role })
      setMsg(`✓ Shared with ${email}`)
      setEmail('')
    } catch (err) {
      setMsg(err.response?.data?.error || 'Could not share')
    }
  }

  const createLink = async () => {
    try {
      const res = await api.post('/api/shares/link', { fileId: file.id, expiryDays: 7 })
      const url = `${window.location.origin}/public/${res.data.token}`
      setPublicLink(url)
    } catch (err) {
      setMsg('Could not create link')
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      onClick={onClose}>
      <div className="bg-slate-800 border border-slate-700 rounded-lg p-6 w-full max-w-md"
        onClick={(e) => e.stopPropagation()}>
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-white font-semibold">Share "{file.name}"</h2>
          <button onClick={onClose} className="text-slate-400 hover:text-white">✕</button>
        </div>

        {/* SHARE WITH USER */}
        <p className="text-slate-400 text-sm mb-2">Share with a user</p>
        <input type="email" placeholder="Email" value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="w-full bg-slate-700 text-white rounded p-2 text-sm mb-2" />
        <div className="flex gap-2 mb-3">
          <select value={role} onChange={(e) => setRole(e.target.value)}
            className="bg-slate-700 text-white rounded p-2 text-sm">
            <option value="VIEWER">Viewer</option>
            <option value="EDITOR">Editor</option>
          </select>
          <button onClick={shareWithUser}
            className="flex-1 bg-blue-600 hover:bg-blue-700 text-white rounded p-2 text-sm">
            Share
          </button>
        </div>

        {msg && <p className="text-sm text-slate-300 mb-3">{msg}</p>}

        <hr className="border-slate-700 my-4" />

        {/* PUBLIC LINK */}
        <p className="text-slate-400 text-sm mb-2">Public link (expires in 7 days)</p>
        {publicLink ? (
          <div className="bg-slate-900 rounded p-2 text-xs text-blue-400 break-all">
            {publicLink}
          </div>
        ) : (
          <button onClick={createLink}
            className="w-full bg-slate-700 hover:bg-slate-600 text-white rounded p-2 text-sm">
            🔗 Create public link
          </button>
        )}
      </div>
    </div>
  )
}

export default ShareModal