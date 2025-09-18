# Enhanced Admin Panel API Documentation

This API provides enhanced admin panel capabilities for platform management, user account management, and system monitoring.

## Base URL
```
/api/admin
```

## Endpoints

### 1. Get Platform Statistics
Retrieve comprehensive platform statistics.

**URL:** `/statistics`  
**Method:** `GET`  

**Response:**
```json
{
  "totalUsers": 1500,
  "totalJobSeekers": 1200,
  "totalEmployers": 250,
  "totalAdmins": 50,
  "totalJobs": 800,
  "activeJobs": 650,
  "closedJobs": 150,
  "totalApplications": 5400,
  "pendingApplications": 3200,
  "acceptedApplications": 850,
  "rejectedApplications": 1350
}
```

### 2. Get All Users
Retrieve all users with pagination.

**URL:** `/users`  
**Method:** `GET`  

**Parameters:**
- `page` (query, optional, default: 0): Page number (0-based)
- `size` (query, optional, default: 20): Page size

**Response:**
```json
[
  {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "role": "JOB_SEEKER",
    "firstName": "John",
    "lastName": "Doe",
    "bio": "Experienced software developer",
    "phoneNumber": "+8801712345678",
    "address": "123 Main St",
    "city": "Dhaka",
    "country": "Bangladesh",
    "active": true
  }
]
```

### 3. Get User by ID
Retrieve a specific user by ID.

**URL:** `/users/{userId}`  
**Method:** `GET`  

**Parameters:**
- `userId` (path, required): User ID

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "role": "JOB_SEEKER",
  "firstName": "John",
  "lastName": "Doe",
  "bio": "Experienced software developer",
  "phoneNumber": "+8801712345678",
  "address": "123 Main St",
  "city": "Dhaka",
  "country": "Bangladesh",
  "active": true
}
```

### 4. Update User Account Status
Update a user's account status (active/inactive).

**URL:** `/users/{userId}/status`  
**Method:** `PUT`  
**Content-Type:** `application/json`

**Parameters:**
- `userId` (path, required): User ID

**Request Body:**
```json
{
  "active": false
}
```

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "role": "JOB_SEEKER",
  "firstName": "John",
  "lastName": "Doe",
  "bio": "Experienced software developer",
  "phoneNumber": "+8801712345678",
  "address": "123 Main St",
  "city": "Dhaka",
  "country": "Bangladesh",
  "active": false
}
```

### 5. Delete User Account
Delete a user account and all related data.

**URL:** `/users/{userId}`  
**Method:** `DELETE`  

**Parameters:**
- `userId` (path, required): User ID

**Response:**
```
204 No Content
```

### 6. Get All Jobs
Retrieve all jobs with pagination.

**URL:** `/jobs`  
**Method:** `GET`  

**Parameters:**
- `page` (query, optional, default: 0): Page number (0-based)
- `size` (query, optional, default: 20): Page size

**Response:**
```json
[
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
]
```

### 7. Update Job Status
Update a job's status.

**URL:** `/jobs/{jobId}/status`  
**Method:** `PUT`  
**Content-Type:** `application/json`

**Parameters:**
- `jobId` (path, required): Job ID

**Request Body:**
```json
{
  "status": "INACTIVE"
}
```

**Response:**
```json
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
  "jobStatus": "INACTIVE"
}
```

### 8. Delete Job
Delete a job and all related applications.

**URL:** `/jobs/{jobId}`  
**Method:** `DELETE`  

**Parameters:**
- `jobId` (path, required): Job ID

**Response:**
```
204 No Content
```

### 9. Get All Applications
Retrieve all applications with pagination.

**URL:** `/applications`  
**Method:** `GET`  

**Parameters:**
- `page` (query, optional, default: 0): Page number (0-based)
- `size` (query, optional, default: 20): Page size

**Response:**
```json
[
  {
    "id": 1,
    "appliedAt": "2025-09-18T14:20:00",
    "lastUpdated": "2025-09-18T14:20:00",
    "status": "APPLIED",
    "coverLetter": "I am excited to apply for this position...",
    "resumeUrl": "https://example.com/resumes/john_doe.pdf"
  }
]
```

### 10. Update Application Status
Update an application's status.

**URL:** `/applications/{applicationId}/status`  
**Method:** `PUT`  
**Content-Type:** `application/json`

**Parameters:**
- `applicationId` (path, required): Application ID

**Request Body:**
```json
{
  "status": "REVIEWED"
}
```

**Response:**
```json
{
  "id": 1,
  "appliedAt": "2025-09-18T14:20:00",
  "lastUpdated": "2025-09-18T15:30:00",
  "status": "REVIEWED",
  "coverLetter": "I am excited to apply for this position...",
  "resumeUrl": "https://example.com/resumes/john_doe.pdf"
}
```

### 11. Get Flagged Content
Retrieve flagged content for review.

**URL:** `/flagged-content`  
**Method:** `GET`  

**Response:**
```json
[]
```

### 12. Resolve Dispute
Resolve a dispute.

**URL:** `/disputes/{disputeId}/resolve`  
**Method:** `POST`  
**Content-Type:** `application/json`

**Parameters:**
- `disputeId` (path, required): Dispute ID

**Request Body:**
```json
{
  "resolution": "Dispute resolved in favor of the job seeker"
}
```

**Response:**
```json
"Dispute resolved successfully"
```

## Error Responses

All endpoints can return the following error responses:

**Bad Request (400):**
```json
{
  "error": "Validation Error",
  "message": "Invalid request parameters"
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

Admin role or higher is required to access these endpoints.