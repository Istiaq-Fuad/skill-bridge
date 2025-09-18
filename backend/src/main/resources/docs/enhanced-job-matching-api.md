# Enhanced Candidate Ranking API Documentation

This API provides enhanced candidate ranking and matching capabilities for employers, with detailed scoring and red flag detection.

## Base URL
```
/api/employer/matching
```

## Endpoints

### 1. Get Enhanced Candidate Matching
Retrieve candidates for a job with detailed compatibility scoring.

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
      // ... other user fields
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
          "fieldOfStudy": "Computer Science"
          // ... other education fields
        }
      },
      "culturalFitScore": {
        "weightedScore": 0.75,
        "locationScore": 0.8,
        "bioScore": 0.7
      },
      "redFlags": {
        "hasRedFlags": false,
        "hasWarnings": true,
        "redFlags": [],
        "warnings": [
          "Employment gap of 3 months from 2022-06-01 to 2022-09-01"
        ]
      }
    }
  }
]
```

### 2. Get Traditional Candidate Matching
Retrieve candidates using the traditional matching algorithm for comparison.

**URL:** `/candidates/{jobId}/traditional`  
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
      "email": "john.doe@example.com"
      // ... other user fields
    },
    "compatibilityScore": 0.75
  }
]
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

## Authentication

All endpoints require authentication with a valid JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

Employer role or higher is required to access these endpoints.