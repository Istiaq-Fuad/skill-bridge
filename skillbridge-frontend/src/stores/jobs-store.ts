import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";
import { apiClient, Job } from "@/lib/api";

interface JobsState {
  // State
  jobs: Job[];
  currentJob: Job | null;
  isLoading: boolean;
  error: string | null;
  searchFilters: {
    search?: string;
    location?: string;
    company?: string;
  };
  pagination: {
    page: number;
    size: number;
    total: number;
    totalPages: number;
  };

  // Actions
  fetchJobs: (params?: {
    search?: string;
    location?: string;
    company?: string;
    page?: number;
    size?: number;
  }) => Promise<void>;
  fetchJobById: (id: number) => Promise<void>;
  createJob: (
    jobData: Omit<Job, "id" | "createdAt" | "employerId">
  ) => Promise<{ success: boolean; error?: string }>;
  updateJob: (
    id: number,
    jobData: Partial<Job>
  ) => Promise<{ success: boolean; error?: string }>;
  deleteJob: (id: number) => Promise<{ success: boolean; error?: string }>;
  setSearchFilters: (filters: Partial<JobsState["searchFilters"]>) => void;
  clearCurrentJob: () => void;
  clearError: () => void;
  setLoading: (loading: boolean) => void;
}

export const useJobsStore = create<JobsState>()(
  devtools(
    immer((set) => ({
      // Initial state
      jobs: [],
      currentJob: null,
      isLoading: false,
      error: null,
      searchFilters: {},
      pagination: {
        page: 0,
        size: 10,
        total: 0,
        totalPages: 0,
      },

      // Actions
      fetchJobs: async (params) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
          if (params) {
            state.searchFilters = { ...state.searchFilters, ...params };
          }
        });

        try {
          const response = await apiClient.getJobs(params);

          if (response.success && response.data) {
            set((state) => {
              state.jobs = response.data || [];
              state.isLoading = false;
              if (params?.page !== undefined) {
                state.pagination.page = params.page;
              }
              if (params?.size !== undefined) {
                state.pagination.size = params.size;
              }
            });
          } else {
            set((state) => {
              state.error = response.error || "Failed to fetch jobs";
              state.isLoading = false;
            });
          }
        } catch (error) {
          set((state) => {
            state.error =
              error instanceof Error ? error.message : "Failed to fetch jobs";
            state.isLoading = false;
          });
        }
      },

      fetchJobById: async (id: number) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          const response = await apiClient.getJob(id);

          if (response.success && response.data) {
            set((state) => {
              state.currentJob = response.data || null;
              state.isLoading = false;
            });
          } else {
            set((state) => {
              state.error = response.error || "Failed to fetch job";
              state.isLoading = false;
            });
          }
        } catch (error) {
          set((state) => {
            state.error =
              error instanceof Error ? error.message : "Failed to fetch job";
            state.isLoading = false;
          });
        }
      },

      createJob: async (jobData) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          const response = await apiClient.createJob(jobData);

          if (response.success && response.data) {
            set((state) => {
              state.jobs.unshift(response.data!);
              state.isLoading = false;
            });
            return { success: true };
          } else {
            set((state) => {
              state.error = response.error || "Failed to create job";
              state.isLoading = false;
            });
            return {
              success: false,
              error: response.error || "Failed to create job",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Failed to create job";
          set((state) => {
            state.error = errorMessage;
            state.isLoading = false;
          });
          return { success: false, error: errorMessage };
        }
      },

      updateJob: async (id, jobData) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          const response = await apiClient.updateJob(id, jobData);

          if (response.success && response.data) {
            set((state) => {
              const index = state.jobs.findIndex((job) => job.id === id);
              if (index !== -1) {
                state.jobs[index] = response.data!;
              }
              if (state.currentJob?.id === id) {
                state.currentJob = response.data!;
              }
              state.isLoading = false;
            });
            return { success: true };
          } else {
            set((state) => {
              state.error = response.error || "Failed to update job";
              state.isLoading = false;
            });
            return {
              success: false,
              error: response.error || "Failed to update job",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Failed to update job";
          set((state) => {
            state.error = errorMessage;
            state.isLoading = false;
          });
          return { success: false, error: errorMessage };
        }
      },

      deleteJob: async (id) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          const response = await apiClient.deleteJob(id);

          if (response.success) {
            set((state) => {
              state.jobs = state.jobs.filter((job) => job.id !== id);
              if (state.currentJob?.id === id) {
                state.currentJob = null;
              }
              state.isLoading = false;
            });
            return { success: true };
          } else {
            set((state) => {
              state.error = response.error || "Failed to delete job";
              state.isLoading = false;
            });
            return {
              success: false,
              error: response.error || "Failed to delete job",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Failed to delete job";
          set((state) => {
            state.error = errorMessage;
            state.isLoading = false;
          });
          return { success: false, error: errorMessage };
        }
      },

      setSearchFilters: (filters) => {
        set((state) => {
          state.searchFilters = { ...state.searchFilters, ...filters };
        });
      },

      clearCurrentJob: () => {
        set((state) => {
          state.currentJob = null;
        });
      },

      clearError: () => {
        set((state) => {
          state.error = null;
        });
      },

      setLoading: (loading: boolean) => {
        set((state) => {
          state.isLoading = loading;
        });
      },
    })),
    {
      name: "jobs-store",
    }
  )
);

// Selectors
export const useJobs = () => useJobsStore((state) => state.jobs);
export const useCurrentJob = () => useJobsStore((state) => state.currentJob);
export const useJobsLoading = () => useJobsStore((state) => state.isLoading);
export const useJobsError = () => useJobsStore((state) => state.error);
export const useSearchFilters = () =>
  useJobsStore((state) => state.searchFilters);
