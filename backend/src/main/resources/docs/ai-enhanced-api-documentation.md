# SkillBridge AI-Enhanced API Documentation

## Overview

This document provides comprehensive documentation for the AI-enhanced SkillBridge platform API endpoints. The platform now includes advanced AI capabilities for intelligent job descriptions, resume parsing, advanced matching, and administrative analytics.

## Base URL

```
http://localhost:8080/api
```

## Authentication

Most endpoints require JWT authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Role-Based Access Control

- **ADMIN**: Full platform access including analytics and moderation
- **EMPLOYER**: Job posting, candidate management, and employer-specific features
- **JOB_SEEKER**: Job searching, applications, and profile management

---

## 1. Intelligent Job Description API

### 1.1 Generate Job Description

**POST** `/intelligent-job-description/generate`

- **Description**: Generate a complete job description using AI
- **Authorization**: Required (EMPLOYER role)
- **Content-Type**: `application/json`

**Request Body:**

```json
{
  "jobTitle": "Senior Software Engineer",
  "company": "Tech Solutions Inc",
  "industry": "Technology",
  "experienceLevel": "senior",
  "location": "San Francisco, CA",
  "employmentType": "full-time",
  "additionalRequirements": "React, Node.js experience preferred"
}
```

**Response:**

```json
{
  "content": "Generated job description text...",
  "suggestedSkills": ["React", "Node.js", "JavaScript"],
  "salaryRange": {
    "min": 120000,
    "max": 180000,
    "currency": "USD"
  },
  "generatedAt": "2024-01-15T10:30:00"
}
```

### 1.2 Optimize Job Description

**POST** `/intelligent-job-description/optimize`

- **Description**: Improve an existing job description for better candidate attraction
- **Authorization**: Required (EMPLOYER role)

**Request Body:**

```json
{
  "jobDescription": "Existing job description text to optimize...",
  "targetAudience": "senior developers",
  "optimizationGoals": ["increase_applications", "improve_quality"]
}
```

### 1.3 Suggest Skills

**POST** `/intelligent-job-description/suggest-skills`

- **Description**: Get AI-powered skill suggestions for a job posting
- **Authorization**: Required (EMPLOYER role)

**Request Body:**

```json
{
  "jobTitle": "Frontend Developer",
  "industry": "E-commerce",
  "experienceLevel": "mid"
}
```

**Response:**

```json
{
  "content": "Skill analysis and recommendations...",
  "requiredSkills": ["HTML", "CSS", "JavaScript", "React"],
  "preferredSkills": ["TypeScript", "Redux", "Jest"],
  "emergingSkills": ["Next.js", "GraphQL"]
}
```

### 1.4 Suggest Salary Range

**POST** `/intelligent-job-description/suggest-salary`

- **Description**: Get market-based salary recommendations
- **Authorization**: Required (EMPLOYER role)

**Request Body:**

```json
{
  "jobTitle": "Data Scientist",
  "location": "New York, NY",
  "experienceLevel": "senior",
  "industry": "Finance"
}
```

---

## 2. Resume Parsing API

### 2.1 Parse Resume

**POST** `/resume-parsing/parse`

- **Description**: Extract structured data from resume documents
- **Authorization**: Required
- **Content-Type**: `multipart/form-data`

**Request:**

- File upload (PDF, DOC, DOCX, TXT supported)
- Max file size: 10MB

**Response:**

```json
{
  "content": "Parsed resume analysis...",
  "extractedData": {
    "personalInfo": {
      "name": "John Doe",
      "email": "john.doe@email.com",
      "phone": "+1-555-0123",
      "location": "Seattle, WA"
    },
    "skills": ["Python", "Machine Learning", "SQL"],
    "experience": [
      {
        "title": "Senior Data Analyst",
        "company": "DataCorp",
        "duration": "2020-2023",
        "description": "Led data analysis projects..."
      }
    ],
    "education": [
      {
        "degree": "MS Computer Science",
        "institution": "University of Washington",
        "year": "2020"
      }
    ]
  }
}
```

### 2.2 Analyze Resume Quality

**POST** `/resume-parsing/analyze-quality`

- **Description**: Get AI-powered resume quality assessment
- **Authorization**: Required

**Request Body:**

```json
{
  "resumeText": "Complete resume text content..."
}
```

**Response:**

```json
{
  "content": "Resume quality analysis and improvement suggestions...",
  "qualityScore": 8.5,
  "strengths": ["Clear work history", "Relevant skills"],
  "improvements": ["Add quantified achievements", "Include portfolio links"]
}
```

### 2.3 Match Resume to Jobs

**POST** `/resume-parsing/match-jobs`

- **Description**: Find matching jobs for a parsed resume
- **Authorization**: Required (JOB_SEEKER role)

---

## 3. Advanced Job Matching API

### 3.1 Find Matching Candidates

**GET** `/advanced-matching/candidates/{jobId}?limit=10`

- **Description**: Get AI-powered candidate recommendations for a job
- **Authorization**: Required (EMPLOYER role)

**Response:**

```json
{
  "jobId": 123,
  "jobTitle": "Software Engineer",
  "recommendedCandidates": [
    {
      "userId": 456,
      "username": "jane_developer",
      "email": "jane@email.com",
      "compatibilityScore": 0.92,
      "skillMatchScore": 0.95,
      "experienceMatchScore": 0.88,
      "locationMatchScore": 0.9
    }
  ],
  "totalRecommendations": 25
}
```

### 3.2 Find Matching Jobs

**GET** `/advanced-matching/jobs/{userId}?limit=10`

- **Description**: Get personalized job recommendations for a user
- **Authorization**: Required (JOB_SEEKER role)

**Response:**

```json
{
  "userId": 456,
  "username": "jane_developer",
  "recommendedJobs": [
    {
      "jobId": 789,
      "title": "Senior Frontend Developer",
      "company": "InnovateTech",
      "compatibilityScore": 0.89,
      "skillMatchScore": 0.92,
      "experienceMatchScore": 0.85,
      "locationMatchScore": 0.9
    }
  ],
  "totalRecommendations": 15
}
```

### 3.3 Update Learning Profile

**POST** `/advanced-matching/learning/update`

- **Description**: Update user preferences for improved matching
- **Authorization**: Required

**Request Body:**

```json
{
  "userId": 456,
  "preferences": {
    "preferredLocation": "Remote",
    "salaryRange": { "min": 80000, "max": 120000 },
    "workType": "remote",
    "industries": ["technology", "finance"]
  },
  "feedback": {
    "interestedJobs": [123, 456],
    "rejectedJobs": [789],
    "appliedJobs": [234]
  }
}
```

---

## 4. Admin Analytics API

### 4.1 Analytics Overview

**GET** `/admin/analytics/overview`

- **Description**: Get comprehensive platform analytics
- **Authorization**: Required (ADMIN role)

**Response:**

```json
{
  "totalUsers": 1250,
  "totalJobs": 890,
  "usersByRole": {
    "JOB_SEEKER": 1000,
    "EMPLOYER": 200,
    "ADMIN": 5
  },
  "jobsByEmployer": {
    "TechCorp": 45,
    "StartupXYZ": 23
  }
}
```

### 4.2 Platform Trends

**GET** `/admin/analytics/trends?days=30`

- **Description**: Get platform activity trends over time
- **Authorization**: Required (ADMIN role)

### 4.3 AI-Powered Platform Insights

**POST** `/admin/analytics/insights`

- **Description**: Generate AI-powered platform insights
- **Authorization**: Required (ADMIN role)

**Request Body:**

```json
{
  "totalUsers": 1250,
  "totalJobs": 890,
  "usersByRole": { "JOB_SEEKER": 1000, "EMPLOYER": 200 }
}
```

**Response:**

```json
{
  "content": "AI-generated insights about platform performance, growth trends, and strategic recommendations..."
}
```

### 4.4 Strategic Recommendations

**POST** `/admin/analytics/recommendations`

- **Description**: Get AI-powered strategic recommendations
- **Authorization**: Required (ADMIN role)

### 4.5 Content Moderation

**POST** `/admin/moderate/job/{jobId}`

- **Description**: AI-powered content moderation for job postings
- **Authorization**: Required (ADMIN role)

### 4.6 Platform Health Check

**GET** `/admin/health/detailed`

- **Description**: Comprehensive platform health metrics
- **Authorization**: Required (ADMIN role)

---

## 5. Enhanced Employer API

### 5.1 Enhanced Dashboard

**GET** `/employers/dashboard-enhanced`

- **Description**: Enhanced employer dashboard with AI insights
- **Authorization**: Required (EMPLOYER role)

**Response:**

```json
{
  "totalJobs": 15,
  "totalApplications": 245,
  "pendingApplications": 67,
  "topPerformingJobs": [
    {
      "jobId": 123,
      "title": "Senior Developer",
      "applications": 45
    }
  ],
  "aiRecommendations": "AI-generated tips for improving hiring performance..."
}
```

### 5.2 Job Performance Analysis

**POST** `/employers/jobs/{jobId}/analyze-performance`

- **Description**: AI-powered analysis of individual job posting performance
- **Authorization**: Required (EMPLOYER role)

**Response:**

```json
{
  "content": "Detailed analysis of job performance including application volume assessment, candidate engagement patterns, and improvement recommendations..."
}
```

### 5.3 Recommended Candidates

**GET** `/employers/jobs/{jobId}/recommended-candidates`

- **Description**: Get AI-recommended candidates for a specific job
- **Authorization**: Required (EMPLOYER role)

### 5.4 Application Insights

**POST** `/employers/analytics/application-insights`

- **Description**: AI-powered insights across all employer applications
- **Authorization**: Required (EMPLOYER role)

---

## 6. Error Responses

### Standard Error Format

```json
{
  "error": "Error type",
  "message": "Detailed error description",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/endpoint"
}
```

### Common HTTP Status Codes

- **200 OK**: Successful request
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid request data
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **429 Too Many Requests**: Rate limit exceeded
- **500 Internal Server Error**: Server-side error
- **503 Service Unavailable**: AI service temporarily unavailable

---

## 7. Rate Limiting

### Current Limits

- **AI-powered endpoints**: 100 requests per hour per user
- **Standard endpoints**: 1000 requests per hour per user
- **File uploads**: 50 requests per hour per user

### Rate Limit Headers

```
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642248000
X-RateLimit-Limit: 100
```

---

## 8. AI Service Configuration

### Supported AI Features

- **Text Generation**: Job descriptions, recommendations, insights
- **Content Analysis**: Resume parsing, job matching, quality assessment
- **Predictive Analytics**: Candidate scoring, performance forecasting
- **Natural Language Processing**: Content moderation, sentiment analysis

### AI Response Quality

- All AI responses include confidence scores when applicable
- Responses are cached for 30 minutes to improve performance
- Fallback mechanisms ensure service availability

---

## 9. Data Privacy and Security

### Data Handling

- All uploaded documents are processed securely and deleted after parsing
- Personal information is encrypted at rest and in transit
- AI processing is performed on anonymized data where possible

### GDPR Compliance

- Users can request data deletion through standard API endpoints
- Data retention policies are enforced automatically
- Consent tracking is integrated into all AI-powered features

---

## 10. Integration Examples

### JavaScript/Frontend Integration

```javascript
// Generate job description
const response = await fetch("/api/intelligent-job-description/generate", {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    jobTitle: "Software Engineer",
    company: "TechCorp",
    experienceLevel: "senior",
  }),
});

const result = await response.json();
console.log(result.content);
```

### cURL Examples

```bash
# Parse resume
curl -X POST "http://localhost:8080/api/resume-parsing/parse" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@resume.pdf"

# Get job recommendations
curl -X GET "http://localhost:8080/api/advanced-matching/jobs/123?limit=5" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Support and Resources

### Documentation Updates

This documentation is updated regularly as new AI features are added. Check the `/api/health` endpoint for current API version information.

### Contact Information

For technical support or API questions:

- Email: api-support@skillbridge.com
- Documentation: https://docs.skillbridge.com
- Status Page: https://status.skillbridge.com
