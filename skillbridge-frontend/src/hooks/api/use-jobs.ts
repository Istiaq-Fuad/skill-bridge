"use client";

import { useCallback, useEffect } from "react";
import { apiClient } from "@/lib/api";
import { useJobsStore } from "@/stores";
import { createStandardError } from "@/lib/error-handler";

export function useJobs() {
  const {
    jobs,
    searchFilters,
    isLoading,
    error,
    setJobs,
    setLoading,
    setError,
  } = useJobsStore();

  const fetchJobs = useCallback(
    async (filters?: {
      search?: string;
      location?: string;
      company?: string;
    }) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.getJobs(filters);

        if (response.success && response.data) {
          setJobs(response.data);
        } else {
          setError(response.error || "Failed to fetch jobs");
        }
      } catch (error) {
        const standardError = createStandardError(error, "fetchJobs");
        setError(standardError.message);
      } finally {
        setLoading(false);
      }
    },
    [setJobs, setLoading, setError]
  );

  const refreshJobs = useCallback(() => {
    return fetchJobs(searchFilters);
  }, [fetchJobs, searchFilters]);

  return {
    jobs,
    searchFilters,
    isLoading,
    error,
    fetchJobs,
    refreshJobs,
  };
}

export function useJob(id: number) {
  const { currentJob, setCurrentJob, setLoading, setError } = useJobsStore();

  const fetchJob = useCallback(
    async (jobId: number) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.getJob(jobId);

        if (response.success && response.data) {
          setCurrentJob(response.data);
          return response.data;
        } else {
          setError(response.error || "Failed to fetch job");
          return null;
        }
      } catch (error) {
        setError(
          error instanceof Error ? error.message : "Failed to fetch job"
        );
        return null;
      } finally {
        setLoading(false);
      }
    },
    [setCurrentJob, setLoading, setError]
  );

  useEffect(() => {
    if (id && (!currentJob || currentJob.id !== id)) {
      const fetchData = async () => {
        try {
          setLoading(true);
          setError(null);

          const response = await apiClient.getJob(id);

          if (response.success && response.data) {
            setCurrentJob(response.data);
          } else {
            setError(response.error || "Failed to fetch job");
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Failed to fetch job";
          setError(errorMessage);
        } finally {
          setLoading(false);
        }
      };

      fetchData();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, currentJob]);

  return {
    job: currentJob,
    fetchJob,
  };
}

export function useCreateJob() {
  const { addJob, setLoading, setError } = useJobsStore();

  const createJob = useCallback(
    async (jobData: Parameters<typeof apiClient.createJob>[0]) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.createJob(jobData);

        if (response.success && response.data) {
          addJob(response.data);
          return { success: true, job: response.data };
        } else {
          setError(response.error || "Failed to create job");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to create job";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [addJob, setLoading, setError]
  );

  return { createJob };
}

export function useUpdateJob() {
  const { updateJob, setLoading, setError } = useJobsStore();

  const updateJobData = useCallback(
    async (id: number, jobData: Parameters<typeof apiClient.updateJob>[1]) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.updateJob(id, jobData);

        if (response.success && response.data) {
          updateJob(id, response.data);
          return { success: true, job: response.data };
        } else {
          setError(response.error || "Failed to update job");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to update job";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [updateJob, setLoading, setError]
  );

  return { updateJob: updateJobData };
}

export function useDeleteJob() {
  const { removeJob, setLoading, setError } = useJobsStore();

  const deleteJob = useCallback(
    async (id: number) => {
      try {
        setLoading(true);
        setError(null);

        const response = await apiClient.deleteJob(id);

        if (response.success) {
          removeJob(id);
          return { success: true };
        } else {
          setError(response.error || "Failed to delete job");
          return { success: false, error: response.error };
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to delete job";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [removeJob, setLoading, setError]
  );

  return { deleteJob };
}
