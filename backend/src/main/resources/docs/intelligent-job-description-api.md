# Intelligent Job Description API Documentation

This API provides intelligent job description generation, optimization, and enhancement capabilities for employers to create compelling job postings that attract top talent.

## Base URL
```
/api/intelligent-jobs
```

## Endpoints

### 1. Generate Job Description
Generate an optimized job description based on minimal input.

**URL:** `/generate`  
**Method:** `POST`  
**Content-Type:** `application/json`

**Request Body:**
```json
{
  "jobTitle": "Software Engineer",
  "industry": "Technology",
  "experienceLevel": "Mid",
  "location": "Dhaka"
}
```

**Response:**
```json
{
  "jobTitle": "Software Engineer",
  "industry": "Technology",
  "experienceLevel": "Mid",
  "location": "Dhaka",
  "companyOverview": "Leading technology company in Bangladesh",
  "jobSummary": "Exciting opportunity for a talented software engineer",
  "responsibilities": [
    "Develop and maintain software applications",
    "Collaborate with cross-functional teams",
    "Participate in code reviews and testing"
  ],
  "requiredQualifications": [
    "Bachelor's degree in Computer Science or related field",
    "3+ years of relevant experience"
  ],
  "preferredQualifications": [
    "Master's degree preferred",
    "Experience with cloud technologies"
  ],
  "technicalSkills": [
    "Java",
    "Spring Boot",
    "React"
  ],
  "softSkills": [
    "Communication",
    "Teamwork",
    "Problem-solving"
  ],
  "benefits": [
    "Health insurance",
    "Flexible hours",
    "Professional development"
  ],
  "workEnvironment": "Collaborative and innovative workplace",
  "processingTime": 1250
}
```

### 2. Optimize Existing Job Description
Optimize an existing job description for better candidate attraction.

**URL:** `/optimize/{jobId}`  
**Method:** `POST`

**Parameters:**
- `jobId` (path, required): The job ID to optimize

**Response:**
```json
{
  "originalJobId": 1,
  "titleOptimization": "Senior Software Engineer - Backend",
  "descriptionEnhancements": [
    "Add more details about company culture",
    "Clarify growth opportunities"
  ],
  "responsibilityImprovements": [
    "Make responsibilities more outcome-focused",
    "Add metrics for success"
  ],
  "qualificationBalancing": [
    "Consider removing overly restrictive requirements",
    "Add alternative qualifications"
  ],
  "skillAlignment": [
    "Include emerging technologies",
    "Emphasize soft skills"
  ],
  "benefitHighlighting": [
    "Better showcase remote work options",
    "Highlight learning and development opportunities"
  ],
  "diversityInclusion": [
    "Use inclusive language",
    "Emphasize equal opportunity commitment"
  ],
  "callToAction": "Apply now to join our innovative team",
  "processingTime": 890
}
```

### 3. Suggest Relevant Skills
Suggest relevant skills and technologies for a job.

**URL:** `/skills/suggest`  
**Method:** `POST`  
**Content-Type:** `application/json`

**Request Body:**
```json
{
  "jobTitle": "Software Engineer",
  "industry": "Technology",
  "experienceLevel": "Mid"
}
```

**Response:**
```json
{
  "jobTitle": "Software Engineer",
  "industry": "Technology",
  "experienceLevel": "Mid",
  "technicalSkills": {
    "programmingLanguages": ["Java", "Python", "JavaScript"],
    "databases": ["PostgreSQL", "MongoDB"],
    "cloudPlatforms": ["AWS", "Azure"],
    "devOps": ["Docker", "Kubernetes"]
  },
  "frameworksAndTools": [
    "Spring Boot",
    "React",
    "Node.js"
  ],
  "domainKnowledge": [
    "Financial services",
    "E-commerce"
  ],
  "certifications": [
    "AWS Certified Developer",
    "Oracle Certified Professional"
  ],
  "emergingTechnologies": [
    "AI/ML",
    "Blockchain",
    "IoT"
  ],
  "softSkills": [
    "Leadership",
    "Communication",
    "Critical Thinking"
  ],
  "processingTime": 750
}
```

### 4. Suggest Competitive Salary Ranges
Suggest competitive salary ranges for a job.

**URL:** `/salary/suggest`  
**Method:** `POST`  
**Content-Type:** `application/json`

**Request Body:**
```json
{
  "jobTitle": "Software Engineer",
  "industry": "Technology",
  "experienceLevel": "Mid",
  "location": "Dhaka"
}
```

**Response:**
```json
{
  "jobTitle": "Software Engineer",
  "industry": "Technology",
  "experienceLevel": "Mid",
  "location": "Dhaka",
  "salaryRanges": {
    "currency": "BDT",
    "minimum": 60000,
    "midpoint": 80000,
    "maximum": 120000
  },
  "marketPositioning": "Competitive with market averages",
  "geographicVariations": {
    "dhaka": "60,000 - 120,000 BDT",
    "chittagong": "50,000 - 100,000 BDT",
    "sylhet": "55,000 - 110,000 BDT"
  },
  "benefitsPackage": [
    "Health insurance",
    "Provident fund",
    "Annual bonuses",
    "Professional development allowance"
  ],
  "performanceIncentives": [
    "Quarterly performance bonuses",
    "Stock options for senior positions",
    "Project completion bonuses"
  ],
  "processingTime": 920
}
```

### 5. Get Employer Jobs with Insights
Get all employer's jobs with intelligent insights.

**URL:** `/employer/jobs`  
**Method:** `GET`

**Response:**
```json
[
  {
    "postId": 1,
    "postProfile": "Software Engineer",
    "postDesc": "We are looking for an experienced software engineer...",
    "reqExperience": 3,
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

### 6. Enhance Job Post
Apply intelligent enhancements to a job post.

**URL:** `/enhance/{jobId}`  
**Method:** `PUT`  
**Content-Type:** `application/json`

**Parameters:**
- `jobId` (path, required): The job ID to enhance

**Request Body:**
```json
{
  "title": "Senior Software Engineer",
  "description": "We are looking for an experienced software engineer...",
  "techStack": ["Java", "Spring Boot", "React", "Docker"],
  "location": "Dhaka",
  "employmentType": "FULL_TIME",
  "salaryMin": 100000,
  "salaryMax": 150000
}
```

**Response:**
```json
{
  "postId": 1,
  "postProfile": "Senior Software Engineer",
  "postDesc": "We are looking for an experienced software engineer...",
  "reqExperience": 5,
  "postTechStack": ["Java", "Spring Boot", "React", "Docker"],
  "location": "Dhaka",
  "employmentType": "FULL_TIME",
  "salaryMin": 100000,
  "salaryMax": 150000,
  "salaryCurrency": "BDT",
  "postedAt": "2025-09-15T10:30:00",
  "jobStatus": "ACTIVE"
}
```

## Error Responses

All endpoints can return the following error responses:

**Bad Request (400):**
```json
{
  "error": "Job Description Generation Error",
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

## Key Features

### 1. Intelligent Job Description Generation
- Creates compelling job descriptions based on minimal input
- Includes all essential sections: company overview, job summary, responsibilities, qualifications, skills, benefits
- Optimized for candidate attraction and search engine visibility

### 2. Job Description Optimization
- Enhances existing job descriptions for better candidate attraction
- Improves responsibility clarity and outcome focus
- Balances qualifications to attract a diverse pool of candidates
- Highlights benefits and perks more effectively

### 3. Skill Suggestions
- Recommends relevant technical and soft skills for job roles
- Suggests popular frameworks, tools, and platforms
- Identifies domain-specific knowledge requirements
- Recommends emerging technologies and certifications

### 4. Salary Range Suggestions
- Provides competitive salary ranges based on market data
- Offers geographic variations for different locations
- Suggests comprehensive benefits packages
- Recommends performance-based incentive structures

### 5. Continuous Learning
- The system learns from successful placements to improve future suggestions
- Adapts to market trends and evolving skill requirements
- Updates recommendations based on candidate engagement and application rates

## Benefits

1. **Time Savings**: Reduce time spent on job description creation by 70%
2. **Quality Improvement**: Create more compelling and complete job descriptions
3. **Candidate Attraction**: Attract higher quality candidates with optimized job postings
4. **Market Alignment**: Ensure competitive compensation and benefits packages
5. **Diversity & Inclusion**: Promote inclusive language and equal opportunity commitments
6. **Data-Driven Decisions**: Make informed decisions based on market insights and analytics