import { create } from "zustand";
import { devtools } from "zustand/middleware";
import {
  apiClient,
  Profile,
  Skill,
  Education,
  Experience,
  Portfolio,
} from "@/lib/api";

interface ProfileState {
  // State
  profile: Profile | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchProfile: (userId: number) => Promise<void>;
  updateProfile: (
    userId: number,
    profileData: Partial<Profile>
  ) => Promise<{ success: boolean; error?: string }>;

  // Skills actions
  addSkill: (
    userId: number,
    skill: Omit<Skill, "id">
  ) => Promise<{ success: boolean; error?: string }>;
  removeSkill: (
    userId: number,
    skillId: number
  ) => Promise<{ success: boolean; error?: string }>;

  // Education actions
  addEducation: (
    userId: number,
    education: Omit<Education, "id">
  ) => Promise<{ success: boolean; error?: string }>;
  updateEducation: (
    userId: number,
    eduId: number,
    education: Partial<Education>
  ) => Promise<{ success: boolean; error?: string }>;
  deleteEducation: (
    userId: number,
    eduId: number
  ) => Promise<{ success: boolean; error?: string }>;

  // Experience actions
  addExperience: (
    userId: number,
    experience: Omit<Experience, "id">
  ) => Promise<{ success: boolean; error?: string }>;
  updateExperience: (
    userId: number,
    expId: number,
    experience: Partial<Experience>
  ) => Promise<{ success: boolean; error?: string }>;
  deleteExperience: (
    userId: number,
    expId: number
  ) => Promise<{ success: boolean; error?: string }>;

  // Portfolio actions
  addPortfolio: (
    userId: number,
    portfolio: Omit<Portfolio, "id">
  ) => Promise<{ success: boolean; error?: string }>;
  deletePortfolio: (
    userId: number,
    portfolioId: number
  ) => Promise<{ success: boolean; error?: string }>;

  // Utility actions
  clearProfile: () => void;
  clearError: () => void;
  setLoading: (loading: boolean) => void;
}

export const useProfileStore = create<ProfileState>()(
  devtools(
    (set, get) => ({
      // Initial state
      profile: null,
      isLoading: false,
      error: null,

      // Actions
      fetchProfile: async (userId: number) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.getUserProfile(userId);

          if (response.success && response.data) {
            set({
              profile: response.data,
              isLoading: false,
            });
          } else {
            set({
              error: response.error || "Failed to fetch profile",
              isLoading: false,
            });
          }
        } catch (error) {
          set({
            error:
              error instanceof Error
                ? error.message
                : "Failed to fetch profile",
            isLoading: false,
          });
        }
      },

      updateProfile: async (userId: number, profileData: Partial<Profile>) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.updateUserProfile(
            userId,
            profileData
          );

          if (response.success && response.data) {
            set({
              profile: response.data,
              isLoading: false,
            });
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to update profile",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to update profile",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Failed to update profile";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      // Skills actions
      addSkill: async (userId: number, skill: Omit<Skill, "id">) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.addSkill(userId, skill);

          if (response.success && response.data) {
            const currentProfile = get().profile;
            if (currentProfile) {
              set({
                profile: {
                  ...currentProfile,
                  skills: [...currentProfile.skills, response.data],
                },
                isLoading: false,
              });
            }
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to add skill",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to add skill",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Failed to add skill";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      removeSkill: async (userId: number, skillId: number) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.removeSkill(userId, skillId);

          if (response.success) {
            const currentProfile = get().profile;
            if (currentProfile) {
              set({
                profile: {
                  ...currentProfile,
                  skills: currentProfile.skills.filter(
                    (skill) => skill.id !== skillId
                  ),
                },
                isLoading: false,
              });
            }
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to remove skill",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to remove skill",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Failed to remove skill";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      // Education actions
      addEducation: async (
        userId: number,
        education: Omit<Education, "id">
      ) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.addEducation(userId, education);

          if (response.success && response.data) {
            const currentProfile = get().profile;
            if (currentProfile) {
              set({
                profile: {
                  ...currentProfile,
                  education: [...currentProfile.education, response.data],
                },
                isLoading: false,
              });
            }
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to add education",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to add education",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Failed to add education";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      updateEducation: async (
        userId: number,
        eduId: number,
        education: Partial<Education>
      ) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.updateEducation(
            userId,
            eduId,
            education
          );

          if (response.success && response.data) {
            const currentProfile = get().profile;
            if (currentProfile) {
              set({
                profile: {
                  ...currentProfile,
                  education: currentProfile.education.map((edu) =>
                    edu.id === eduId ? response.data! : edu
                  ),
                },
                isLoading: false,
              });
            }
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to update education",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to update education",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "Failed to update education";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      deleteEducation: async (userId: number, eduId: number) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.deleteEducation(userId, eduId);

          if (response.success) {
            const currentProfile = get().profile;
            if (currentProfile) {
              set({
                profile: {
                  ...currentProfile,
                  education: currentProfile.education.filter(
                    (edu) => edu.id !== eduId
                  ),
                },
                isLoading: false,
              });
            }
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to delete education",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to delete education",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "Failed to delete education";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      // Experience actions
      addExperience: async (
        userId: number,
        experience: Omit<Experience, "id">
      ) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.addExperience(userId, experience);

          if (response.success && response.data) {
            const currentProfile = get().profile;
            if (currentProfile) {
              set({
                profile: {
                  ...currentProfile,
                  experience: [...currentProfile.experience, response.data],
                },
                isLoading: false,
              });
            }
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to add experience",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to add experience",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Failed to add experience";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      updateExperience: async (
        userId: number,
        expId: number,
        experience: Partial<Experience>
      ) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.updateExperience(
            userId,
            expId,
            experience
          );

          if (response.success && response.data) {
            const currentProfile = get().profile;
            if (currentProfile) {
              set({
                profile: {
                  ...currentProfile,
                  experience: currentProfile.experience.map((exp) =>
                    exp.id === expId ? response.data! : exp
                  ),
                },
                isLoading: false,
              });
            }
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to update experience",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to update experience",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "Failed to update experience";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      deleteExperience: async (userId: number, expId: number) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.deleteExperience(userId, expId);

          if (response.success) {
            const currentProfile = get().profile;
            if (currentProfile) {
              set({
                profile: {
                  ...currentProfile,
                  experience: currentProfile.experience.filter(
                    (exp) => exp.id !== expId
                  ),
                },
                isLoading: false,
              });
            }
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to delete experience",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to delete experience",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "Failed to delete experience";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      // Portfolio actions
      addPortfolio: async (
        userId: number,
        portfolio: Omit<Portfolio, "id">
      ) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.addPortfolio(userId, portfolio);

          if (response.success && response.data) {
            const currentProfile = get().profile;
            if (currentProfile) {
              set({
                profile: {
                  ...currentProfile,
                  portfolio: [...currentProfile.portfolio, response.data],
                },
                isLoading: false,
              });
            }
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to add portfolio item",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to add portfolio item",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "Failed to add portfolio item";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      deletePortfolio: async (userId: number, portfolioId: number) => {
        set({ isLoading: true, error: null });

        try {
          const response = await apiClient.deletePortfolio(userId, portfolioId);

          if (response.success) {
            const currentProfile = get().profile;
            if (currentProfile) {
              set({
                profile: {
                  ...currentProfile,
                  portfolio: currentProfile.portfolio.filter(
                    (item) => item.id !== portfolioId
                  ),
                },
                isLoading: false,
              });
            }
            return { success: true };
          } else {
            set({
              error: response.error || "Failed to delete portfolio item",
              isLoading: false,
            });
            return {
              success: false,
              error: response.error || "Failed to delete portfolio item",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "Failed to delete portfolio item";
          set({
            error: errorMessage,
            isLoading: false,
          });
          return { success: false, error: errorMessage };
        }
      },

      // Utility actions
      clearProfile: () => {
        set({ profile: null });
      },

      clearError: () => {
        set({ error: null });
      },

      setLoading: (loading: boolean) => {
        set({ isLoading: loading });
      },
    }),
    {
      name: "profile-store",
    }
  )
);

// Selectors
export const useProfile = () => useProfileStore((state) => state.profile);
export const useProfileLoading = () =>
  useProfileStore((state) => state.isLoading);
export const useProfileError = () => useProfileStore((state) => state.error);
export const useSkills = () =>
  useProfileStore((state) => state.profile?.skills || []);
export const useEducation = () =>
  useProfileStore((state) => state.profile?.education || []);
export const useExperience = () =>
  useProfileStore((state) => state.profile?.experience || []);
export const usePortfolio = () =>
  useProfileStore((state) => state.profile?.portfolio || []);
