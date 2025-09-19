import { notFound } from "next/navigation";
import { createServerApiClient } from "./api";
import { cookies } from "next/headers";

// Helper to get auth token from cookies for server-side requests
async function getServerToken(): Promise<string | undefined> {
  try {
    const cookieStore = await cookies();
    return cookieStore.get("token")?.value;
  } catch {
    // In case cookies() is not available in all contexts
    return undefined;
  }
}

// Server-side data fetching functions for Next.js App Router
// These follow Next.js best practices for caching and error handling

export async function getJobs(filters?: {
  search?: string;
  location?: string;
  company?: string;
}) {
  try {
    const token = await getServerToken();
    const apiClient = createServerApiClient(token);
    const response = await apiClient.getJobs(filters);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || "Failed to fetch jobs");
  } catch (error) {
    console.error("Error fetching jobs:", error);
    return [];
  }
}

export async function getJob(id: number) {
  try {
    const token = await getServerToken();
    const apiClient = createServerApiClient(token);
    const response = await apiClient.getJob(id);
    if (response.success && response.data) {
      return response.data;
    }
    notFound(); // Next.js 404 page
  } catch (error) {
    console.error("Error fetching job:", error);
    notFound();
  }
}

export async function getUserProfile(userId: number) {
  try {
    const token = await getServerToken();
    const apiClient = createServerApiClient(token);
    const response = await apiClient.getUserProfile(userId);
    if (response.success && response.data) {
      return response.data;
    }
    return null;
  } catch (error) {
    console.error("Error fetching user profile:", error);
    return null;
  }
}

export async function getUserApplications(userId: number) {
  try {
    const token = await getServerToken();
    const apiClient = createServerApiClient(token);
    const response = await apiClient.getUserApplications(userId);
    if (response.success && response.data) {
      return response.data;
    }
    return [];
  } catch (error) {
    console.error("Error fetching user applications:", error);
    return [];
  }
}

export async function getJobApplications(jobId: number) {
  try {
    const token = await getServerToken();
    const apiClient = createServerApiClient(token);
    const response = await apiClient.getJobApplications(jobId);
    if (response.success && response.data) {
      return response.data;
    }
    return [];
  } catch (error) {
    console.error("Error fetching job applications:", error);
    return [];
  }
}
