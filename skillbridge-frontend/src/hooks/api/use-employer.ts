"use client";

import { useCallback } from "react";
import { apiClient, EmployerDashboardStats } from "@/lib/api";
import { useJobsStore } from "@/stores";
import { create } from "zustand";

// Employer-specific store
interface EmployerState {
  dashboardStats: EmployerDashboardStats | null;
  isLoadingStats: boolean;
  statsError: string | null;
}

interface EmployerActions {
  setDashboardStats: (stats: EmployerDashboardStats | null) => void;
  setLoadingStats: (loading: boolean) => void;
  setStatsError: (error: string | null) => void;
  clearEmployerData: () => void;
}

type EmployerStore = EmployerState & EmployerActions;

export const useEmployerStore = create<EmployerStore>((set) => ({
  // Initial state
  dashboardStats: null,
  isLoadingStats: false,
  statsError: null,

  // Actions
  setDashboardStats: (stats) => {
    set({ dashboardStats: stats, statsError: null });
  },

  setLoadingStats: (loading) => {
    set({ isLoadingStats: loading });
  },

  setStatsError: (error) => {
    set({ statsError: error });
  },

  clearEmployerData: () => {
    set({
      dashboardStats: null,
      isLoadingStats: false,
      statsError: null,
    });
  },
}));

// Hooks for employer functionality
export function useEmployerDashboardStats() {
  const {
    dashboardStats,
    isLoadingStats,
    statsError,
    setDashboardStats,
    setLoadingStats,
    setStatsError,
  } = useEmployerStore();

  const fetchStats = useCallback(async () => {
    try {
      setLoadingStats(true);
      setStatsError(null);

      const response = await apiClient.getEmployerDashboardStats();

      if (response.success && response.data) {
        setDashboardStats(response.data);
      } else {
        setStatsError(response.error || "Failed to fetch dashboard stats");
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : "Failed to fetch dashboard stats";
      setStatsError(errorMessage);
    } finally {
      setLoadingStats(false);
    }
  }, [setDashboardStats, setLoadingStats, setStatsError]);

  return {
    dashboardStats,
    isLoadingStats,
    statsError,
    fetchStats,
  };
}

export function useEmployerJobs() {
  const { jobs, setJobs, setLoading, setError } = useJobsStore();

  const fetchEmployerJobs = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await apiClient.getEmployerJobs();

      if (response.success && response.data) {
        setJobs(response.data);
      } else {
        setError(response.error || "Failed to fetch employer jobs");
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : "Failed to fetch employer jobs";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [setJobs, setLoading, setError]);

  return {
    employerJobs: jobs,
    isLoading: useJobsStore((state) => state.isLoading),
    error: useJobsStore((state) => state.error),
    fetchEmployerJobs,
  };
}

export function useUpdateEmployerProfile() {
  const updateProfile = useCallback(
    async (
      profileData: Parameters<typeof apiClient.updateEmployerProfile>[0]
    ) => {
      try {
        const response = await apiClient.updateEmployerProfile(profileData);

        if (response.success && response.data) {
          return { success: true, user: response.data };
        } else {
          return {
            success: false,
            error: response.error || "Failed to update profile",
          };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to update profile";
        return { success: false, error: errorMessage };
      }
    },
    []
  );

  return { updateProfile };
}
