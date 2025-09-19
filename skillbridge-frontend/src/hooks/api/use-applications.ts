"use client";

import { useCallback, useEffect } from "react";
import { apiClient } from "@/lib/api";
import { useApplicationsStore } from "@/stores";
import { createStandardError } from "@/lib/error-handler";

export function useApplications() {
  const { applications, isLoading, error, setLoading, setError } =
    useApplicationsStore();

  const fetchApplications = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      // Note: This would need to be adapted based on your API structure
      // Currently there's no generic get all applications endpoint
      // You might need to fetch user or job specific applications
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Failed to fetch applications"
      );
    } finally {
      setLoading(false);
    }
  }, [setLoading, setError]);

  return {
    applications,
    isLoading,
    error,
    fetchApplications,
  };
}

export function useUserApplications(userId: number) {
  const { userApplications, isLoading, error } = useApplicationsStore();

  const fetchUserApplications = useCallback(async (id: number) => {
    try {
      const { setLoading, setError, setUserApplications } =
        useApplicationsStore.getState();
      setLoading(true);
      setError(null);

      const response = await apiClient.getUserApplications(id);

      if (response.success && response.data) {
        setUserApplications(response.data, id);
        return response.data;
      } else {
        setError(response.error || "Failed to fetch user applications");
        return [];
      }
    } catch (error) {
      const standardError = createStandardError(error, "fetchUserApplications");
      const { setError } = useApplicationsStore.getState();
      setError(standardError.message);
      return [];
    } finally {
      const { setLoading } = useApplicationsStore.getState();
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // Only fetch if we have a valid userId
    if (userId && userId > 0) {
      const { isLoading, isUserDataFresh } = useApplicationsStore.getState();

      // Don't fetch if already loading or data is fresh
      if (isLoading || isUserDataFresh(userId)) {
        return;
      }

      const fetchData = async () => {
        try {
          const { setLoading, setError, setUserApplications } =
            useApplicationsStore.getState();
          setLoading(true);
          setError(null);

          const response = await apiClient.getUserApplications(userId);

          if (response.success && response.data) {
            setUserApplications(response.data, userId);
          } else {
            setError(response.error || "Failed to fetch user applications");
          }
        } catch (error) {
          const standardError = createStandardError(
            error,
            "fetchUserApplications"
          );
          const { setError, setLoading } = useApplicationsStore.getState();
          setError(standardError.message);
          setLoading(false);
        } finally {
          const { setLoading } = useApplicationsStore.getState();
          setLoading(false);
        }
      };

      fetchData();
    }
  }, [userId]);

  const refreshUserApplications = useCallback(() => {
    return fetchUserApplications(userId);
  }, [fetchUserApplications, userId]);

  return {
    userApplications,
    isLoading,
    error,
    fetchUserApplications,
    refreshUserApplications,
  };
}

export function useJobApplications(jobId: number) {
  const { jobApplications, isLoading, error } = useApplicationsStore();

  const fetchJobApplications = useCallback(async (id: number) => {
    try {
      const { setLoading, setError, setJobApplications } =
        useApplicationsStore.getState();
      setLoading(true);
      setError(null);

      const response = await apiClient.getJobApplications(id);

      if (response.success && response.data) {
        setJobApplications(response.data, id);
        return response.data;
      } else {
        setError(response.error || "Failed to fetch job applications");
        return [];
      }
    } catch (error) {
      const standardError = createStandardError(error, "fetchJobApplications");
      const { setError } = useApplicationsStore.getState();
      setError(standardError.message);
      return [];
    } finally {
      const { setLoading } = useApplicationsStore.getState();
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // Only fetch if we have a valid jobId
    if (jobId && jobId > 0) {
      const { isLoading, isJobDataFresh } = useApplicationsStore.getState();

      // Don't fetch if already loading or data is fresh
      if (isLoading || isJobDataFresh(jobId)) {
        return;
      }

      const fetchData = async () => {
        try {
          const { setLoading, setError, setJobApplications } =
            useApplicationsStore.getState();
          setLoading(true);
          setError(null);

          const response = await apiClient.getJobApplications(jobId);

          if (response.success && response.data) {
            setJobApplications(response.data, jobId);
          } else {
            setError(response.error || "Failed to fetch job applications");
          }
        } catch (error) {
          const standardError = createStandardError(
            error,
            "fetchJobApplications"
          );
          const { setError, setLoading } = useApplicationsStore.getState();
          setError(standardError.message);
          setLoading(false);
        } finally {
          const { setLoading } = useApplicationsStore.getState();
          setLoading(false);
        }
      };

      fetchData();
    }
  }, [jobId]);

  const refreshJobApplications = useCallback(() => {
    return fetchJobApplications(jobId);
  }, [fetchJobApplications, jobId]);

  return {
    jobApplications,
    isLoading,
    error,
    fetchJobApplications,
    refreshJobApplications,
  };
}

export function useApplyForJob() {
  const { addApplication, setLoading, setError } = useApplicationsStore();

  const applyForJob = useCallback(
    async (jobId: number) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.applyForJob(jobId);

        if (response.success && response.data) {
          addApplication(response.data);
          return { success: true, application: response.data };
        } else {
          setError(response.error || "Failed to apply for job");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const standardError = createStandardError(error, "applyForJob");
        setError(standardError.message);
        return { success: false, error: standardError.message };
      } finally {
        setLoading(false);
      }
    },
    [addApplication, setLoading, setError]
  );

  return { applyForJob };
}

export function useUpdateApplicationStatus() {
  const { updateApplicationStatus, setLoading, setError } =
    useApplicationsStore();

  const updateStatus = useCallback(
    async (
      applicationId: number,
      status: Parameters<typeof apiClient.updateApplicationStatus>[1]
    ) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.updateApplicationStatus(
          applicationId,
          status
        );

        if (response.success && response.data) {
          updateApplicationStatus(applicationId, status);
          return { success: true, application: response.data };
        } else {
          setError(response.error || "Failed to update application status");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error
            ? error.message
            : "Failed to update application status";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [updateApplicationStatus, setLoading, setError]
  );

  return { updateApplicationStatus: updateStatus };
}

export function useDeleteApplication() {
  const { removeApplication, setLoading, setError } = useApplicationsStore();

  const deleteApplication = useCallback(
    async (applicationId: number) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.deleteApplication(applicationId);

        if (response.success) {
          removeApplication(applicationId);
          return { success: true };
        } else {
          setError(response.error || "Failed to delete application");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error
            ? error.message
            : "Failed to delete application";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [removeApplication, setLoading, setError]
  );

  return { deleteApplication };
}
