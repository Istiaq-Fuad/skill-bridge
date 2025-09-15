# Zustand State Management Migration Guide

This guide explains how to migrate from React Context to Zustand for state management in the SkillBridge frontend application.

## Overview

We've implemented Zustand stores to replace React Context for better performance, developer experience, and type safety. The stores are organized by domain:

- **Auth Store**: User authentication and session management
- **Jobs Store**: Job listings, search, and CRUD operations
- **Applications Store**: Job applications management
- **Profile Store**: User profiles, skills, education, experience, and portfolio

## Store Structure

### Auth Store (`/src/stores/auth-store.ts`)

```typescript
// Usage example
import { useAuthStore, useUser, useIsAuthenticated } from "@/stores";

function MyComponent() {
  // Direct store access
  const { login, logout, updateUser } = useAuthStore();

  // Optimized selectors (prevent unnecessary re-renders)
  const user = useUser();
  const isAuthenticated = useIsAuthenticated();

  // Actions
  const handleLogin = async () => {
    const result = await login(username, password);
    if (result.success) {
      // Handle success
    }
  };
}
```

### Jobs Store (`/src/stores/jobs-store.ts`)

Uses immer middleware for cleaner state mutations:

```typescript
// Usage example
import { useJobsStore, useJobs, useCurrentJob } from "@/stores";

function JobsList() {
  const { fetchJobs, createJob, updateJob, deleteJob } = useJobsStore();
  const jobs = useJobs();
  const isLoading = useJobsStore((state) => state.isLoading);

  // Fetch jobs with filters
  const handleSearch = async () => {
    await fetchJobs({
      search: "React",
      location: "Remote",
      page: 0,
      size: 10,
    });
  };
}
```

### Applications Store (`/src/stores/applications-store.ts`)

```typescript
// Usage example
import { useApplicationsStore, useUserApplications } from "@/stores";

function MyApplications() {
  const { applyForJob, fetchUserApplications } = useApplicationsStore();
  const applications = useUserApplications();

  const handleApply = async (jobId: number) => {
    const result = await applyForJob(jobId);
    if (result.success) {
      // Handle success
    }
  };
}
```

### Profile Store (`/src/stores/profile-store.ts`)

```typescript
// Usage example
import { useProfileStore, useProfile, useSkills } from "@/stores";

function Profile() {
  const { fetchProfile, addSkill, updateProfile } = useProfileStore();
  const profile = useProfile();
  const skills = useSkills();

  const handleAddSkill = async (skill) => {
    const result = await addSkill(userId, skill);
    if (result.success) {
      // Skill added successfully
    }
  };
}
```

## Migration Steps

### 1. Replace Context Imports

**Before (React Context):**

```typescript
import { useAuth } from "@/contexts/AuthContext";

function LoginPage() {
  const { login, user, isLoading } = useAuth();
  // ...
}
```

**After (Zustand):**

```typescript
import { useAuthStore, useUser, useIsLoading } from "@/stores";

function LoginPage() {
  const login = useAuthStore((state) => state.login);
  const user = useUser();
  const isLoading = useIsLoading();
  // ...
}
```

### 2. Initialize Stores in App Layout

```typescript
// In your app/layout.tsx or main component
import { useInitializeStores } from '@/hooks/use-stores';

export default function RootLayout() {
  useInitializeStores(); // Initialize auth and other stores

  return (
    // Your layout JSX
  );
}
```

### 3. Remove Context Providers

You can remove the AuthProvider and other context providers from your app since Zustand doesn't require providers.

## Key Benefits

### 1. **Better Performance**

- Only components using specific state slices re-render
- Automatic optimization with selectors
- No provider tree overhead

### 2. **Type Safety**

- Full TypeScript support
- Compile-time error checking
- IntelliSense support

### 3. **Developer Experience**

- DevTools integration for debugging
- Cleaner, more predictable code
- Better testing capabilities

### 4. **Persistence**

- Automatic localStorage persistence for auth
- Customizable persistence strategies
- SSR-friendly

## Best Practices

### 1. Use Selectors for Optimization

```typescript
// Good - only re-renders when user changes
const user = useUser();

// Bad - re-renders on any auth state change
const { user } = useAuthStore();
```

### 2. Organize Actions by Domain

```typescript
// Group related actions in the same store
const jobsStore = {
  // State
  jobs: [],

  // Related actions
  fetchJobs: async () => {},
  createJob: async () => {},
  updateJob: async () => {},
  deleteJob: async () => {},
};
```

### 3. Handle Loading and Error States

```typescript
const MyComponent = () => {
  const { fetchJobs } = useJobsStore();
  const isLoading = useJobsLoading();
  const error = useJobsError();

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return <JobsList />;
};
```

### 4. Use Immer for Complex State Updates

```typescript
// With immer middleware, you can mutate state directly
set((state) => {
  state.jobs.push(newJob);
  state.isLoading = false;
});

// Instead of immutable updates
set({
  jobs: [...get().jobs, newJob],
  isLoading: false,
});
```

## Testing

Zustand stores are easy to test:

```typescript
import { renderHook, act } from "@testing-library/react";
import { useAuthStore } from "@/stores";

test("should login user", async () => {
  const { result } = renderHook(() => useAuthStore());

  await act(async () => {
    const loginResult = await result.current.login("username", "password");
    expect(loginResult.success).toBe(true);
  });

  expect(result.current.isAuthenticated).toBe(true);
});
```

## Debugging

Use the Redux DevTools extension to debug your Zustand stores:

1. Install Redux DevTools browser extension
2. Stores are automatically connected (via `devtools` middleware)
3. View state changes, time-travel debug, and inspect actions

## Migration Checklist

- [ ] Install Zustand: `npm install zustand`
- [ ] Create domain-specific stores
- [ ] Add type definitions
- [ ] Implement persistence for auth
- [ ] Replace Context usage in components
- [ ] Initialize stores in app layout
- [ ] Remove Context providers
- [ ] Test all functionality
- [ ] Update error handling
- [ ] Add loading states

## Files Changed

- `/src/stores/` - New directory with all Zustand stores
- `/src/hooks/use-stores.ts` - Store initialization hooks
- `/src/lib/api.ts` - Updated to work with Zustand (optional)
- Components - Replace Context imports with Zustand imports

This migration provides a more scalable, performant, and maintainable state management solution for your application.
