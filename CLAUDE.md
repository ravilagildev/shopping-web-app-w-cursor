# CLAUDE.md

This file provides guidance for Claude Code when working with this codebase.

## Project Overview

Specialty Coffee Tracker - A full-stack web application for tracking coffee inventory, roasters, and freshness.

## Tech Stack

- **Backend**: Spring Boot 3.2.0, Java 17+, Spring Data JPA, H2 Database, JWT Auth
- **Frontend**: React 18, TypeScript 5.2, Vite 6.4, Axios

## Common Commands

### Backend (from `backend/` directory)
```bash
./mvnw spring-boot:run          # Run the backend server (port 8080)
./mvnw test                      # Run all tests
./mvnw package                   # Build JAR file
```

### Frontend (from `frontend/` directory)
```bash
npm install                      # Install dependencies
npm run dev                      # Start dev server (port 5173)
npm run build                    # Production build
```

## Architecture

```
backend/src/main/java/com/avilachehab/christmasgifts/
├── controller/     # REST endpoints
├── service/        # Business logic layer
├── repository/     # Spring Data JPA repositories
├── model/          # JPA entities (Coffee, Roaster)
├── dto/            # Data transfer objects
├── config/         # SecurityConfig, CORS
├── filter/         # JwtAuthenticationFilter
└── util/           # JwtUtil

frontend/src/
├── App.tsx         # Main component
├── Login.tsx       # Authentication
├── api.ts          # Axios client with JWT interceptors
└── types.ts        # TypeScript interfaces
```

## Data Model

- **Roaster** (1) → (N) **Coffee**
- **RoastLevel** enum: LIGHT, MEDIUM, MEDIUM_DARK, DARK

## API Endpoints

- `POST /api/auth/login` - Authentication
- `/api/roasters` - CRUD for roasters
- `/api/coffees` - CRUD + consume endpoint
- `GET /api/inventory/summary` - Inventory stats

## Development Notes

- H2 in-memory DB (resets on restart)
- H2 console: `http://localhost:8080/h2-console`
- Default credentials: admin/admin123
