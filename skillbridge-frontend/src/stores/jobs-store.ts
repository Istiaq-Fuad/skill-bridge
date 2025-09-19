"use client";

import { create } from "zustand";
import { Job } from "@/lib/api";

interface JobsState {
  jobs: Job[];
  currentJob: Job | null;
  searchFilters: {
    search?: string;
    location?: string;
    company?: string;
  };
  isLoading: boolean;
  error: string | null;
}

interface JobsActions {
  setJobs: (jobs: Job[]) => void;
  setCurrentJob: (job: Job | null) => void;
  addJob: (job: Job) => void;
  updateJob: (id: number, job: Partial<Job>) => void;
  removeJob: (id: number) => void;
  setSearchFilters: (filters: JobsState["searchFilters"]) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearJobs: () => void;
}

type JobsStore = JobsState & JobsActions;

export const useJobsStore = create<JobsStore>((set, get) => ({
  // Initial state
  jobs: [],
  currentJob: null,
  searchFilters: {},
  isLoading: false,
  error: null,

  // Actions
  setJobs: (jobs: Job[]) => {
    set({ jobs, error: null });
  },

  setCurrentJob: (job: Job | null) => {
    set({ currentJob: job });
  },

  addJob: (job: Job) => {
    const { jobs } = get();
    set({ jobs: [job, ...jobs] });
  },

  updateJob: (id: number, jobUpdate: Partial<Job>) => {
    const { jobs, currentJob } = get();
    const updatedJobs = jobs.map((job) =>
      job.id === id ? { ...job, ...jobUpdate } : job
    );

    set({
      jobs: updatedJobs,
      currentJob:
        currentJob?.id === id ? { ...currentJob, ...jobUpdate } : currentJob,
    });
  },

  removeJob: (id: number) => {
    const { jobs, currentJob } = get();
    const filteredJobs = jobs.filter((job) => job.id !== id);

    set({
      jobs: filteredJobs,
      currentJob: currentJob?.id === id ? null : currentJob,
    });
  },

  setSearchFilters: (filters: JobsState["searchFilters"]) => {
    set({ searchFilters: filters });
  },

  setLoading: (loading: boolean) => {
    set({ isLoading: loading });
  },

  setError: (error: string | null) => {
    set({ error });
  },

  clearJobs: () => {
    set({ jobs: [], currentJob: null, searchFilters: {}, error: null });
  },
}));
