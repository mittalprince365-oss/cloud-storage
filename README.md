# ☁️ Cloud Based Meta File Storage Service

A full-stack cloud file storage and sharing application inspired by Google Drive. Users can securely upload, organize, search, and share files with fine-grained access control.

🌍 **Live Demo:** https://cloud-storage-eight-psi.vercel.app
🔗 **API:** https://cloud-storage-api-yqy1.onrender.com

## Features

- Secure user authentication (JWT + BCrypt password hashing)
- File upload & download (cloud storage)
- Nested folder management
- File sharing with roles (Viewer / Editor)
- Public share links with expiry
- Search with pagination
- Trash & restore (soft delete)

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React (Vite), Tailwind CSS, React Router, Axios |
| Backend | Java 21, Spring Boot, Spring Security, Spring Data JPA (Hibernate) |
| Database | PostgreSQL (Supabase) |
| Storage | Supabase Storage |
| Auth | JWT + BCrypt |

## Architecture

React frontend → Spring Boot REST API → PostgreSQL (metadata) + Supabase Storage (files). Every request carries a JWT token verified by the backend. File ownership and sharing permissions are enforced on the server.

## Security Highlights

- Passwords hashed with BCrypt (never stored in plain text)
- JWT-based authentication on every protected endpoint
- Ownership checks — users can only access their own files
- Correct file access enforced server-side, not on the client

## Deployment

- Frontend deployed on Vercel
- Backend deployed on Render (Docker)
- Database & file storage on Supabase

## Running Locally

1. **Backend:** create `application-local.properties` with your database and Supabase credentials, then run:
2. **Frontend:** create `.env` with `VITE_API_URL=http://localhost:8080`, then run:


## Database Schema

users, folders, files, shares, link_shares — connected with foreign keys and ownership references.