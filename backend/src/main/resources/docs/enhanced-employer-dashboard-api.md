# Enhanced Employer Dashboard API Documentation

This API provides enhanced dashboard capabilities for employers, with comprehensive analytics, job management, and candidate insights.

## Base URL
```
/api/employer/enhanced-dashboard
```

## Endpoints

### 1. Get Enhanced Dashboard Statistics
Retrieve comprehensive dashboard statistics for the employer.

**URL:** `/stats`  
**Method:** `GET`  

**Response:**
```json
{
  "totalJobs": 15,
  "totalApplications": 127,
  "applicationsByStatus": {
    "APPLIED": 85,
    "REVIEWED": 25,
    "INTERVIEW": 12,
    "REJECTED": 3,
    "ACCEPTED": 2
  },
  "recentJobs": [
    {
      "postId": 1,
      "postProfile": "Senior Software Engineer",
      "postDesc": "We are looking for an experienced software engineer...",
      "reqExperience": 5,
      "postTechStack": ["Java", "Spring", "React"],
      "location": "Dhaka",
      "employmentType": "FULL_TIME",
      "salaryMin": 80000,
      "salaryMax": 120000,
      "salaryCurrency": "BDT",
      "postedAt": "2025-09-15T10:30:00",
      "expiryDate": "2025-10-15T10:30:00",
      "jobStatus": "ACTIVE"
    }
  ],
  "recentApplications": [
    {
      "id": 1,
      "appliedAt": "2025-09-18T14:20:00",
      "lastUpdated": "2025-09-18T14:20:00",
      "status": "APPLIED",
      "coverLetter": "I am excited to apply for this position...",
      "resumeUrl": "https://example.com/resumes/john_doe.pdf"
    }
  ],
  "jobPerformanceMetrics": {
    "totalApplications": 127,
    "totalAccepted": 2,
    "averageApplicationsPerJob": 8.47,
    "acceptanceRate": 0.0157,
    "averageDaysToFill": 45.0,
    "activeJobs": 8,
    "conversionRate": 0.133
  },
  "applicationTrends": {
    "dailyApplications": {
      "2025-09-15": 5,
      "2025-09-16": 8,
      "2025-09-17": 12,
      "2025-09-18": 7
    },
    "weeklyTrend": {
      "This Week": 22,
      "Last Week": 35,
      "2 Weeks Ago": 28,
      "3 Weeks Ago": 42
    }
  },
  "candidateDemographics": {
    "totalCandidates": 98,
    "byExperienceLevel": {},
    "byLocation": {},
    "byEducation": {}
  },
  "employerActivityMetrics": {
    "jobsCreated": 15,
    "lastActivity": "2025-09-18T16:45:00",
    "responseRate": 0.85,
    "avgResponseTime": 24.5
  }
}
```

### 2. Get Detailed Job Analytics
Retrieve detailed analytics for a specific job.

**URL:** `/jobs/{jobId}/analytics`  
**Method:** `GET`  

**Parameters:**
- `jobId` (path, required): The job ID to get analytics for

**Response:**
```json
{
  "job": {
    "postId": 1,
    "postProfile": "Senior Software Engineer",
    "postDesc": "We are looking for an experienced software engineer...",
    "reqExperience": 5,
    "postTechStack": ["Java", "Spring", "React"],
    "location": "Dhaka",
    "employmentType": "FULL_TIME",
    "salaryMin": 80000,
    "salaryMax": 120000,
    "salaryCurrency": "BDT",
    "postedAt": "2025-09-15T10:30:00",
    "expiryDate": "2025-10-15T10:30:00",
    "jobStatus": "ACTIVE"
  },
  "totalApplications": 25,
  "applicationsByStatus": {
    "APPLIED": 18,
    "REVIEWED": 5,
    "INTERVIEW": 2,
    "REJECTED": 0,
    "ACCEPTED": 0
  },
  "applicationTimeline": {
    "2025-09-15": 3,
    "2025-09-16": 7,
    "2025-09-17": 10,
    "2025-09-18": 5
  },
  "topSources": {
    "JOB_PORTAL": 20,
    "REFERRAL": 3,
    "LINKEDIN": 2
  },
  "daysToFirstHire": 12
}
```

### 3. Bulk Update Job Statuses
Update the status of multiple jobs at once.

**URL:** `/jobs/bulk-update`  
**Method:** `PUT`  
**Content-Type:** `application/json`

**Request Body:**
```json
{
  "jobIds": [1, 2, 3],
  "status": "INACTIVE"
}
```

**Response:**
```json
{
  "updatedCount": 3
}
```

### 4. Get Enhanced Candidate Information
Retrieve enhanced candidate information for a specific job.

**URL:** `/jobs/{jobId}/candidates`  
**Method:** `GET`  

**Parameters:**
- `jobId` (path, required): The job ID to get candidates for

**Response:**
```json
[
  {
    "application": {
      "id": 1,
      "appliedAt": "2025-09-18T14:20:00",
      "lastUpdated": "2025-09-18T14:20:00",
      "status": "APPLIED",
      "coverLetter": "I am excited to apply for this position...",
      "resumeUrl": "https://example.com/resumes/john_doe.pdf"
    },
    "candidate": {
      "id": 101,
      "username": "john_doe",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com"
    },
    "daysSinceApplication": 1,
    "skillsCount": 12
  }
]
```

## Error Responses

All endpoints can return the following error responses:

**Bad Request (400):**
```json
{
  "error": "Dashboard Error",
  "message": "Error description"
}
```

**Unauthorized (401):**
```json
{
  "error": "Authentication Error",
  "message": "Full authentication is required to access this resource"
}
```

**Forbidden (403):**
```json
{
  "error": "Authorization Error",
  "message": "Access denied to this resource"
}
```

**Not Found (404):**
```json
{
  "error": "Resource Not Found",
  "message": "The requested resource was not found"
}
```

## Authentication

All endpoints require authentication with a valid JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

Employer role or higher is required to access these endpoints.