import { create } from "zustand";
import { devtools } from "zustand/middleware";

export interface JobDescriptionGeneration {
  id: string;
  title: string;
  industry: string;
  experienceLevel: string;
  location: string;
  generatedDescription?: string;
  suggestedSkills?: string[];
  salaryRange?: { min: number; max: number };
  status: "idle" | "generating" | "completed" | "error";
  error?: string;
}

export interface ContactInfo {
  name: string;
  email: string;
  phone: string;
  address: string;
  linkedin?: string;
  github?: string;
}

export interface Experience {
  company: string;
  position: string;
  startDate: string;
  endDate?: string;
  description: string;
  skills: string[];
}

export interface Education {
  institution: string;
  degree: string;
  field: string;
  startDate: string;
  endDate?: string;
  gpa?: string;
}

export interface ResumeParseResult {
  id: string;
  fileName: string;
  extractedData?: {
    contactInfo: ContactInfo;
    skills: string[];
    experience: Experience[];
    education: Education[];
  };
  qualityScore?: number;
  qualityAnalysis?: {
    score: number;
    strengths: string[];
    weaknesses: string[];
    recommendations: string[];
  };
  status: "idle" | "parsing" | "completed" | "error";
  error?: string;
}

export interface JobMatch {
  jobId: string;
  jobTitle: string;
  company: string;
  compatibilityScore: number;
  skillsMatch: number;
  experienceMatch: number;
  locationMatch: number;
  salaryMatch: number;
  reasons: string[];
}

export interface CandidateMatch {
  candidateId: string;
  name: string;
  email: string;
  compatibilityScore: number;
  skillsMatch: number;
  experienceMatch: number;
  reasons: string[];
}

export interface AnalyticsDashboard {
  overview?: {
    totalUsers: number;
    totalJobs: number;
    totalApplications: number;
    successRate: number;
  };
  trends?: {
    userGrowth: number[];
    jobPostings: number[];
    applicationVolume: number[];
    labels: string[];
  };
  insights?: {
    recommendations: string[];
    keyMetrics: Record<string, number>;
  };
}

interface AiState {
  // Job Description Generation
  jobGenerations: JobDescriptionGeneration[];
  currentGeneration: JobDescriptionGeneration | null;

  // Resume Parsing
  resumeParses: ResumeParseResult[];
  currentParse: ResumeParseResult | null;

  // Job Matching
  jobMatches: JobMatch[];
  candidateMatches: CandidateMatch[];
  isLoadingMatches: boolean;

  // Analytics
  analyticsDashboard: AnalyticsDashboard;

  // General AI state
  isLoading: boolean;
  error: string | null;

  // Actions
  setJobGeneration: (generation: JobDescriptionGeneration) => void;
  updateJobGeneration: (
    id: string,
    updates: Partial<JobDescriptionGeneration>
  ) => void;
  clearJobGeneration: (id: string) => void;
  setCurrentGeneration: (generation: JobDescriptionGeneration | null) => void;

  setResumeParseResult: (result: ResumeParseResult) => void;
  updateResumeParseResult: (
    id: string,
    updates: Partial<ResumeParseResult>
  ) => void;
  clearResumeParseResult: (id: string) => void;
  setCurrentParse: (parse: ResumeParseResult | null) => void;

  setJobMatches: (matches: JobMatch[]) => void;
  setCandidateMatches: (matches: CandidateMatch[]) => void;
  clearMatches: () => void;
  setLoadingMatches: (loading: boolean) => void;

  setAnalyticsDashboard: (dashboard: AnalyticsDashboard) => void;

  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;

  reset: () => void;
}

const initialState = {
  jobGenerations: [],
  currentGeneration: null,
  resumeParses: [],
  currentParse: null,
  jobMatches: [],
  candidateMatches: [],
  isLoadingMatches: false,
  analyticsDashboard: {},
  isLoading: false,
  error: null,
};

export const useAiStore = create<AiState>()(
  devtools(
    (set) => ({
      ...initialState,

      // Job Generation Actions
      setJobGeneration: (generation) =>
        set(
          (state) => ({
            jobGenerations: [...state.jobGenerations, generation],
          }),
          false,
          "setJobGeneration"
        ),

      updateJobGeneration: (id, updates) =>
        set(
          (state) => ({
            jobGenerations: state.jobGenerations.map((gen) =>
              gen.id === id ? { ...gen, ...updates } : gen
            ),
            currentGeneration:
              state.currentGeneration?.id === id
                ? { ...state.currentGeneration, ...updates }
                : state.currentGeneration,
          }),
          false,
          "updateJobGeneration"
        ),

      clearJobGeneration: (id) =>
        set(
          (state) => ({
            jobGenerations: state.jobGenerations.filter((gen) => gen.id !== id),
            currentGeneration:
              state.currentGeneration?.id === id
                ? null
                : state.currentGeneration,
          }),
          false,
          "clearJobGeneration"
        ),

      setCurrentGeneration: (generation) =>
        set({ currentGeneration: generation }, false, "setCurrentGeneration"),

      // Resume Parse Actions
      setResumeParseResult: (result) =>
        set(
          (state) => ({
            resumeParses: [...state.resumeParses, result],
          }),
          false,
          "setResumeParseResult"
        ),

      updateResumeParseResult: (id, updates) =>
        set(
          (state) => ({
            resumeParses: state.resumeParses.map((parse) =>
              parse.id === id ? { ...parse, ...updates } : parse
            ),
            currentParse:
              state.currentParse?.id === id
                ? { ...state.currentParse, ...updates }
                : state.currentParse,
          }),
          false,
          "updateResumeParseResult"
        ),

      clearResumeParseResult: (id) =>
        set(
          (state) => ({
            resumeParses: state.resumeParses.filter((parse) => parse.id !== id),
            currentParse:
              state.currentParse?.id === id ? null : state.currentParse,
          }),
          false,
          "clearResumeParseResult"
        ),

      setCurrentParse: (parse) =>
        set({ currentParse: parse }, false, "setCurrentParse"),

      // Matching Actions
      setJobMatches: (matches) =>
        set({ jobMatches: matches }, false, "setJobMatches"),

      setCandidateMatches: (matches) =>
        set({ candidateMatches: matches }, false, "setCandidateMatches"),

      clearMatches: () =>
        set({ jobMatches: [], candidateMatches: [] }, false, "clearMatches"),

      setLoadingMatches: (loading) =>
        set({ isLoadingMatches: loading }, false, "setLoadingMatches"),

      // Analytics Actions
      setAnalyticsDashboard: (dashboard) =>
        set({ analyticsDashboard: dashboard }, false, "setAnalyticsDashboard"),

      // General Actions
      setLoading: (loading) => set({ isLoading: loading }, false, "setLoading"),

      setError: (error) => set({ error }, false, "setError"),

      reset: () => set(initialState, false, "reset"),
    }),
    {
      name: "ai-store",
    }
  )
);

// Selectors
export const useJobGenerations = () =>
  useAiStore((state) => state.jobGenerations);
export const useCurrentGeneration = () =>
  useAiStore((state) => state.currentGeneration);
export const useResumeParses = () => useAiStore((state) => state.resumeParses);
export const useCurrentParse = () => useAiStore((state) => state.currentParse);
export const useJobMatches = () => useAiStore((state) => state.jobMatches);
export const useCandidateMatches = () =>
  useAiStore((state) => state.candidateMatches);
export const useAnalyticsDashboard = () =>
  useAiStore((state) => state.analyticsDashboard);
