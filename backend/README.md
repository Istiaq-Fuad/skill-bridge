# SkillBridge Backend

A robust REST API built with Spring Boot that powers the SkillBridge job board platform.

## 🚀 Overview

The SkillBridge backend provides a comprehensive API for managing users, job postings, applications, and user profiles. Built with modern Spring Boot practices, it offers secure authentication, data persistence, and scalable architecture.

## 🏗️ Architecture

```
backend/
├── src/main/java/org/jobai/skillbridge/
│   ├── SkillBridgeApplication.java    # Main application class
│   ├── controller/                    # REST Controllers
│   │   ├── UserController.java        # User management & auth
│   │   ├── JobController.java         # Job listings
│   │   ├── ApplicationController.java # Job applications
│   │   └── ProfileController.java     # User profiles
│   ├── service/                       # Business Logic Layer
│   │   ├── UserService.java          # User operations
│   │   ├── JobService.java           # Job operations
│   │   ├── ApplicationService.java    # Application logic
│   │   └── ProfileService.java       # Profile management
│   ├── model/                         # JPA Entity Models
│   │   ├── User.java                 # User entity
│   │   ├── JobPost.java              # Job posting entity
│   │   ├── JobApplication.java       # Application entity
│   │   ├── Education.java            # Education records
│   │   ├── Experience.java           # Work experience
│   │   ├── Skill.java                # User skills
│   │   └── Portfolio.java            # Portfolio items
│   ├── repo/                          # Data Access Layer
│   │   ├── UserRepository.java
│   │   ├── JobRepo.java
│   │   ├── JobApplicationRepository.java
│   │   ├── EducationRepository.java
│   │   ├── ExperienceRepository.java
│   │   ├── SkillRepository.java
│   │   └── PortfolioRepository.java
│   ├── config/                        # Configuration Classes
│   │   ├── SecurityConfig.java       # Spring Security setup
│   │   ├── WebConfig.java            # CORS & Web configuration
│   │   └── JwtConfig.java            # JWT configuration
│   └── util/                          # Utility Classes
│       ├── JwtUtil.java              # JWT token operations
│       └── PasswordUtil.java         # Password utilities
└── src/main/resources/
    ├── application.properties         # Configuration properties
    └── static/                       # Static resources
```

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.5
- **Security**: Spring Security 6
- **Authentication**: JWT (JSON Web Tokens)
- **Database**: PostgreSQL with Neon Cloud
- **ORM**: Spring Data JPA + Hibernate
- **Caching**: Spring Data Redis (configured)
- **Search**: Spring Data Elasticsearch (configured)
- **Build Tool**: Maven
- **Java Version**: 21 LTS
- **Validation**: Bean Validation (Hibernate Validator)
- **Logging**: SLF4J with Logback
- **Testing**: JUnit 5, Spring Boot Test

## 📦 Key Dependencies

```xml
<!-- Core Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- JWT Authentication -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- Caching & Search -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>

<!-- Utilities -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## 🗄️ Database Schema

### Users Table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    bio TEXT,
    phone_number VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    country VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE
);
```

### Job Posts Table

```sql
CREATE TABLE job_post (
    post_id INTEGER PRIMARY KEY,
    post_profile VARCHAR(255),
    post_desc TEXT,
    req_experience INTEGER,
    post_tech_stack TEXT[]
);
```

### Job Applications Table

```sql
CREATE TABLE job_applications (
    id BIGSERIAL PRIMARY KEY,
    job_post_id INTEGER REFERENCES job_post(post_id),
    user_id BIGINT REFERENCES users(id),
    applied_at TIMESTAMP,
    status VARCHAR(50),
    cover_letter TEXT,
    resume_url VARCHAR(500)
);
```

### Profile Tables

```sql
-- Education
CREATE TABLE educations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    institution VARCHAR(255),
    degree VARCHAR(255),
    field_of_study VARCHAR(255),
    start_date DATE,
    end_date DATE,
    grade VARCHAR(50),
    description TEXT
);

-- Experience
CREATE TABLE experiences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    company VARCHAR(255),
    position VARCHAR(255),
    description TEXT,
    start_date DATE,
    end_date DATE,
    currently_working BOOLEAN
);

-- Skills
CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    name VARCHAR(255),
    category VARCHAR(100),
    proficiency_level INTEGER
);

-- Portfolio
CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255),
    description TEXT,
    url VARCHAR(500),
    media_type VARCHAR(50)
);
```

## 🔗 API Endpoints

### Authentication & User Management

```
POST   /api/users/register     # Register new user
POST   /api/users/login        # User login
GET    /api/users              # Get all users (admin)
GET    /api/users/{id}         # Get user by ID
GET    /api/users/username/{username}  # Get user by username
PUT    /api/users/{id}         # Update user
DELETE /api/users/{id}         # Delete user
```

### Job Management

```
GET    /home                   # Welcome message
GET    /jobs                   # Get all jobs
GET    /jobs/keyword/{keyword} # Search jobs by keyword
POST   /api/jobs               # Create new job
PUT    /api/jobs/{id}          # Update job
DELETE /api/jobs/{id}          # Delete job
```

### Job Applications

```
GET    /api/applications              # Get user's applications
POST   /api/applications/apply/{jobId} # Apply for job
PUT    /api/applications/{id}/status   # Update application status
DELETE /api/applications/{id}          # Delete application
GET    /api/applications/job/{jobId}   # Get job applications (employers)
```

### User Profiles

```
# Education
GET    /api/profile/education     # Get user education
POST   /api/profile/education     # Add education
PUT    /api/profile/education/{id} # Update education
DELETE /api/profile/education/{id} # Delete education

# Experience
GET    /api/profile/experience     # Get user experience
POST   /api/profile/experience     # Add experience
PUT    /api/profile/experience/{id} # Update experience
DELETE /api/profile/experience/{id} # Delete experience

# Skills
GET    /api/profile/skills     # Get user skills
POST   /api/profile/skills     # Add skill
PUT    /api/profile/skills/{id} # Update skill
DELETE /api/profile/skills/{id} # Delete skill

# Portfolio
GET    /api/profile/portfolio     # Get user portfolio
POST   /api/profile/portfolio     # Add portfolio item
PUT    /api/profile/portfolio/{id} # Update portfolio
DELETE /api/profile/portfolio/{id} # Delete portfolio item
```

## 🔒 Security Configuration

### JWT Authentication

- Stateless authentication using JSON Web Tokens
- Token expiration: 24 hours (configurable)
- Secure token generation with HS512 algorithm
- Custom JWT filter for request authentication

### Password Security

- BCrypt password encoding
- Minimum password requirements enforced
- Account lockout protection (planned)

### CORS Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

## ⚙️ Configuration

### Environment Variables

```properties
# Database Configuration
DATABASE_URL=postgresql://username:password@host:port/database
PGUSER=your_database_username
PGPASSWORD=your_database_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400

# Server Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# JPA Configuration
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_FORMAT_SQL=true

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### Application Profiles

#### Development (`application-dev.properties`)

```properties
spring.jpa.show-sql=true
logging.level.org.springframework.security=DEBUG
logging.level.org.jobai.skillbridge=DEBUG
```

#### Production (`application-prod.properties`)

```properties
spring.jpa.show-sql=false
logging.level.org.springframework.security=WARN
logging.level.org.jobai.skillbridge=INFO
server.compression.enabled=true
```

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- PostgreSQL database (Neon recommended)

### Local Development Setup

1. **Clone and navigate to backend**

   ```bash
   cd backend
   ```

2. **Configure environment variables**

   ```bash
   # Create .env file in project root
   cp ../.env.example ../.env
   # Edit .env with your database credentials
   ```

3. **Run the application**

   ```bash
   # Using Maven wrapper (recommended)
   ./mvnw spring-boot:run

   # Or using Maven directly
   mvn spring-boot:run

   # Or using IDE: Run SkillBridgeApplication.java
   ```

4. **Verify installation**
   ```bash
   curl http://localhost:8080/home
   # Should return: "Welcome to the Job Portal"
   ```

### Building for Production

```bash
# Clean and package
./mvnw clean package -Pprod

# Run the JAR file
java -jar target/skillBridge-0.0.1-SNAPSHOT.jar
```

### Docker Build

```bash
# Build Docker image
docker build -t skillbridge-backend .

# Run container
docker run -p 8080:8080 --env-file .env skillbridge-backend
```

## 🧪 Testing

### Unit Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserServiceTest

# Run tests with coverage
./mvnw test jacoco:report
```

### Integration Tests

```bash
# Run integration tests
./mvnw test -Dtest=**/*IntegrationTest

# Test with embedded database
./mvnw test -Dspring.profiles.active=test
```

### API Testing with cURL

```bash
# Register a new user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123","role":"JOB_SEEKER"}'

# Login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Get jobs (with JWT token)
curl -X GET http://localhost:8080/jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📊 Performance Considerations

### Database Optimization

- Proper indexing on frequently queried columns
- Lazy loading for entity relationships
- Connection pooling with HikariCP
- Query optimization with JPA hints

### Caching Strategy

- Redis integration for session management
- Application-level caching for frequently accessed data
- Database query result caching

### Security Best Practices

- Input validation and sanitization
- SQL injection prevention with parameterized queries
- Rate limiting (planned)
- Request size limitations

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Issues**

   ```
   Solution: Verify DATABASE_URL, PGUSER, and PGPASSWORD in .env file
   ```

2. **JWT Token Errors**

   ```
   Solution: Check JWT_SECRET configuration and token expiration
   ```

3. **CORS Errors**

   ```
   Solution: Verify CORS_ALLOWED_ORIGINS matches frontend URL
   ```

4. **Port Already in Use**
   ```bash
   # Change port in application.properties or .env
   SERVER_PORT=8081
   ```

### Logging Configuration

```properties
# Enable SQL logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Enable Spring Security logging
logging.level.org.springframework.security=DEBUG

# Application logging
logging.level.org.jobai.skillbridge=DEBUG
```

## 🤝 Contributing

1. Follow Spring Boot best practices
2. Write unit tests for new features
3. Use proper exception handling
4. Follow RESTful API conventions
5. Update documentation for API changes

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://spring.io/projects/spring-security)
- [Spring Data JPA Guide](https://spring.io/projects/spring-data-jpa)
- [JWT.io](https://jwt.io/) - JWT token debugger
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

Built with ❤️ using Spring Boot
