import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Login from './Login'
import Register from './Register'
import MyDrive from './MyDrive'
import SharedWithMe from './SharedWithMe'

function ProtectedDrive() {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (!user) return <Navigate to="/login" />
  return <MyDrive />
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<ProtectedDrive />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
                <Route path="/shared" element={<SharedWithMe />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App