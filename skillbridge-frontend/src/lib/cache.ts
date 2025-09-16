"use client";

import { cache } from "react";
import { apiClient } from "@/lib/api";

// Server-side cache functions for Next.js App Router
// These functions use React's cache function for deduplication

export const getCachedJobs = cache(
  async (filters?: {
    search?: string;
    location?: string;
    company?: string;
  }) => {
    const response = await apiClient.getJobs(filters);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || "Failed to fetch jobs");
  }
);

export const getCachedJob = cache(async (id: number) => {
  const response = await apiClient.getJob(id);
  if (response.success && response.data) {
    return response.data;
  }
  throw new Error(response.error || "Failed to fetch job");
});

export const getCachedUserProfile = cache(async (userId: number) => {
  const response = await apiClient.getUserProfile(userId);
  if (response.success && response.data) {
    return response.data;
  }
  throw new Error(response.error || "Failed to fetch user profile");
});

export const getCachedUserApplications = cache(async (userId: number) => {
  const response = await apiClient.getUserApplications(userId);
  if (response.success && response.data) {
    return response.data;
  }
  throw new Error(response.error || "Failed to fetch user applications");
});

export const getCachedJobApplications = cache(async (jobId: number) => {
  const response = await apiClient.getJobApplications(jobId);
  if (response.success && response.data) {
    return response.data;
  }
  throw new Error(response.error || "Failed to fetch job applications");
});

export const getCachedProfile = cache(async () => {
  const response = await apiClient.getProfile();
  if (response.success && response.data) {
    return response.data;
  }
  throw new Error(response.error || "Failed to fetch profile");
});

// Tag-based cache revalidation functions
export const revalidateJobsCache = () => {
  // This would be used with Next.js revalidateTag if using fetch with tags
  // For now, we'll implement custom cache invalidation in hooks
};

export const revalidateUserCache = (_userId: number) => {
  // Custom cache invalidation for user-specific data
};

export const revalidateJobCache = (_jobId: number) => {
  // Custom cache invalidation for job-specific data
};

// Cache configuration constants
export const CACHE_TAGS = {
  JOBS: "jobs",
  JOB: "job",
  USER: "user",
  PROFILE: "profile",
  APPLICATIONS: "applications",
} as const;

export const CACHE_DURATIONS = {
  JOBS: 5 * 60, // 5 minutes
  JOB: 10 * 60, // 10 minutes
  USER: 15 * 60, // 15 minutes
  PROFILE: 10 * 60, // 10 minutes
  APPLICATIONS: 2 * 60, // 2 minutes
} as const;
