# Christmas Gifts Tracker 🎄

A web application to track Christmas gifts for your family, including budget management.

## Project Structure

```
├── backend/          # Spring Boot REST API
│   ├── src/main/java/com/avilachehab/christmasgifts/
│   │   ├── controller/    # REST endpoints
│   │   ├── service/       # Business logic layer
│   │   ├── repository/   # Data access layer
│   │   ├── model/        # JPA entities
│   │   ├── dto/          # Data transfer objects
│   │   ├── config/       # Spring configuration
│   │   ├── filter/       # JWT authentication filter
│   │   └── util/         # Utility classes (JWT)
│   └── pom.xml
├── frontend/         # React + TypeScript frontend
│   ├── src/
│   │   ├── App.tsx       # Main application component
│   │   ├── Login.tsx     # Authentication component
│   │   ├── api.ts        # API client with interceptors
│   │   └── types.ts      # TypeScript type definitions
│   └── package.json
├── deploy.sh         # Automated deployment script
├── deploy.env.example # Deployment configuration template
└── README.md
```

## Architecture Overview

This application follows a **three-tier architecture** with clear separation of concerns:

### Backend Architecture (Spring Boot)

**Layered Architecture:**
- **Controller Layer** (`controller/`): Handles HTTP requests/responses, validates input, delegates to services
- **Service Layer** (`service/`): Contains business logic, orchestrates data operations
- **Repository Layer** (`repository/`): Data access abstraction using Spring Data JPA
- **Model Layer** (`model/`): JPA entities representing database schema
- **DTO Layer** (`dto/`): Data Transfer Objects for API responses (decouples internal models from API contracts)

**Design Decisions:**
- **Spring Boot 3.2.0**: Modern framework with auto-configuration, reducing boilerplate
- **Spring Data JPA**: Simplifies database operations with repository pattern
- **Lombok**: Reduces boilerplate code (getters, setters, constructors)
- **H2 Database**: In-memory for local dev, can be switched to file-based or PostgreSQL for production
- **JWT Authentication**: Stateless authentication suitable for REST APIs and scalable deployments
- **DTO Pattern**: Separates internal entity structure from API contract, allows for future API versioning

### Frontend Architecture (React + TypeScript)

**Component Structure:**
- **Functional Components**: Modern React with hooks (useState, useEffect)
- **TypeScript**: Type safety throughout the application
- **Axios Interceptors**: Centralized token management and error handling
- **Component Composition**: Reusable form components (PersonForm, GiftForm)

**Design Decisions:**
- **React 18**: Latest stable version with concurrent features
- **Vite**: Fast build tool and dev server (replaces Create React App)
- **TypeScript**: Type safety catches errors at compile time
- **Axios**: HTTP client with interceptors for automatic token injection
- **Local Storage**: Stores JWT token for persistence across page refreshes
- **No State Management Library**: Simple useState/useEffect sufficient for this app's scope

### Security Architecture

**Authentication Flow:**
1. User submits credentials to `/api/auth/login`
2. Backend validates against configured credentials (env vars)
3. JWT token generated with 24-hour expiration
4. Frontend stores token in localStorage
5. All subsequent requests include token in `Authorization: Bearer <token>` header
6. `JwtAuthenticationFilter` validates token on each request

**Design Decisions:**
- **JWT over Sessions**: Stateless, scalable, works well with containerized deployments
- **Single User Authentication**: Simple credential-based auth (can be extended to multi-user)
- **CORS Configuration**: Allows frontend origin, restricts cross-origin requests
- **Stateless Sessions**: No server-side session storage, reduces memory footprint
- **Password Encoding**: BCrypt support for hashed passwords (optional)

### Data Persistence Strategy

**Current Implementation:**
- **H2 In-Memory**: Default for local development (data lost on restart)
- **H2 File-Based**: Optional via `SPRING_DATASOURCE_URL=jdbc:h2:/data/christmasgifts` (persists data)
- **Production Ready**: Can switch to PostgreSQL/MySQL by changing datasource configuration

**Design Decisions:**
- **H2 for Development**: Zero configuration, fast startup, perfect for local testing
- **JPA with Hibernate**: Database-agnostic ORM, easy to switch databases
- **Auto Schema Management**: `ddl-auto=update` creates/updates schema automatically
- **No Migration Tool**: Simple enough for manual schema changes (Flyway/Liquibase can be added later)

### Deployment Architecture

**Current Setup (EC2 + S3):**
- **Backend**: Docker container on EC2 instance
- **Frontend**: Static files served from S3 bucket
- **Separation**: Frontend and backend can be deployed independently

**Design Decisions:**
- **Containerization**: Docker ensures consistent runtime environment
- **Static Frontend**: React build produces static files, perfect for S3/CloudFront
- **Separate Deployments**: Frontend and backend decoupled, can scale independently
- **Cost-Effective**: EC2 t3.micro (free tier eligible) + S3 (pay per storage/requests)
- **No Load Balancer**: Single instance sufficient for personal use (can add ALB later)

**Future Enhancements:**
- CloudFront CDN for frontend (HTTPS, caching, global distribution)
- RDS for production database (PostgreSQL/MySQL)
- Application Load Balancer for high availability
- Auto-scaling groups for multiple backend instances

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm

## Running the Application

### Backend (Spring Boot)

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   Or if you have Maven installed:
   ```bash
   mvn spring-boot:run
   ```

3. The backend will start on `http://localhost:8080`

4. You can access the H2 database console at `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:christmasgifts`
   - Username: `sa`
   - Password: (leave empty)

### Frontend (React)

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

4. The frontend will start on `http://localhost:5173`

### Authentication

- Default credentials (change before deploying):
  - Username: `admin`
  - Password: `admin123`
- Configure via environment variables for backend:
  - `APP_USERNAME`, `APP_PASSWORD`, `JWT_SECRET`

## Features

- ✅ Add and manage family members (persons)
- ✅ Add gifts for each person
- ✅ Track total budget and spending
- ✅ View remaining budget
- ✅ Edit and delete persons and gifts
- ✅ Beautiful, responsive UI

## API Endpoints

### Persons
- `GET /api/persons` - Get all persons
- `GET /api/persons/{id}` - Get person by ID
- `POST /api/persons` - Create a new person
- `PUT /api/persons/{id}` - Update a person
- `DELETE /api/persons/{id}` - Delete a person

### Gifts
- `GET /api/gifts` - Get all gifts
- `GET /api/gifts/{id}` - Get gift by ID
- `GET /api/gifts/person/{personId}` - Get gifts for a person
- `POST /api/gifts` - Create a new gift
- `PUT /api/gifts/{id}` - Update a gift
- `DELETE /api/gifts/{id}` - Delete a gift

### Budget
- `GET /api/budget/summary?totalBudget={amount}` - Get budget summary

## Technology Stack

### Backend
- **Spring Boot 3.2.0**: Application framework with auto-configuration
- **Spring Data JPA**: Data persistence abstraction
- **Spring Security**: Authentication and authorization
- **H2 Database**: Embedded database (in-memory or file-based)
- **JWT (jjwt 0.12.3)**: Token-based authentication
- **Lombok**: Code generation to reduce boilerplate
- **Bean Validation**: Input validation on DTOs

### Frontend
- **React 18**: UI library with hooks
- **TypeScript 5.2**: Type-safe JavaScript
- **Vite 6.4**: Build tool and dev server
- **Axios 1.6**: HTTP client with interceptors

### Deployment
- **Docker**: Containerization for backend
- **AWS EC2**: Compute for backend container
- **AWS S3**: Static hosting for frontend
- **AWS CLI**: Infrastructure provisioning and deployment

## API Design

**RESTful Principles:**
- Resource-based URLs (`/api/persons`, `/api/gifts`)
- HTTP methods map to operations (GET, POST, PUT, DELETE)
- JSON request/response format
- Stateless (no server-side sessions)

**Error Handling:**
- HTTP status codes (200, 201, 400, 401, 404, 500)
- Validation errors return 400 with details
- Authentication failures return 401
- Generic error messages to avoid information leakage

**DTO Pattern:**
- Separate DTOs from entities to control API contract
- DTOs include computed fields (e.g., `totalSpent` on PersonDto)
- Allows API evolution without changing internal models

## Development Notes

### Local Development
- Backend uses H2 in-memory database by default (data lost on restart)
- CORS configured for `http://localhost:5173` and `http://localhost:3000`
- Frontend Vite dev server proxies API requests to backend
- H2 console available at `http://localhost:8080/h2-console` for database inspection

### Data Persistence Options
1. **In-Memory (Default)**: Fastest, no persistence
   ```properties
   spring.datasource.url=jdbc:h2:mem:christmasgifts
   ```

2. **File-Based H2**: Persists to disk, survives restarts
   ```properties
   spring.datasource.url=jdbc:h2:file:/data/christmasgifts
   ```

3. **PostgreSQL (Production)**: Replace H2 dependency, update datasource config
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/christmasgifts
   spring.datasource.username=postgres
   spring.datasource.password=password
   ```

### Environment Variables
- `APP_USERNAME`: Login username (default: admin)
- `APP_PASSWORD`: Login password (default: admin123)
- `JWT_SECRET`: Secret key for JWT signing (must be at least 256 bits)
- `JWT_EXPIRATION`: Token expiration in milliseconds (default: 86400000 = 24 hours)

### Security Considerations
- Change default credentials before deployment
- Use strong, random `JWT_SECRET` in production
- Consider HTTPS for production (CloudFront + ACM certificate)
- Restrict EC2 security group to specific IPs if possible
- Use AWS Secrets Manager for sensitive configuration in production

