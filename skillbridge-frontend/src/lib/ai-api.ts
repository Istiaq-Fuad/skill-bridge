// AI API service functions and type definitions

export interface AiResponse {
  content: string;
  suggestedSkills?: string[];
  salaryRange?: {
    min: number;
    max: number;
    currency: string;
  };
  extractedData?: {
    personalInfo?: {
      name?: string;
      email?: string;
      phone?: string;
      location?: string;
    };
    skills?: string[];
    experience?: Array<{
      title: string;
      company: string;
      duration: string;
      description: string;
    }>;
    education?: Array<{
      degree: string;
      institution: string;
      year: string;
    }>;
  };
  qualityScore?: number;
  strengths?: string[];
  improvements?: string[];
  generatedAt?: string;
}

// Job Description API Types
export interface JobDescriptionGenerationRequest {
  jobTitle: string;
  company: string;
  industry?: string;
  experienceLevel: string;
  location: string;
  employmentType?: string;
  additionalRequirements?: string;
}

export interface JobDescriptionGenerationResponse {
  jobDescription: string;
  suggestedSkills?: string[];
  salaryRange?: {
    min: number;
    max: number;
  };
}

export interface JobDescriptionOptimizationRequest {
  jobDescription: string;
  targetAudience?: string;
  optimizationGoals?: string[];
}

export interface JobDescriptionOptimizationResponse {
  optimizedJobDescription: string;
  improvements: string[];
  seoScore?: number;
}

export interface SkillSuggestionResponse {
  skills: string[];
  categories: Record<string, string[]>;
}

export interface SalarySuggestionResponse {
  minSalary: number;
  maxSalary: number;
  currency: string;
  marketData?: {
    percentile25: number;
    percentile75: number;
    average: number;
  };
}

// Resume Parsing API Types
export interface ResumeParsingResponse {
  extractedData: {
    contactInfo: {
      name: string;
      email: string;
      phone: string;
      address: string;
      linkedin?: string;
      github?: string;
    };
    skills: string[];
    experience: Array<{
      company: string;
      position: string;
      startDate: string;
      endDate?: string;
      description: string;
      skills: string[];
    }>;
    education: Array<{
      institution: string;
      degree: string;
      field: string;
      startDate: string;
      endDate?: string;
      gpa?: string;
    }>;
  };
  qualityScore: number;
}

export interface ResumeQualityAnalysisRequest {
  resumeText: string;
}

export interface ResumeQualityAnalysisResponse {
  overallScore: number;
  strengths: string[];
  weaknesses: string[];
  improvements: string[];
  sections: Record<string, number>;
}

// Job Matching API Types
export interface CandidateMatchResponse {
  candidates: Array<{
    candidateId: string;
    name: string;
    email: string;
    compatibilityScore: number;
    skillsMatch: number;
    experienceMatch: number;
    reasons: string[];
  }>;
  totalCandidates: number;
}

export interface JobMatchResponse {
  jobs: Array<{
    jobId: string;
    jobTitle: string;
    company: string;
    compatibilityScore: number;
    skillsMatch: number;
    experienceMatch: number;
    locationMatch: number;
    salaryMatch: number;
    reasons: string[];
  }>;
  totalJobs: number;
}

// Admin Analytics API Types
export interface AnalyticsOverviewResponse {
  totalUsers: number;
  totalJobs: number;
  totalApplications: number;
  successRate: number;
  usersByRole?: Record<string, number>;
  jobsByCategory?: Record<string, number>;
}

export interface AnalyticsTrendsResponse {
  userGrowth: number[];
  jobPostings: number[];
  applicationVolume: number[];
  labels: string[];
  period: number;
}

export interface PlatformInsightsRequest {
  overview: AnalyticsOverviewResponse;
  trends: AnalyticsTrendsResponse;
  period: number;
}

export interface PlatformInsightsResponse {
  recommendations: Array<{
    category: string;
    priority: "high" | "medium" | "low";
    title: string;
    description: string;
    action: string;
  }>;
  keyInsights: string[];
}

export interface StrategicRecommendationsResponse {
  recommendations: string[];
  categories: Record<string, string[]>;
}

export interface JobModerationResponse {
  flagged: boolean;
  issues: string[];
  severity: "low" | "medium" | "high";
  suggestions: string[];
}

export interface HealthCheckResponse {
  status: "healthy" | "warning" | "critical";
  metrics: Array<{
    name: string;
    status: "healthy" | "warning" | "critical";
    value: number;
    threshold: number;
    description: string;
  }>;
  overallScore: number;
}

// Enhanced Employer API Types
export interface EnhancedEmployerDashboardResponse {
  stats: {
    totalJobs: number;
    totalApplications: number;
    responseRate: number;
    topPerformingJobs: Array<{
      id: number;
      title: string;
      applications: number;
      views: number;
    }>;
  };
  insights: string[];
  recommendations: string[];
}

export interface JobPerformanceAnalysisResponse {
  performanceScore: number;
  metrics: {
    views: number;
    applications: number;
    conversionRate: number;
  };
  benchmarks: {
    industryAverage: number;
    topPerformer: number;
  };
  suggestions: string[];
}

export interface RecommendedCandidatesResponse {
  candidates: Array<{
    id: number;
    name: string;
    email: string;
    matchScore: number;
    skills: string[];
    experience: string;
  }>;
  totalRecommendations: number;
}

export interface ApplicationInsightsResponse {
  totalApplications: number;
  applicationTrends: number[];
  topSources: Record<string, number>;
  insights: string[];
  recommendations: string[];
}

export interface JobDescriptionRequest {
  jobTitle: string;
  company: string;
  industry?: string;
  experienceLevel: string;
  location: string;
  employmentType?: string;
  additionalRequirements?: string;
}

export interface JobOptimizationRequest {
  jobDescription: string;
  targetAudience?: string;
  optimizationGoals?: string[];
}

export interface SkillSuggestionRequest {
  jobTitle: string;
  industry?: string;
  experienceLevel: string;
}

export interface SalarySuggestionRequest {
  jobTitle: string;
  location: string;
  experienceLevel: string;
  industry?: string;
}

export interface CandidateMatch {
  userId: number;
  username: string;
  email: string;
  compatibilityScore: number;
  skillMatchScore: number;
  experienceMatchScore: number;
  locationMatchScore: number;
}

export interface AdminAnalytics {
  totalUsers: number;
  totalJobs: number;
  usersByRole: Record<string, number>;
  jobsByEmployer: Record<string, number>;
}

export interface UserPreferences {
  preferredLocation?: string;
  salaryRange?: {
    min: number;
    max: number;
  };
  workType?: string;
  industries?: string[];
}

export interface UserFeedback {
  interestedJobs?: number[];
  rejectedJobs?: number[];
  appliedJobs?: number[];
}

export interface AnalyticsTrends {
  userRegistrations: Record<string, number>;
  jobPostings: Record<string, number>;
  applications: Record<string, number>;
}

export interface PlatformHealth {
  timestamp: string;
  totalUsers: number;
  totalJobs: number;
  activityScore: number;
  overallStatus: string;
}

// Enhanced Employer Dashboard Types
export interface EmployerDashboard {
  totalJobs: number;
  totalApplications: number;
  pendingApplications: number;
  aiRecommendations?: string;
  topPerformingJobs?: TopPerformingJob[];
}

export interface TopPerformingJob {
  jobId: number;
  title: string;
  applications: number;
}

export interface JobMatch {
  jobId: number;
  jobTitle: string;
  company?: string;
  matchScore: number;
  skillMatch: number;
  experienceMatch: number;
  locationMatch: number;
  salaryMatch: number;
  reasons: string[];
  postedDate: string;
  applications?: number;
}

export interface CandidateMatch {
  candidateId: number;
  candidateName: string;
  title: string;
  profilePicture?: string;
  experience: string;
  matchScore: number;
  skillMatch: number;
  experienceMatch: number;
  locationMatch: number;
  salaryMatch?: number;
  reasons: string[];
  skills: string[];
  location?: string;
}

// Enhanced Employer Response Types
export interface EnhancedEmployerDashboardResponse {
  totalJobs: number;
  totalApplications: number;
  pendingApplications: number;
  aiRecommendations?: string;
  topPerformingJobs?: TopPerformingJob[];
}

export interface JobPerformanceAnalysisResponse {
  content: string;
  confidence: number;
  timestamp: string;
}

export interface RecommendedCandidatesResponse {
  jobId: number;
  jobTitle: string;
  recommendedCandidates: CandidateMatch[];
  totalRecommendations: number;
}

export interface ApplicationInsightsResponse {
  content: string;
  confidence: number;
  timestamp: string;
}

// Note: AI API functions are now implemented directly in the ApiClient class (api.ts)
// This file only contains type definitions for AI API responses and requests
