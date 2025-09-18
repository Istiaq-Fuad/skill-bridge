# SkillBridge - AI-Powered Talent Platform

SkillBridge is an intelligent AI-driven talent platform that revolutionizes the employment ecosystem in Bangladesh by helping users build optimized resumes, guiding them on career paths, providing personalized upskilling opportunities, and assisting employers in bias-free candidate shortlisting.

## Features

### For Job Seekers
- AI-powered resume generation and optimization
- Skill assessment and validation
- Personalized career path recommendations
- Interview preparation tools
- Job application tracking

### For Employers
- Intelligent candidate matching and ranking
- Automated resume parsing and skill extraction
- Job description optimization
- Talent pool building
- Advanced analytics and reporting
- Enhanced candidate ranking with detailed scoring

### For Admins
- Platform monitoring and management
- User account management
- Dispute resolution
- Analytics and reporting

## Technology Stack

### Backend
- **Spring Boot** - Main application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations and ORM
- **PostgreSQL** - Primary database for relational data
- **Redis** - Caching and session management
- **Apache Tika** - Document parsing (PDF, DOCX)
- **Mistral AI** - AI services integration

### Frontend
- **Next.js** - React framework for server-side rendering
- **TypeScript** - Type-safe JavaScript development
- **Tailwind CSS** - Utility-first CSS framework

## API Documentation

- [Resume Parsing API](backend/src/main/resources/docs/resume-parsing-api.md)
- [Enhanced Job Matching API](backend/src/main/resources/docs/enhanced-job-matching-api.md)

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- PostgreSQL
- Docker (optional, for containerized deployment)

### Environment Variables
Copy `.env.example` to `.env` and configure the required variables:

```bash
# Database Configuration
POSTGRES_USER=your_db_user
POSTGRES_PASSWORD=your_db_password
POSTGRES_DB=your_db_name
DB_HOST=localhost

# Mistral AI Configuration
MISTRAL_API_TOKEN=your_mistral_api_token

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
```

### Running the Application

#### Using Docker (Recommended)
```bash
# Build and start all services
docker-compose up --build
```

#### Manual Setup
```bash
# Navigate to backend directory
cd backend

# Build the application
mvn clean package

# Run the application
java -jar target/skillbridge-0.0.1-SNAPSHOT.jar
```

## Development

### Branching Strategy
- `main` - Production-ready code
- `employer-features` - Employer-specific features
- `ai-development` - AI-related features
- `resume-parsing-feature` - Resume parsing functionality
- `candidate-ranking-feature` - Enhanced candidate ranking functionality (current development)

### Building New Features
1. Create a new branch from the appropriate base branch
2. Implement your feature
3. Write tests if applicable
4. Submit a pull request for review

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.