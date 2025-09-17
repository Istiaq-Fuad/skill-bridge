// API configuration and utilities
import { handleApiError } from "./error-handler";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: "JOB_SEEKER" | "EMPLOYER" | "ADMIN";
  companyName?: string;
  companyDescription?: string;
  companyWebsite?: string;
  companyLocation?: string;
  contactPhone?: string;
}

export interface Job {
  id: number;
  title: string;
  description: string;
  company: string;
  location: string;
  salary?: number;
  requirements: string[];
  createdAt: string;
  employerId: number;
}

export interface JobApplication {
  id: number;
  jobId: number;
  userId: number;
  status: "PENDING" | "REVIEWED" | "ACCEPTED" | "REJECTED";
  appliedAt: string;
  coverLetter?: string;
  resumeUrl?: string;
  job?: Job;
  user?: User;
  profile?: Profile;
}

export interface EmployerDashboardStats {
  totalJobs: number;
  totalApplications: number;
  pendingApplications: number;
  activeJobs: number;
  responseRate: number;
  profileViews: number;
}

export interface Profile {
  id: number;
  userId: number;
  bio?: string;
  skills: Skill[];
  education: Education[];
  experience: Experience[];
  portfolio: Portfolio[];
}

export interface Skill {
  id: number;
  name: string;
  level: "BEGINNER" | "INTERMEDIATE" | "ADVANCED" | "EXPERT";
}

export interface Education {
  id: number;
  institution: string;
  degree: string;
  fieldOfStudy: string;
  startDate: string;
  endDate?: string;
  description?: string;
}

export interface Experience {
  id: number;
  company: string;
  position: string;
  startDate: string;
  endDate?: string;
  description?: string;
  current: boolean;
}

export interface Portfolio {
  id: number;
  title: string;
  description: string;
  url?: string;
  technologies: string[];
  imageUrl?: string;
}

class ApiClient {
  private baseUrl: string;

  constructor() {
    this.baseUrl = API_BASE_URL;
  }

  protected getAuthHeader(): Record<string, string> {
    // Check if we're in a browser environment first
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("token");
      if (token && token.trim()) {
        return { Authorization: `Bearer ${token}` };
      }
    }

    // For server-side, we can't access localStorage
    // Token should be passed explicitly for server-side calls
    return {};
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit & { token?: string } = {}
  ): Promise<ApiResponse<T>> {
    try {
      // Handle token from options (for server-side) or get from localStorage (client-side)
      const authHeaders = options.token
        ? { Authorization: `Bearer ${options.token}` }
        : this.getAuthHeader();

      // Remove token from options before passing to fetch
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { token: _, ...fetchOptions } = options;

      const response = await fetch(`${this.baseUrl}${endpoint}`, {
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
          ...fetchOptions.headers,
        },
        // Simplified caching - let the calling code handle caching strategy
        cache: fetchOptions.method === "GET" ? "default" : "no-store",
        ...fetchOptions,
      });

      // Check content type to determine how to parse the response
      const contentType = response.headers.get("content-type");
      const isJson = contentType && contentType.includes("application/json");

      if (!response.ok) {
        let errorMessage = "An error occurred";

        try {
          if (isJson) {
            const errorData = await response.json();
            errorMessage =
              errorData.message ||
              errorData.error ||
              `HTTP ${response.status}: ${response.statusText}`;
          } else {
            const textData = await response.text();
            errorMessage =
              textData || `HTTP ${response.status}: ${response.statusText}`;
          }
        } catch {
          errorMessage = `HTTP ${response.status}: ${response.statusText}`;
        }

        return {
          success: false,
          error: errorMessage,
        };
      }

      // Handle successful responses
      try {
        if (isJson) {
          const data = await response.json();
          return {
            success: true,
            data,
          };
        } else {
          // Handle plain text responses (like JWT tokens)
          const textData = await response.text();
          return {
            success: true,
            data: textData as T,
          };
        }
      } catch {
        return {
          success: false,
          error: "Failed to parse response data",
        };
      }
    } catch (error) {
      const errorInfo = handleApiError(error);
      return {
        success: false,
        error: errorInfo.message,
      };
    }
  }

  // Auth endpoints
  async login(credentials: {
    username: string;
    password: string;
  }): Promise<ApiResponse<{ token: string; user: User }>> {
    return this.request("/users/login", {
      method: "POST",
      body: JSON.stringify(credentials),
    });
  }

  async register(userData: {
    username: string;
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
    role: "JOB_SEEKER" | "EMPLOYER" | "ADMIN";
  }): Promise<ApiResponse<{ token: string; user: User }>> {
    return this.request("/users/register", {
      method: "POST",
      body: JSON.stringify(userData),
    });
  }

  async getProfile(): Promise<ApiResponse<User>> {
    return this.request("/users/profile");
  }

  async updateProfile(userData: Partial<User>): Promise<ApiResponse<User>> {
    return this.request("/users/profile", {
      method: "PUT",
      body: JSON.stringify(userData),
    });
  }

  // Jobs endpoints
  async getJobs(params?: {
    search?: string;
    location?: string;
    company?: string;
  }): Promise<ApiResponse<Job[]>> {
    const searchParams = new URLSearchParams();
    if (params?.search) searchParams.append("search", params.search);
    if (params?.location) searchParams.append("location", params.location);
    if (params?.company) searchParams.append("company", params.company);

    const queryString = searchParams.toString();
    return this.request(`/jobs${queryString ? `?${queryString}` : ""}`);
  }

  async getJob(id: number): Promise<ApiResponse<Job>> {
    return this.request(`/jobs/${id}`);
  }

  async createJob(
    jobData: Omit<Job, "id" | "createdAt" | "employerId">
  ): Promise<ApiResponse<Job>> {
    return this.request("/jobs", {
      method: "POST",
      body: JSON.stringify(jobData),
    });
  }

  async updateJob(
    id: number,
    jobData: Partial<Job>
  ): Promise<ApiResponse<Job>> {
    return this.request(`/jobs/${id}`, {
      method: "PUT",
      body: JSON.stringify(jobData),
    });
  }

  async deleteJob(id: number): Promise<ApiResponse<void>> {
    return this.request(`/jobs/${id}`, {
      method: "DELETE",
    });
  }

  // Applications endpoints
  async applyForJob(jobId: number): Promise<ApiResponse<JobApplication>> {
    return this.request("/applications", {
      method: "POST",
      body: JSON.stringify({ jobId }),
    });
  }

  async getUserApplications(
    userId: number
  ): Promise<ApiResponse<JobApplication[]>> {
    return this.request(`/applications/user/${userId}`);
  }

  async getJobApplications(
    jobId: number
  ): Promise<ApiResponse<JobApplication[]>> {
    return this.request(`/applications/job/${jobId}`);
  }

  async updateApplicationStatus(
    applicationId: number,
    status: JobApplication["status"]
  ): Promise<ApiResponse<JobApplication>> {
    return this.request(`/applications/${applicationId}/status`, {
      method: "PUT",
      body: JSON.stringify({ status }),
    });
  }

  async deleteApplication(applicationId: number): Promise<ApiResponse<void>> {
    return this.request(`/applications/${applicationId}`, {
      method: "DELETE",
    });
  }

  // Profile endpoints
  async getUserProfile(userId: number): Promise<ApiResponse<Profile>> {
    return this.request(`/profiles/${userId}`);
  }

  async updateUserProfile(
    userId: number,
    profileData: Partial<Profile>
  ): Promise<ApiResponse<Profile>> {
    return this.request(`/profiles/${userId}`, {
      method: "PUT",
      body: JSON.stringify(profileData),
    });
  }

  // Skills endpoints
  async addSkill(
    userId: number,
    skill: Omit<Skill, "id">
  ): Promise<ApiResponse<Skill>> {
    return this.request(`/profiles/${userId}/skills`, {
      method: "POST",
      body: JSON.stringify(skill),
    });
  }

  async removeSkill(
    userId: number,
    skillId: number
  ): Promise<ApiResponse<void>> {
    return this.request(`/profiles/${userId}/skills/${skillId}`, {
      method: "DELETE",
    });
  }

  // Education endpoints
  async addEducation(
    userId: number,
    education: Omit<Education, "id">
  ): Promise<ApiResponse<Education>> {
    return this.request(`/profiles/${userId}/education`, {
      method: "POST",
      body: JSON.stringify(education),
    });
  }

  async updateEducation(
    userId: number,
    eduId: number,
    education: Partial<Education>
  ): Promise<ApiResponse<Education>> {
    return this.request(`/profiles/${userId}/education/${eduId}`, {
      method: "PUT",
      body: JSON.stringify(education),
    });
  }

  async deleteEducation(
    userId: number,
    eduId: number
  ): Promise<ApiResponse<void>> {
    return this.request(`/profiles/${userId}/education/${eduId}`, {
      method: "DELETE",
    });
  }

  // Experience endpoints
  async addExperience(
    userId: number,
    experience: Omit<Experience, "id">
  ): Promise<ApiResponse<Experience>> {
    return this.request(`/profiles/${userId}/experience`, {
      method: "POST",
      body: JSON.stringify(experience),
    });
  }

  async updateExperience(
    userId: number,
    expId: number,
    experience: Partial<Experience>
  ): Promise<ApiResponse<Experience>> {
    return this.request(`/profiles/${userId}/experience/${expId}`, {
      method: "PUT",
      body: JSON.stringify(experience),
    });
  }

  async deleteExperience(
    userId: number,
    expId: number
  ): Promise<ApiResponse<void>> {
    return this.request(`/profiles/${userId}/experience/${expId}`, {
      method: "DELETE",
    });
  }

  // Portfolio endpoints
  async addPortfolio(
    userId: number,
    portfolio: Omit<Portfolio, "id">
  ): Promise<ApiResponse<Portfolio>> {
    return this.request(`/profiles/${userId}/portfolio`, {
      method: "POST",
      body: JSON.stringify(portfolio),
    });
  }

  async deletePortfolio(
    userId: number,
    portfolioId: number
  ): Promise<ApiResponse<void>> {
    return this.request(`/profiles/${userId}/portfolio/${portfolioId}`, {
      method: "DELETE",
    });
  }

  // Employer-specific endpoints
  async getEmployerJobs(employerId?: number): Promise<ApiResponse<Job[]>> {
    const endpoint = employerId
      ? `/jobs?employerId=${employerId}`
      : "/jobs/my-jobs";
    return this.request(endpoint);
  }

  async getEmployerDashboardStats(): Promise<
    ApiResponse<EmployerDashboardStats>
  > {
    return this.request("/employers/dashboard-stats");
  }

  async getJobApplicationsWithDetails(
    jobId: number
  ): Promise<ApiResponse<JobApplication[]>> {
    return this.request(`/applications/job/${jobId}/detailed`);
  }

  async updateEmployerProfile(profileData: {
    companyName?: string;
    companyDescription?: string;
    companyWebsite?: string;
    companyLocation?: string;
    contactPhone?: string;
  }): Promise<ApiResponse<User>> {
    return this.request("/users/profile/employer", {
      method: "PUT",
      body: JSON.stringify(profileData),
    });
  }

  async getApplicationsByJob(
    jobId: number
  ): Promise<ApiResponse<JobApplication[]>> {
    return this.request(`/jobs/${jobId}/applications`);
  }

  async bulkUpdateApplicationStatus(
    updates: {
      applicationId: number;
      status: JobApplication["status"];
    }[]
  ): Promise<ApiResponse<JobApplication[]>> {
    return this.request("/applications/bulk-status", {
      method: "PUT",
      body: JSON.stringify({ updates }),
    });
  }
}

// Client-side instance
export const apiClient = new ApiClient();

// Server-side API client factory
export class ServerApiClient extends ApiClient {
  private serverToken?: string;

  constructor(token?: string) {
    super();
    this.serverToken = token;
  }

  protected getAuthHeader(): Record<string, string> {
    return this.serverToken
      ? { Authorization: `Bearer ${this.serverToken}` }
      : {};
  }
}

// Helper function to create server-side API client
export function createServerApiClient(token?: string) {
  return new ServerApiClient(token);
}
