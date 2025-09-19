"use client";

import { useCallback, useEffect } from "react";
import { apiClient } from "@/lib/api";
import { useProfileStore } from "@/stores";

export function useProfile(userId: number) {
  const { profile, isLoading, error, setProfile, setLoading, setError } =
    useProfileStore();

  const fetchProfile = useCallback(
    async (id: number) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.getUserProfile(id);

        if (response.success && response.data) {
          setProfile(response.data);
          return response.data;
        } else {
          setError(response.error || "Failed to fetch profile");
          return null;
        }
      } catch (error) {
        setError(
          error instanceof Error ? error.message : "Failed to fetch profile"
        );
        return null;
      } finally {
        setLoading(false);
      }
    },
    [setProfile, setLoading, setError]
  );

  useEffect(() => {
    if (
      userId &&
      (!profile || profile.userId !== userId) &&
      !isLoading &&
      !error
    ) {
      fetchProfile(userId);
    }
  }, [userId, profile, isLoading, error, fetchProfile]);

  const refreshProfile = useCallback(() => {
    return fetchProfile(userId);
  }, [fetchProfile, userId]);

  return {
    profile,
    isLoading,
    error,
    fetchProfile,
    refreshProfile,
  };
}

export function useUpdateProfile() {
  const { updateProfile, setLoading, setError } = useProfileStore();

  const updateProfileData = useCallback(
    async (
      userId: number,
      profileData: Parameters<typeof apiClient.updateUserProfile>[1]
    ) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.updateUserProfile(userId, profileData);

        if (response.success && response.data) {
          updateProfile(response.data);
          return { success: true, profile: response.data };
        } else {
          setError(response.error || "Failed to update profile");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to update profile";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [updateProfile, setLoading, setError]
  );

  return { updateProfile: updateProfileData };
}

export function useSkillsManagement(userId: number) {
  const { addSkill, removeSkill, setLoading, setError } = useProfileStore();

  const addSkillToProfile = useCallback(
    async (skill: Parameters<typeof apiClient.addSkill>[1]) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.addSkill(userId, skill);

        if (response.success && response.data) {
          addSkill(response.data);
          return { success: true, skill: response.data };
        } else {
          setError(response.error || "Failed to add skill");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to add skill";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [userId, addSkill, setLoading, setError]
  );

  const removeSkillFromProfile = useCallback(
    async (skillId: number) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.removeSkill(userId, skillId);

        if (response.success) {
          removeSkill(skillId);
          return { success: true };
        } else {
          setError(response.error || "Failed to remove skill");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to remove skill";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [userId, removeSkill, setLoading, setError]
  );

  return {
    addSkill: addSkillToProfile,
    removeSkill: removeSkillFromProfile,
  };
}

export function useEducationManagement(userId: number) {
  const {
    addEducation,
    updateEducation,
    removeEducation,
    setLoading,
    setError,
  } = useProfileStore();

  const addEducationToProfile = useCallback(
    async (education: Parameters<typeof apiClient.addEducation>[1]) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.addEducation(userId, education);

        if (response.success && response.data) {
          addEducation(response.data);
          return { success: true, education: response.data };
        } else {
          setError(response.error || "Failed to add education");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to add education";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [userId, addEducation, setLoading, setError]
  );

  const updateEducationInProfile = useCallback(
    async (
      eduId: number,
      educationData: Parameters<typeof apiClient.updateEducation>[2]
    ) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.updateEducation(
          userId,
          eduId,
          educationData
        );

        if (response.success && response.data) {
          updateEducation(eduId, response.data);
          return { success: true, education: response.data };
        } else {
          setError(response.error || "Failed to update education");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to update education";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [userId, updateEducation, setLoading, setError]
  );

  const removeEducationFromProfile = useCallback(
    async (eduId: number) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.deleteEducation(userId, eduId);

        if (response.success) {
          removeEducation(eduId);
          return { success: true };
        } else {
          setError(response.error || "Failed to remove education");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to remove education";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [userId, removeEducation, setLoading, setError]
  );

  return {
    addEducation: addEducationToProfile,
    updateEducation: updateEducationInProfile,
    removeEducation: removeEducationFromProfile,
  };
}

export function useExperienceManagement(userId: number) {
  const {
    addExperience,
    updateExperience,
    removeExperience,
    setLoading,
    setError,
  } = useProfileStore();

  const addExperienceToProfile = useCallback(
    async (experience: Parameters<typeof apiClient.addExperience>[1]) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.addExperience(userId, experience);

        if (response.success && response.data) {
          addExperience(response.data);
          return { success: true, experience: response.data };
        } else {
          setError(response.error || "Failed to add experience");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to add experience";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [userId, addExperience, setLoading, setError]
  );

  const updateExperienceInProfile = useCallback(
    async (
      expId: number,
      experienceData: Parameters<typeof apiClient.updateExperience>[2]
    ) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.updateExperience(
          userId,
          expId,
          experienceData
        );

        if (response.success && response.data) {
          updateExperience(expId, response.data);
          return { success: true, experience: response.data };
        } else {
          setError(response.error || "Failed to update experience");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error
            ? error.message
            : "Failed to update experience";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [userId, updateExperience, setLoading, setError]
  );

  const removeExperienceFromProfile = useCallback(
    async (expId: number) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.deleteExperience(userId, expId);

        if (response.success) {
          removeExperience(expId);
          return { success: true };
        } else {
          setError(response.error || "Failed to remove experience");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error
            ? error.message
            : "Failed to remove experience";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [userId, removeExperience, setLoading, setError]
  );

  return {
    addExperience: addExperienceToProfile,
    updateExperience: updateExperienceInProfile,
    removeExperience: removeExperienceFromProfile,
  };
}

export function usePortfolioManagement(userId: number) {
  const { addPortfolio, removePortfolio, setLoading, setError } =
    useProfileStore();

  const addPortfolioToProfile = useCallback(
    async (portfolio: Parameters<typeof apiClient.addPortfolio>[1]) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.addPortfolio(userId, portfolio);

        if (response.success && response.data) {
          addPortfolio(response.data);
          return { success: true, portfolio: response.data };
        } else {
          setError(response.error || "Failed to add portfolio item");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error
            ? error.message
            : "Failed to add portfolio item";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [userId, addPortfolio, setLoading, setError]
  );

  const removePortfolioFromProfile = useCallback(
    async (portfolioId: number) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.deletePortfolio(userId, portfolioId);

        if (response.success) {
          removePortfolio(portfolioId);
          return { success: true };
        } else {
          setError(response.error || "Failed to remove portfolio item");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error
            ? error.message
            : "Failed to remove portfolio item";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [userId, removePortfolio, setLoading, setError]
  );

  return {
    addPortfolio: addPortfolioToProfile,
    removePortfolio: removePortfolioFromProfile,
  };
}
