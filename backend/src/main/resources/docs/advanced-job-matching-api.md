# Advanced Job Matching API Documentation

This API provides enhanced job matching capabilities with continuous learning, detailed scoring, and comprehensive candidate/job analysis.

## Base URL
```
/api/advanced-matching
```

## Endpoints

### 1. Get Advanced Candidate Matching
Retrieve candidates for a job with advanced compatibility scoring and learning.

**URL:** `/candidates/{jobId}`  
**Method:** `GET`  

**Parameters:**
- `jobId` (path, required): The job ID to match candidates against
- `limit` (query, optional, default: 10): Maximum number of candidates to return

**Response:**
```json
[
  {
    "candidate": {
      "id": 1,
      "username": "john_doe",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "role": "JOB_SEEKER",
      "bio": "Experienced software developer with 5 years of experience in Java and Spring Boot",
      "phoneNumber": "+8801712345678",
      "address": "123 Main Street",
      "city": "Dhaka",
      "country": "Bangladesh",
      "active": true
    },
    "score": {
      "totalScore": 0.85,
      "skillScore": {
        "weightedScore": 0.92,
        "skillCoverageScore": 0.85,
        "proficiencyScore": 0.95,
        "exactMatchRatio": 0.75,
        "skillMatches": [
          {
            "requiredSkill": "Java",
            "matchedSkill": "Java",
            "matched": true,
            "proficiencyScore": 0.8,
            "matchType": "EXACT"
          },
          {
            "requiredSkill": "Spring",
            "matchedSkill": "Spring Boot",
            "matched": true,
            "proficiencyScore": 0.7,
            "matchType": "PARTIAL"
          }
        ]
      },
      "experienceScore": {
        "weightedScore": 0.78,
        "experienceDurationScore": 0.85,
        "relevantExperienceScore": 0.75,
        "totalExperienceYears": 5,
        "recentExperienceScore": 0.8,
        "relevantExperiences": [
          {
            "position": "Software Developer",
            "company": "Tech Corp",
            "startDate": "2020-01-15",
            "endDate": "2023-12-31",
            "yearsSince": 1,
            "relevanceReason": "Position matches job title"
          }
        ]
      },
      "educationScore": {
        "weightedScore": 0.9,
        "educationPresenceScore": 1.0,
        "degreeLevelScore": 0.8,
        "highestEducation": {
          "id": 1,
          "institution": "University of Technology",
          "degree": "Bachelor of Science",
          "fieldOfStudy": "Computer Science",
          "startDate": "2015-09-01",
          "endDate": "2019-06-15",
          "grade": "3.8 GPA"
        }
      },
      "culturalFitScore": {
        "weightedScore": 0.75,
        "locationScore": 0.8,
        "bioScore": 0.7
      },
      "marketDemandScore": {
        "weightedScore": 0.82,
        "marketDemandScore": 0.75,
        "competitionScore": 0.85,
        "trendingScore": 0.87
      }
    }
  }
]
```

### 2. Get Advanced Job Matching
Retrieve jobs for a user with advanced compatibility scoring and learning.

**URL:** `/jobs/{userId}`  
**Method:** `GET`  

**Parameters:**
- `userId` (path, required): The user ID to match jobs for
- `limit` (query, optional, default: 10): Maximum number of jobs to return

**Response:**
```json
[
  {
    "job": {
      "postId": 1,
      "postProfile": "Senior Software Engineer",
      "postDesc": "We are looking for an experienced software engineer with expertise in Java and Spring Boot...",
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
    "score": {
      "totalScore": 0.85,
      "skillScore": {
        "weightedScore": 0.92,
        "skillCoverageScore": 0.85,
        "proficiencyScore": 0.95,
        "exactMatchRatio": 0.75,
        "skillMatches": [
          {
            "requiredSkill": "Java",
            "matchedSkill": "Java",
            "matched": true,
            "proficiencyScore": 0.8,
            "matchType": "EXACT"
          }
        ]
      },
      "experienceScore": {
        "weightedScore": 0.78,
        "experienceDurationScore": 0.85,
        "relevantExperienceScore": 0.75,
        "totalExperienceYears": 5,
        "recentExperienceScore": 0.8,
        "relevantExperiences": []
      },
      "educationScore": {
        "weightedScore": 0.9,
        "educationPresenceScore": 1.0,
        "degreeLevelScore": 0.8,
        "highestEducation": {
          "id": 1,
          "institution": "University of Technology",
          "degree": "Bachelor of Science",
          "fieldOfStudy": "Computer Science"
        }
      },
      "culturalFitScore": {
        "weightedScore": 0.75,
        "locationScore": 0.8,
        "bioScore": 0.7
      },
      "marketDemandScore": {
        "weightedScore": 0.82,
        "marketDemandScore": 0.75,
        "competitionScore": 0.85,
        "trendingScore": 0.87
      }
    }
  }
]
```

### 3. Update Learning Models
Update learning models based on successful placements to improve future matching.

**URL:** `/learning/update`  
**Method:** `POST`  
**Content-Type:** `application/json`

**Request Body:**
```json
{
  "jobId": 1,
  "userId": 1
}
```

**Response:**
```json
"Learning models updated successfully"
```

## Error Responses

All endpoints can return the following error responses:

**Bad Request (400):**
```json
{
  "error": "Matching Error",
  "message": "Error description"
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

Appropriate roles are required:
- Job seekers can access their job matches
- Employers can access candidate matches for their jobs
- Admins can access all matching endpoints

## Continuous Learning

The advanced job matching system continuously learns from successful placements to improve future matching accuracy. When a candidate is successfully placed in a job, the system updates its learning models to better understand which skills, experiences, and attributes are most predictive of successful placements.

The learning process considers:
- Skill performance weights based on successful placements
- Experience level importance for different job types
- Location preferences and cultural fit factors
- Company and industry preferences

This continuous learning approach ensures that the matching system becomes increasingly accurate over time, providing better outcomes for both job seekers and employers.