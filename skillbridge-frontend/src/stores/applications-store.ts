import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { apiClient, JobApplication } from "@/lib/api";

interface ApplicationsState {
  // State
  userApplications: JobApplication[];
  jobApplications: JobApplication[];
  isLoading: boolean;
  error: string | null;

  // Actions
  applyForJob: (jobId: number) => Promise<{ success: boolean; error?: string }>;
  fetchUserApplications: (userId: number) => Promise<void>;
  fetchJobApplications: (jobId: number) => Promise<void>;
  updateApplicationStatus: (
    applicationId: number,
    status: JobApplication["status"]
  ) => Promise<{ success: boolean; error?: string }>;
  deleteApplication: (
    applicationId: number
  ) => Promise<{ success: boolean; error?: string }>;
  clearApplications: () => void;
  clearError: () => void;
  setLoading: (loading: boolean) => void;
}

export const useApplicationsStore = create<ApplicationsState>()(
  devtools(
    (set, get) => ({
      // Initial state
      userApplications: [],
      jobApplications: [],
      isLoading: false,
      error: null,

      // Actions
      applyForJob: async (jobId: number) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.applyForJob(jobId);

          if (response.success && response.data) {
            set({
              userApplications: [response.data, ...get().userApplications],
              isLoading: false,
            });
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to apply for job",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to apply for job",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Failed to apply for job";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      fetchUserApplications: async (userId: number) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.getUserApplications(userId);

          if (response.success && response.data) {
            set({
              userApplications: response.data || [],
              isLoading: false,
            });
          } else {
            set({
              error: response.error || "Failed to fetch applications",
              isLoading: false,
            });
          }
        } catch (error) {
          set({
            error:
              error instanceof Error
                ? error.message
                : "Failed to fetch applications",
            isLoading: false,
          });
        }
      },

      fetchJobApplications: async (jobId: number) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.getJobApplications(jobId);

          if (response.success && response.data) {
            set({
              jobApplications: response.data || [],
              isLoading: false,
            });
          } else {
            set({
              error: response.error || "Failed to fetch job applications",
              isLoading: false,
            });
          }
        } catch (error) {
          set({
            error:
              error instanceof Error
                ? error.message
                : "Failed to fetch job applications",
            isLoading: false,
          });
        }
      },

      updateApplicationStatus: async (
        applicationId: number,
        status: JobApplication["status"]
      ) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.updateApplicationStatus(
            applicationId,
            status
          );

          if (response.success && response.data) {
            const currentState = get();

            // Update in user applications
            const updatedUserApplications = currentState.userApplications.map(
              (app: JobApplication) =>
                app.id === applicationId ? response.data! : app
            );

            // Update in job applications
            const updatedJobApplications = currentState.jobApplications.map(
              (app: JobApplication) =>
                app.id === applicationId ? response.data! : app
            );

            set({
              userApplications: updatedUserApplications,
              jobApplications: updatedJobApplications,
              isLoading: false,
            });
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to update application status",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to update application status",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "Failed to update application status";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      deleteApplication: async (applicationId: number) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.deleteApplication(applicationId);

          if (response.success) {
            const currentState = get();
            set({
              userApplications: currentState.userApplications.filter(
                (app: JobApplication) => app.id !== applicationId
              ),
              jobApplications: currentState.jobApplications.filter(
                (app: JobApplication) => app.id !== applicationId
              ),
              isLoading: false,
            });
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to delete application",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to delete application",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "Failed to delete application";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      clearApplications: () => {
        set({ userApplications: [], jobApplications: [] });
      },

      clearError: () => {
        set({ error: null });
      },

      setLoading: (loading: boolean) => {
        set({ isLoading: loading });
      },
    }),
    {
      name: "applications-store",
    }
  )
);

// Selectors
export const useUserApplications = () =>
  useApplicationsStore((state) => state.userApplications);
export const useJobApplications = () =>
  useApplicationsStore((state) => state.jobApplications);
export const useApplicationsLoading = () =>
  useApplicationsStore((state) => state.isLoading);
export const useApplicationsError = () =>
  useApplicationsStore((state) => state.error);
