# SecureAuth Frontend

A modern, production-ready React frontend for user authentication, built with React, TypeScript, and Tailwind CSS.

## Features

- 🔐 JWT-based authentication
- 🎨 Beautiful, responsive UI with light/dark mode
- ✅ Form validation with Zod schemas
- 🚀 React Query for data fetching and caching
- 🎯 Protected routes with role-based access
- 📱 Mobile-first responsive design
- ♿ Accessible components with ARIA labels

## Tech Stack

- **Framework**: React 18 + Vite + TypeScript
- **Styling**: Tailwind CSS + shadcn/ui
- **Routing**: React Router v6
- **State Management**: React Context + TanStack Query
- **Forms**: React Hook Form + Zod validation
- **HTTP Client**: Axios with interceptors
- **Theme**: next-themes for dark mode

## Prerequisites

- Node.js 18+ and npm
- Backend API running on `http://localhost:9090`

## Getting Started

### 1. Install Dependencies

```bash
npm install
```

### 2. Environment Setup

Create a `.env.development` file:

```env
VITE_API_BASE_URL=http://localhost:9090
```

### 3. Run Development Server

```bash
npm run dev
```

The app will be available at `http://localhost:6969`

## Available Scripts

- `npm run dev` - Start development server on port 6969
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Project Structure

```
src/
├── components/       # Reusable UI components
│   ├── ui/          # shadcn/ui components
│   ├── Navigation.tsx
│   ├── ThemeToggle.tsx
│   └── ProtectedRoute.tsx
├── contexts/        # React contexts
│   └── AuthContext.tsx
├── pages/           # Route pages
│   ├── Landing.tsx
│   ├── Login.tsx
│   ├── Register.tsx
│   └── Dashboard.tsx
├── lib/             # Utilities
│   ├── api.ts       # Axios instance
│   └── utils.ts
├── hooks/           # Custom hooks
└── App.tsx          # Main app component
```

## API Integration

The frontend connects to a Spring Boot backend with the following endpoints:

### POST /login
- Content-Type: `application/x-www-form-urlencoded`
- Body: `username=<string>&password=<string>`
- Response: `{ "token": "<JWT>" }`

### POST /register
- Content-Type: `application/json`
- Body:
```json
{
  "username": "alice",
  "password": "Password123!",
  "email": "alice@example.com",
  "roles": [{ "name": "ROLE_USER" }]
}
```

## Authentication Flow

1. User logs in via `/login`
2. Backend returns JWT token
3. Token stored in localStorage
4. Axios interceptor adds token to all requests
5. Protected routes check authentication status
6. Token decoded to display user info

## Deployment

### Build for Production

```bash
npm run build
```

The build output will be in the `dist/` directory.

### Deploy to Lovable

Click the **Publish** button in the Lovable interface to deploy your app.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## License

MIT
