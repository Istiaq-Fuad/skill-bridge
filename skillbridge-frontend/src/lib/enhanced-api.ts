// Enhanced API endpoints for the new backend functionality
import { ApiResponse } from "./api";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

export interface EnhancedApiClient {
  // Employer endpoints
  getEmployerDashboard(): Promise<ApiResponse<EmployerDashboardData>>;
  generateJobDescription(
    data: JobDescriptionRequest
  ): Promise<ApiResponse<JobDescriptionResponse>>;
  enhanceJobDescription(
    data: JobDescriptionEnhanceRequest
  ): Promise<ApiResponse<JobDescriptionResponse>>;
  getRankedCandidates(jobId: number): Promise<ApiResponse<CandidateRanking[]>>;
  getJobAnalytics(jobId: number): Promise<ApiResponse<JobAnalyticsData>>;
  getEmployerAnalytics(): Promise<ApiResponse<EmployerAnalyticsData>>;
  getApplicationInsights(): Promise<ApiResponse<ApplicationInsightsData>>;
  getCandidateInsights(): Promise<ApiResponse<CandidateInsightsData>>;

  // Job Seeker endpoints
  getJobSeekerDashboard(): Promise<ApiResponse<JobSeekerDashboardData>>;
  uploadResume(formData: FormData): Promise<ApiResponse<FileUploadResponse>>;
  parseResume(fileUrl: string): Promise<ApiResponse<ResumeParseResponse>>;
  generateResume(
    profileData: ResumeGenerationRequest
  ): Promise<ApiResponse<ResumeGenerationResponse>>;
  optimizeResume(
    data: ResumeOptimizationRequest
  ): Promise<ApiResponse<ResumeOptimizationResponse>>;
  getJobRecommendations(): Promise<ApiResponse<JobRecommendation[]>>;
  analyzeSkillGap(jobId: number): Promise<ApiResponse<SkillGapAnalysis>>;
  getCareerAdvice(query: string): Promise<ApiResponse<CareerAdviceResponse>>;
  getInterviewPrep(jobId: number): Promise<ApiResponse<InterviewPrepResponse>>;
  getJobSeekerAnalytics(): Promise<ApiResponse<JobSeekerAnalyticsData>>;

  // Admin endpoints
  getAdminDashboard(): Promise<ApiResponse<AdminDashboardData>>;
  getComprehensiveDashboard(): Promise<ApiResponse<ComprehensiveDashboardData>>;
  getUsersWithPagination(
    params: UserPaginationParams
  ): Promise<ApiResponse<PaginatedUsers>>;
  getUserProfile(userId: number): Promise<ApiResponse<UserProfileDetails>>;
  updateUserStatus(
    userId: number,
    isActive: boolean
  ): Promise<ApiResponse<User>>;
  getJobAnalyticsAdmin(): Promise<ApiResponse<AdminJobAnalytics>>;
  getApplicationAnalyticsAdmin(): Promise<
    ApiResponse<AdminApplicationAnalytics>
  >;
  getSystemHealth(): Promise<ApiResponse<SystemHealthData>>;
  getUserActivityAnalytics(): Promise<ApiResponse<UserActivityData>>;
  getPlatformInsightsAI(): Promise<ApiResponse<PlatformInsightsData>>;
  getPerformanceAnalytics(): Promise<ApiResponse<PerformanceAnalyticsData>>;
  getPlatformUsageStatistics(): Promise<ApiResponse<PlatformUsageData>>;
}

// Type definitions
export interface EmployerDashboardData {
  totalJobs: number;
  totalApplications: number;
  pendingApplications: number;
  activeJobs: number;
  responseRate: number;
  topPerformingJobs: Array<{
    id: number;
    title: string;
    applicationCount: number;
    responseRate: number;
  }>;
  recentApplications: Array<{
    id: number;
    jobTitle: string;
    candidateName: string;
    appliedDate: string;
    status: string;
  }>;
  hiringMetrics: {
    averageTimeToHire: number;
    offerAcceptanceRate: number;
    candidateQualityScore: number;
  };
}

export interface JobDescriptionRequest {
  jobTitle: string;
  companyInfo: string;
  requirements: string[];
  benefits?: string[];
  salaryRange?: string;
  experienceLevel: string;
}

export interface JobDescriptionResponse {
  title: string;
  description: string;
  requirements: string[];
  responsibilities: string[];
  qualifications: string[];
  benefits: string[];
  salaryGuidance?: string;
  aiGeneratedContent: boolean;
}

export interface JobDescriptionEnhanceRequest {
  existingDescription: string;
  targetAudience: string;
  focusAreas: string[];
}

export interface CandidateRanking {
  userId: number;
  candidateName: string;
  email: string;
  overallScore: number;
  skillsScore: number;
  experienceScore: number;
  educationScore: number;
  aiRecommendationScore: number;
  resumeUrl?: string;
  matchingSkills: string[];
  experienceSummary: string;
  aiInsights: string;
}

export interface JobAnalyticsData {
  jobId: number;
  title: string;
  views: number;
  applications: number;
  conversionRate: number;
  averageApplicationQuality: number;
  topSkillsInApplications: string[];
  applicationTrends: Array<{
    date: string;
    count: number;
  }>;
  candidateSourceAnalysis: Record<string, number>;
}

export interface EmployerAnalyticsData {
  jobsPosted: number;
  totalApplications: number;
  hiredCandidates: number;
  averageTimeToHire: number;
  popularJobTitles: Array<{ title: string; count: number }>;
  applicationTrends: Array<{ month: string; applications: number }>;
  candidateQualityTrends: Array<{ month: string; averageScore: number }>;
}

export interface ApplicationInsightsData {
  totalApplications: number;
  applicationsByStatus: Record<string, number>;
  averageApplicationsPerJob: number;
  topPerformingJobs: Array<{
    id: number;
    title: string;
    applicationCount: number;
  }>;
  candidateEngagementMetrics: {
    averageResponseTime: number;
    mostActiveTimeOfDay: string;
    geographicDistribution: Record<string, number>;
  };
}

export interface CandidateInsightsData {
  totalCandidates: number;
  averageExperience: number;
  topSkills: Array<{ skill: string; count: number }>;
  educationLevels: Record<string, number>;
  experienceDistribution: Record<string, number>;
  candidateQualityTrends: Array<{ month: string; averageScore: number }>;
}

export interface JobSeekerDashboardData {
  profileCompleteness: number;
  applicationsSent: number;
  interviewsScheduled: number;
  jobRecommendations: number;
  profileViews: number;
  skillsAssessed: number;
  resumeScore: number;
  applicationsByStatus: Record<string, number>;
  recentApplications: Array<{
    id: number;
    jobTitle: string;
    company: string;
    appliedDate: string;
    status: string;
  }>;
  skillGaps: string[];
  recommendedActions: string[];
}

export interface FileUploadResponse {
  fileId: string;
  fileName: string;
  fileUrl: string;
  category: string;
  uploadedAt: string;
}

export interface ResumeParseResponse {
  extractedText: string;
  skills: string[];
  experience: Array<{
    company: string;
    position: string;
    duration: string;
    description: string;
  }>;
  education: Array<{
    institution: string;
    degree: string;
    field: string;
    graduationYear?: string;
  }>;
  contactInfo: {
    email?: string;
    phone?: string;
    location?: string;
  };
  summary?: string;
  aiAnalysis: string;
}

export interface ResumeGenerationRequest {
  personalInfo: {
    name: string;
    email: string;
    phone: string;
    location: string;
  };
  professionalSummary: string;
  skills: string[];
  experience: Array<{
    company: string;
    position: string;
    startDate: string;
    endDate?: string;
    description: string;
  }>;
  education: Array<{
    institution: string;
    degree: string;
    field: string;
    graduationYear: string;
  }>;
  template?: string;
}

export interface ResumeGenerationResponse {
  resumeContent: string;
  resumeUrl: string;
  suggestions: string[];
  aiEnhancements: string[];
}

export interface ResumeOptimizationRequest {
  resumeContent: string;
  targetJobDescription: string;
  focusAreas: string[];
}

export interface ResumeOptimizationResponse {
  optimizedContent: string;
  improvementSuggestions: string[];
  keywordOptimization: {
    addedKeywords: string[];
    optimizedSections: string[];
  };
  matchScore: number;
}

export interface JobRecommendation {
  jobId: number;
  title: string;
  company: string;
  location: string;
  salary?: string;
  matchScore: number;
  matchingSkills: string[];
  missingSkills: string[];
  reasonForRecommendation: string;
  applicationDeadline?: string;
}

export interface SkillGapAnalysis {
  jobId: number;
  jobTitle: string;
  requiredSkills: string[];
  userSkills: string[];
  matchingSkills: string[];
  missingSkills: string[];
  skillGapScore: number;
  recommendations: Array<{
    skill: string;
    priority: "HIGH" | "MEDIUM" | "LOW";
    learningResources: string[];
  }>;
  careerPathSuggestions: string[];
}

export interface CareerAdviceResponse {
  advice: string;
  actionItems: string[];
  resources: Array<{
    title: string;
    url: string;
    type: "course" | "article" | "video" | "book";
  }>;
  nextSteps: string[];
}

export interface InterviewPrepResponse {
  commonQuestions: Array<{
    question: string;
    category: string;
    suggestedAnswer: string;
  }>;
  companyInsights: string;
  roleSpecificTips: string[];
  technicalTopics: string[];
  behavioralTopics: string[];
}

export interface JobSeekerAnalyticsData {
  profileStrength: number;
  applicationSuccessRate: number;
  averageResponseTime: number;
  profileViews: number;
  skillsAssessmentResults: Record<string, number>;
  applicationTrends: Array<{ month: string; applications: number }>;
  interviewConversionRate: number;
}

export interface AdminDashboardData {
  totalUsers: number;
  usersByRole: Record<string, number>;
  totalJobs: number;
  totalApplications: number;
  applicationsByStatus: Record<string, number>;
  filesByCategory: Record<string, number>;
  newUsersLast30Days: number;
  platformHealth: string;
}

export interface ComprehensiveDashboardData extends AdminDashboardData {
  activeUsers: number;
  successRate: number;
  totalFiles: number;
  memoryUsage: {
    maxMemoryMB: number;
    usedMemoryMB: number;
    freeMemoryMB: number;
    memoryUsagePercent: number;
  };
  systemHealth: {
    databaseHealthy: boolean;
    fileStorageHealthy: boolean;
    overallStatus: string;
  };
}

export interface UserPaginationParams {
  page?: number;
  size?: number;
  role?: string;
  search?: string;
}

export interface PaginatedUsers {
  users: User[];
  totalUsers: number;
  currentPage: number;
  totalPages: number;
}

export interface UserProfileDetails {
  user: User;
  applications?: Array<object>;
  jobs?: Array<object>;
  files?: Array<object>;
  applicationCount?: number;
  successRate?: number;
  jobCount?: number;
  totalApplicationsReceived?: number;
  fileCount?: number;
}

export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: "JOB_SEEKER" | "EMPLOYER" | "ADMIN";
  active: boolean;
  companyName?: string;
  companyDescription?: string;
}

export interface AdminJobAnalytics {
  totalJobs: number;
  jobsByMonth: Record<string, number>;
  topEmployers: Record<string, number>;
  applicationStats: {
    averageApplicationsPerJob: number;
    maxApplicationsPerJob: number;
    minApplicationsPerJob: number;
  };
}

export interface AdminApplicationAnalytics {
  totalApplications: number;
  statusDistribution: Record<string, number>;
  successRate: number;
  applicationsByMonth: Record<string, number>;
}

export interface SystemHealthData {
  databaseHealthy: boolean;
  fileStorageHealthy: boolean;
  memoryStats: {
    maxMemory: string;
    totalMemory: string;
    usedMemory: string;
    freeMemory: string;
    memoryUsagePercent: number;
  };
  overallStatus: string;
  timestamp: string;
}

export interface UserActivityData {
  activeJobSeekers: number;
  activeEmployers: number;
  fileUploadsbyCategory: Record<string, number>;
  totalFileUploads: number;
  applicationsPerRole: Record<string, number>;
  totalActiveUsers: number;
}

export interface PlatformInsightsData {
  aiAnalysis: string;
  platformSummary: string;
  generatedAt: string;
}

export interface PerformanceAnalyticsData {
  memoryStats: {
    maxMemoryMB: number;
    totalMemoryMB: number;
    usedMemoryMB: number;
    freeMemoryMB: number;
    memoryUsagePercent: number;
  };
  databaseHealthy: boolean;
  timestamp: string;
  overallStatus: string;
}

export interface PlatformUsageData {
  totalUsers: number;
  activeUsers: number;
  totalJobs: number;
  totalApplications: number;
  totalFiles: number;
  activeFiles: number;
  efficiencyMetrics: {
    userEngagementRate: number;
    averageApplicationsPerJob: number;
    fileStorageUtilization: number;
  };
}

class EnhancedApiClientImpl implements EnhancedApiClient {
  private baseUrl: string;

  constructor() {
    this.baseUrl = API_BASE_URL;
  }

  private getAuthHeader(): Record<string, string> {
    if (typeof window !== "undefined") {
      try {
        const authStorage = localStorage.getItem("auth-storage");
        if (authStorage) {
          const parsedAuth = JSON.parse(authStorage);
          const token = parsedAuth?.state?.token;
          if (token && token.trim()) {
            return { Authorization: `Bearer ${token}` };
          }
        }
      } catch (e) {
        console.error("Error reading auth token:", e);
      }

      const token = localStorage.getItem("token");
      if (token && token.trim()) {
        return { Authorization: `Bearer ${token}` };
      }
    }
    return {};
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    try {
      const authHeaders = this.getAuthHeader();

      const response = await fetch(`${this.baseUrl}${endpoint}`, {
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
          ...options.headers,
        },
        credentials: "include",
        cache: options.method === "GET" ? "default" : "no-store",
        ...options,
      });

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

      try {
        if (isJson) {
          const data = await response.json();
          return {
            success: true,
            data,
          };
        } else {
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
      return {
        success: false,
        error:
          error instanceof Error ? error.message : "Network error occurred",
      };
    }
  }

  // Employer endpoints implementation
  async getEmployerDashboard(): Promise<ApiResponse<EmployerDashboardData>> {
    return this.request("/employer/dashboard");
  }

  async generateJobDescription(
    data: JobDescriptionRequest
  ): Promise<ApiResponse<JobDescriptionResponse>> {
    return this.request("/employer/generate-job-description", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async enhanceJobDescription(
    data: JobDescriptionEnhanceRequest
  ): Promise<ApiResponse<JobDescriptionResponse>> {
    return this.request("/employer/enhance-job-description", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async getRankedCandidates(
    jobId: number
  ): Promise<ApiResponse<CandidateRanking[]>> {
    return this.request(`/employer/jobs/${jobId}/candidates/ranked`);
  }

  async getJobAnalytics(jobId: number): Promise<ApiResponse<JobAnalyticsData>> {
    return this.request(`/employer/jobs/${jobId}/analytics`);
  }

  async getEmployerAnalytics(): Promise<ApiResponse<EmployerAnalyticsData>> {
    return this.request("/employer/analytics/overview");
  }

  async getApplicationInsights(): Promise<
    ApiResponse<ApplicationInsightsData>
  > {
    return this.request("/employer/analytics/applications");
  }

  async getCandidateInsights(): Promise<ApiResponse<CandidateInsightsData>> {
    return this.request("/employer/analytics/candidates");
  }

  // Job Seeker endpoints implementation
  async getJobSeekerDashboard(): Promise<ApiResponse<JobSeekerDashboardData>> {
    return this.request("/job-seeker/dashboard");
  }

  async uploadResume(
    formData: FormData
  ): Promise<ApiResponse<FileUploadResponse>> {
    const authHeaders = this.getAuthHeader();
    return this.request("/job-seeker/resume/upload", {
      method: "POST",
      headers: authHeaders, // Don't set Content-Type for FormData
      body: formData,
    });
  }

  async parseResume(
    fileUrl: string
  ): Promise<ApiResponse<ResumeParseResponse>> {
    return this.request("/job-seeker/resume/parse", {
      method: "POST",
      body: JSON.stringify({ fileUrl }),
    });
  }

  async generateResume(
    profileData: ResumeGenerationRequest
  ): Promise<ApiResponse<ResumeGenerationResponse>> {
    return this.request("/job-seeker/resume/generate", {
      method: "POST",
      body: JSON.stringify(profileData),
    });
  }

  async optimizeResume(
    data: ResumeOptimizationRequest
  ): Promise<ApiResponse<ResumeOptimizationResponse>> {
    return this.request("/job-seeker/resume/optimize", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async getJobRecommendations(): Promise<ApiResponse<JobRecommendation[]>> {
    return this.request("/job-seeker/jobs/recommendations");
  }

  async analyzeSkillGap(jobId: number): Promise<ApiResponse<SkillGapAnalysis>> {
    return this.request(`/job-seeker/analysis/skill-gap/${jobId}`);
  }

  async getCareerAdvice(
    query: string
  ): Promise<ApiResponse<CareerAdviceResponse>> {
    return this.request("/job-seeker/advice/career", {
      method: "POST",
      body: JSON.stringify({ query }),
    });
  }

  async getInterviewPrep(
    jobId: number
  ): Promise<ApiResponse<InterviewPrepResponse>> {
    return this.request(`/job-seeker/advice/interview/${jobId}`);
  }

  async getJobSeekerAnalytics(): Promise<ApiResponse<JobSeekerAnalyticsData>> {
    return this.request("/job-seeker/analytics/profile");
  }

  // Admin endpoints implementation
  async getAdminDashboard(): Promise<ApiResponse<AdminDashboardData>> {
    return this.request("/admin/dashboard");
  }

  async getComprehensiveDashboard(): Promise<
    ApiResponse<ComprehensiveDashboardData>
  > {
    return this.request("/admin/dashboard/comprehensive");
  }

  async getUsersWithPagination(
    params: UserPaginationParams
  ): Promise<ApiResponse<PaginatedUsers>> {
    const queryString = new URLSearchParams(
      params as Record<string, string>
    ).toString();
    return this.request(`/admin/users?${queryString}`);
  }

  async getUserProfile(
    userId: number
  ): Promise<ApiResponse<UserProfileDetails>> {
    return this.request(`/admin/users/${userId}/profile`);
  }

  async updateUserStatus(
    userId: number,
    isActive: boolean
  ): Promise<ApiResponse<User>> {
    return this.request(`/admin/users/${userId}/status`, {
      method: "PUT",
      body: JSON.stringify({ isActive }),
    });
  }

  async getJobAnalyticsAdmin(): Promise<ApiResponse<AdminJobAnalytics>> {
    return this.request("/admin/analytics/jobs");
  }

  async getApplicationAnalyticsAdmin(): Promise<
    ApiResponse<AdminApplicationAnalytics>
  > {
    return this.request("/admin/analytics/applications");
  }

  async getSystemHealth(): Promise<ApiResponse<SystemHealthData>> {
    return this.request("/admin/system/health");
  }

  async getUserActivityAnalytics(): Promise<ApiResponse<UserActivityData>> {
    return this.request("/admin/analytics/user-activity");
  }

  async getPlatformInsightsAI(): Promise<ApiResponse<PlatformInsightsData>> {
    return this.request("/admin/insights/platform");
  }

  async getPerformanceAnalytics(): Promise<
    ApiResponse<PerformanceAnalyticsData>
  > {
    return this.request("/admin/analytics/performance");
  }

  async getPlatformUsageStatistics(): Promise<ApiResponse<PlatformUsageData>> {
    return this.request("/admin/analytics/platform-usage");
  }
}

export const enhancedApiClient = new EnhancedApiClientImpl();
