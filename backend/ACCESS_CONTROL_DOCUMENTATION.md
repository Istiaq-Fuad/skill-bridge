# SkillBridge Access Control Documentation

## User Roles

- **ANONYMOUS**: Unauthenticated users
- **JOB_SEEKER**: Registered job seekers
- **EMPLOYER**: Registered employers
- **ADMIN**: System administrators

## Access Control Rules

### Public Endpoints (ANONYMOUS)

- `GET /` - Home page
- `POST /api/users/register` - User registration
- `POST /api/users/login` - User login
- `GET /api/jobs` - View all jobs
- `GET /api/jobs/{id}` - View specific job
- `GET /api/jobs/keyword/**` - Search jobs by keyword

### Authenticated User Endpoints

- `GET /api/users/profile` - Get current user profile (JOB_SEEKER, EMPLOYER, ADMIN)
- `PUT /api/users/profile` - Update current user profile (JOB_SEEKER, EMPLOYER, ADMIN)

### Job Seeker Specific (JOB_SEEKER)

- `POST /api/applications` - Apply for jobs
- `GET /api/applications` - View own applications
- `GET /api/applications/user/{userId}` - View applications (own only)

### Employer Specific (EMPLOYER or ADMIN)

- `POST /api/jobs` - Create new job posts
- `PUT /api/jobs/{id}` - Update own job posts (with ownership validation)
- `DELETE /api/jobs/{id}` - Delete own job posts (with ownership validation)
- `PUT /api/applications/{id}/status` - Update application status
- `GET /api/applications/job/{jobId}` - View applications for job

### Profile Management (JOB_SEEKER, EMPLOYER, ADMIN)

- `GET /api/profiles/{userId}` - View user profiles
- `PUT /api/profiles/{userId}` - Update profile (with ownership validation)
- `GET /api/profiles/education` - View own education records
- `POST /api/profiles/education` - Add education records
- `PUT /api/profiles/education/{id}` - Update education records

### Admin Only (ADMIN)

- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update any user
- `DELETE /api/users/{id}` - Delete any user

## Implementation Details

### Security Configuration

- **Method Security**: Enabled with `@EnableMethodSecurity(prePostEnabled = true)`
- **JWT Authentication**: Required for all protected endpoints
- **Role-based Access**: Implemented using `@PreAuthorize` annotations

### Authorization Flow

1. User authenticates via JWT token
2. User object implements `UserDetails` with role as authority
3. Spring Security checks role permissions against `@PreAuthorize` annotations
4. Additional business logic validation (e.g., ownership checks) in controller methods

### Key Features

- **Ownership Validation**: Users can only modify their own resources
- **Role Hierarchy**: ADMIN has access to all endpoints
- **Granular Permissions**: Method-level security for fine-grained control
- **Business Logic Integration**: Role checks combined with ownership validation

## Testing Access Control

### For Employers

- Should be able to: Create, update, delete own jobs; view applications to their jobs
- Should NOT be able to: Access admin endpoints, apply for jobs, modify other employers' jobs

### For Job Seekers

- Should be able to: Apply for jobs, view own applications, manage own profile
- Should NOT be able to: Create jobs, view other users' applications, access admin endpoints

### For Admins

- Should be able to: Access all endpoints, manage all users, override ownership restrictions
- Should NOT have restrictions (full system access)

## Troubleshooting

### Common Issues

1. **403 Forbidden**: Check if user has correct role and JWT token is valid
2. **401 Unauthorized**: Verify JWT token is included in Authorization header
3. **Role Mismatch**: Ensure UserRole enum values match authority checks in @PreAuthorize

### Debug Tips

- Check JWT token payload for correct role
- Verify `getAuthorities()` method returns correct role name
- Ensure @PreAuthorize uses exact role names from UserRole enum
