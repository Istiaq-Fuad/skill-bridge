"use client";

import { create } from "zustand";
import { JobApplication } from "@/lib/api";

interface ApplicationsState {
  applications: JobApplication[];
  userApplications: JobApplication[];
  jobApplications: JobApplication[];
  isLoading: boolean;
  error: string | null;
  loadedUserId: number | null; // Track which user's data has been loaded
  loadedJobId: number | null; // Track which job's data has been loaded
  lastUserFetchTime: number | null; // Track when user data was last fetched
  lastJobFetchTime: number | null; // Track when job data was last fetched
}

interface ApplicationsActions {
  setApplications: (applications: JobApplication[]) => void;
  setUserApplications: (applications: JobApplication[], userId: number) => void;
  setJobApplications: (applications: JobApplication[], jobId: number) => void;
  addApplication: (application: JobApplication) => void;
  updateApplicationStatus: (
    applicationId: number,
    status: JobApplication["status"]
  ) => void;
  removeApplication: (applicationId: number) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearApplications: () => void;
  isUserDataFresh: (userId: number) => boolean;
  isJobDataFresh: (jobId: number) => boolean;
}

type ApplicationsStore = ApplicationsState & ApplicationsActions;

export const useApplicationsStore = create<ApplicationsStore>((set, get) => ({
  // Initial state
  applications: [],
  userApplications: [],
  jobApplications: [],
  isLoading: false,
  error: null,
  loadedUserId: null,
  loadedJobId: null,
  lastUserFetchTime: null,
  lastJobFetchTime: null,

  // Actions
  setApplications: (applications: JobApplication[]) => {
    set({ applications, error: null });
  },

  setUserApplications: (applications: JobApplication[], userId: number) => {
    set({
      userApplications: applications,
      error: null,
      loadedUserId: userId,
      lastUserFetchTime: Date.now(),
    });
  },

  setJobApplications: (applications: JobApplication[], jobId: number) => {
    set({
      jobApplications: applications,
      error: null,
      loadedJobId: jobId,
      lastJobFetchTime: Date.now(),
    });
  },

  addApplication: (application: JobApplication) => {
    const { applications, userApplications } = get();

    set({
      applications: [application, ...applications],
      userApplications: [application, ...userApplications],
    });
  },

  updateApplicationStatus: (
    applicationId: number,
    status: JobApplication["status"]
  ) => {
    const { applications, userApplications, jobApplications } = get();

    const updateApplicationInArray = (apps: JobApplication[]) =>
      apps.map((app) => (app.id === applicationId ? { ...app, status } : app));

    set({
      applications: updateApplicationInArray(applications),
      userApplications: updateApplicationInArray(userApplications),
      jobApplications: updateApplicationInArray(jobApplications),
    });
  },

  removeApplication: (applicationId: number) => {
    const { applications, userApplications, jobApplications } = get();

    const filterApplications = (apps: JobApplication[]) =>
      apps.filter((app) => app.id !== applicationId);

    set({
      applications: filterApplications(applications),
      userApplications: filterApplications(userApplications),
      jobApplications: filterApplications(jobApplications),
    });
  },

  setLoading: (loading: boolean) => {
    set({ isLoading: loading });
  },

  setError: (error: string | null) => {
    set({ error });
  },

  clearApplications: () => {
    set({
      applications: [],
      userApplications: [],
      jobApplications: [],
      error: null,
      loadedUserId: null,
      loadedJobId: null,
      lastUserFetchTime: null,
      lastJobFetchTime: null,
    });
  },

  isUserDataFresh: (userId: number) => {
    const { loadedUserId, lastUserFetchTime } = get();
    const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

    return (
      loadedUserId === userId &&
      lastUserFetchTime !== null &&
      Date.now() - lastUserFetchTime < CACHE_DURATION
    );
  },

  isJobDataFresh: (jobId: number) => {
    const { loadedJobId, lastJobFetchTime } = get();
    const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

    return (
      loadedJobId === jobId &&
      lastJobFetchTime !== null &&
      Date.now() - lastJobFetchTime < CACHE_DURATION
    );
  },
}));
