# Christmas Gifts Tracker 🎄

A web application to track Christmas gifts for your family, including budget management.

## Project Structure

```
├── backend/          # Spring Boot REST API
├── frontend/         # React + TypeScript frontend
└── README.md
```

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

**Backend:**
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database (in-memory for local development)
- Lombok

**Frontend:**
- React 18
- TypeScript
- Vite
- Axios

## Development Notes

- The backend uses H2 in-memory database, so data will be lost on restart
- CORS is configured to allow requests from `http://localhost:5173` and `http://localhost:3000`
- The frontend proxies API requests to the backend during development

