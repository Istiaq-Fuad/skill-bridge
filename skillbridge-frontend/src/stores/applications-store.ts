"use client";

import { create } from "zustand";
import { JobApplication } from "@/lib/api";

interface ApplicationsState {
  applications: JobApplication[];
  userApplications: JobApplication[];
  jobApplications: JobApplication[];
  isLoading: boolean;
  error: string | null;
}

interface ApplicationsActions {
  setApplications: (applications: JobApplication[]) => void;
  setUserApplications: (applications: JobApplication[]) => void;
  setJobApplications: (applications: JobApplication[]) => void;
  addApplication: (application: JobApplication) => void;
  updateApplicationStatus: (
    applicationId: number,
    status: JobApplication["status"]
  ) => void;
  removeApplication: (applicationId: number) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearApplications: () => void;
}

type ApplicationsStore = ApplicationsState & ApplicationsActions;

export const useApplicationsStore = create<ApplicationsStore>((set, get) => ({
  // Initial state
  applications: [],
  userApplications: [],
  jobApplications: [],
  isLoading: false,
  error: null,

  // Actions
  setApplications: (applications: JobApplication[]) => {
    set({ applications, error: null });
  },

  setUserApplications: (applications: JobApplication[]) => {
    set({ userApplications: applications, error: null });
  },

  setJobApplications: (applications: JobApplication[]) => {
    set({ jobApplications: applications, error: null });
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
    });
  },
}));
