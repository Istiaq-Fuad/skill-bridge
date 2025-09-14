# SkillBridge

A modern job board platform that connects talented job seekers with employers, built with Spring Boot and Next.js.

## 🚀 Features

- **Multi-role Authentication**: Support for Job Seekers and Employers
- **Complete User Profiles**: Education, experience, skills, and portfolio management
- **Job Management**: Post, search, and filter job opportunities
- **Application System**: Apply for jobs with cover letters and resume uploads
- **Real-time Dashboard**: Role-based dashboards with analytics
- **Modern UI**: Clean, responsive design using shadcn/ui components
- **Secure Authentication**: JWT-based authentication with Spring Security

## 🏗️ Architecture

```
SkillBridge/
├── backend/                 # Spring Boot REST API
│   ├── src/main/java/
│   │   └── org/jobai/skillbridge/
│   │       ├── controller/  # REST Controllers
│   │       ├── service/     # Business Logic
│   │       ├── model/       # JPA Entities
│   │       ├── repo/        # Repositories
│   │       ├── config/      # Security & Configuration
│   │       └── util/        # Utilities (JWT, etc.)
│   └── src/main/resources/
│       └── application.properties
├── skillbridge-frontend/    # Next.js Frontend
│   ├── src/
│   │   ├── app/            # App Router pages
│   │   ├── components/     # React Components
│   │   ├── lib/            # API client & utilities
│   │   └── contexts/       # React Context providers
│   └── public/
└── docker-compose.yml      # Container orchestration
```

## 🛠️ Technology Stack

### Backend

- **Framework**: Spring Boot 3.5.5
- **Security**: Spring Security with JWT authentication
- **Database**: PostgreSQL (Neon)
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Maven
- **Java Version**: 21

### Frontend

- **Framework**: Next.js 15 with App Router
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **UI Components**: Radix UI + shadcn/ui
- **State Management**: React Context
- **HTTP Client**: Fetch API

### Database & Infrastructure

- **Database**: PostgreSQL (Neon Cloud)
- **Caching**: Redis (planned)
- **Search**: Elasticsearch (planned)
- **Container**: Docker & Docker Compose

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL database (Neon account recommended)

### Environment Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/Istiaq-Fuad/skill-bridge.git
   cd skill-bridge
   ```

2. **Create environment file**

   ```bash
   cp .env.example .env
   ```

3. **Configure environment variables**

   ```env
   # Database Configuration
   DATABASE_URL=postgresql://username:password@host:port/database
   PGUSER=your_username
   PGPASSWORD=your_password

   # Backend Configuration
   JWT_SECRET=your_jwt_secret_key
   JWT_EXPIRATION=86400
   SERVER_PORT=8080
   SPRING_PROFILES_ACTIVE=dev
   SPRING_JPA_HIBERNATE_DDL_AUTO=update
   SPRING_JPA_SHOW_SQL=false
   CORS_ALLOWED_ORIGINS=http://localhost:3000

   # Frontend Configuration
   NEXT_PUBLIC_API_URL=http://localhost:8080/api
   NODE_ENV=development
   ```

### Running with Docker (Recommended)

```bash
# Build and run all services
docker-compose up --build

# Run in detached mode
docker-compose up -d

# Stop services
docker-compose down
```

The application will be available at:

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

### Manual Setup

1. **Backend Setup**

   ```bash
   cd backend
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

2. **Frontend Setup**
   ```bash
   cd skillbridge-frontend
   npm install
   npm run dev
   ```

## 📚 API Documentation

### Authentication Endpoints

- `POST /api/users/register` - User registration
- `POST /api/users/login` - User login
- `GET /api/users/profile` - Get user profile

### Job Endpoints

- `GET /jobs` - Get all jobs
- `GET /jobs/{id}` - Get job by ID
- `GET /jobs/keyword/{keyword}` - Search jobs by keyword
- `POST /api/jobs` - Create job (Employers only)
- `PUT /api/jobs/{id}` - Update job
- `DELETE /api/jobs/{id}` - Delete job

### Application Endpoints

- `GET /api/applications` - Get user applications
- `POST /api/applications/apply/{jobId}` - Apply for job
- `PUT /api/applications/{id}/status` - Update application status
- `GET /api/applications/job/{jobId}` - Get job applications

### Profile Endpoints

- `GET /api/profile/education` - Get user education
- `POST /api/profile/education` - Add education
- `PUT /api/profile/education/{id}` - Update education
- `DELETE /api/profile/education/{id}` - Delete education
- Similar endpoints for experience, skills, and portfolio

## 🎯 User Roles

### Job Seekers

- Create and manage detailed profiles
- Search and filter job opportunities
- Apply for jobs with cover letters
- Track application status
- Manage education, experience, and skills
- Build portfolio showcase

### Employers

- Post and manage job listings
- Review job applications
- Search candidate profiles
- Manage company information
- Track hiring analytics

## 🔒 Security Features

- JWT-based stateless authentication
- Password encryption with BCrypt
- CORS protection
- Input validation and sanitization
- Role-based access control
- Secure API endpoints

## 📱 Frontend Features

- Responsive design for all devices
- Modern, intuitive user interface
- Real-time notifications with Sonner
- Form validation and error handling
- Loading states and skeleton screens
- Dark mode support (planned)

## 🧪 Testing

### Backend Tests

```bash
cd backend
./mvnw test
```

### Frontend Tests

```bash
cd skillbridge-frontend
npm run test
```

## 🚀 Deployment

### Production Build

1. **Backend**

   ```bash
   cd backend
   ./mvnw clean package -Pprod
   ```

2. **Frontend**
   ```bash
   cd skillbridge-frontend
   npm run build
   npm start
   ```

### Docker Production

```bash
# Production build with optimized containers
docker-compose -f docker-compose.prod.yml up --build -d
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Istiaq Fuad** - Full Stack Developer - [GitHub](https://github.com/Istiaq-Fuad)

## 📞 Support

For support, email support@skillbridge.dev or join our Slack channel.

## 🔮 Roadmap

- [ ] Advanced search with filters
- [ ] Company profiles and ratings
- [ ] Video interview integration
- [ ] AI-powered job matching
- [ ] Mobile application
- [ ] Analytics dashboard
- [ ] Email notifications
- [ ] Payment integration for premium features

---

Made with ❤️ by the SkillBridge Team
