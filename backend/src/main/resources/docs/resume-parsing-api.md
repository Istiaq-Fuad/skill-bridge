# Resume Parsing API Documentation

This API provides endpoints for parsing resumes in PDF and DOCX formats and extracting structured data including contact information, skills, experience, and education.

## Base URL
```
/api/employer/resume
```

## Endpoints

### 1. Parse Resume
Parse a resume file and return extracted data.

**URL:** `/parse`  
**Method:** `POST`  
**Content-Type:** `multipart/form-data`  

**Parameters:**
- `file` (required): The resume file to parse (PDF or DOCX format)

**Response:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1-555-123-4567",
  "summary": "Experienced software developer with 5 years of experience in Java and Spring Boot",
  "skills": [
    {
      "id": null,
      "name": "Java",
      "category": "Programming Languages",
      "proficiencyLevel": 8
    }
  ],
  "experiences": [
    {
      "id": null,
      "company": "Tech Corp",
      "position": "Senior Developer",
      "description": "Led development of enterprise applications",
      "startDate": "2020-01-15",
      "endDate": "2023-12-31",
      "currentlyWorking": false
    }
  ],
  "educations": [
    {
      "id": null,
      "institution": "University of Technology",
      "degree": "Bachelor of Science",
      "fieldOfStudy": "Computer Science",
      "startDate": "2015-09-01",
      "endDate": "2019-06-15",
      "grade": "3.8 GPA"
    }
  ]
}
```

### 2. Parse and Save Resume
Parse a resume and save the extracted data to a user profile.

**URL:** `/parse-and-save`  
**Method:** `POST`  
**Content-Type:** `multipart/form-data`  

**Parameters:**
- `file` (required): The resume file to parse (PDF or DOCX format)
- `userId` (optional): The user ID to save the data to. If not provided, saves to the current authenticated user.

**Response:**
```json
{
  "message": "Resume parsed and data saved successfully",
  "data": {
    // Parsed resume data as shown in the parse endpoint
  }
}
```

### 3. Extract Skills
Extract only the skills section from a resume.

**URL:** `/skills`  
**Method:** `POST`  
**Content-Type:** `multipart/form-data`  

**Parameters:**
- `file` (required): The resume file to parse (PDF or DOCX format)

**Response:**
```json
[
  {
    "id": null,
    "name": "Java",
    "category": "Programming Languages",
    "proficiencyLevel": 8
  },
  {
    "id": null,
    "name": "Spring Boot",
    "category": "Frameworks",
    "proficiencyLevel": 7
  }
]
```

### 4. Extract Experiences
Extract only the work experiences from a resume.

**URL:** `/experiences`  
**Method:** `POST`  
**Content-Type:** `multipart/form-data`  

**Parameters:**
- `file` (required): The resume file to parse (PDF or DOCX format)

**Response:**
```json
[
  {
    "id": null,
    "company": "Tech Corp",
    "position": "Senior Developer",
    "description": "Led development of enterprise applications",
    "startDate": "2020-01-15",
    "endDate": "2023-12-31",
    "currentlyWorking": false
  }
]
```

### 5. Extract Education
Extract only the education section from a resume.

**URL:** `/education`  
**Method:** `POST`  
**Content-Type:** `multipart/form-data`  

**Parameters:**
- `file` (required): The resume file to parse (PDF or DOCX format)

**Response:**
```json
[
  {
    "id": null,
    "institution": "University of Technology",
    "degree": "Bachelor of Science",
    "fieldOfStudy": "Computer Science",
    "startDate": "2015-09-01",
    "endDate": "2019-06-15",
    "grade": "3.8 GPA"
  }
]
```

## Error Responses

All endpoints can return the following error responses:

**Bad Request (400):**
```json
{
  "error": "Parsing Error",
  "message": "Error description"
}
```

**Internal Server Error (500):**
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred while parsing the resume"
}
```

## Supported File Types

- PDF (.pdf)
- Microsoft Word (.docx)
- Legacy Word (.doc)

## Authentication

All endpoints require authentication with a valid JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

Employer role or higher is required to access these endpoints.