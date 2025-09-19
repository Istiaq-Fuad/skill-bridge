"use client";

import { create } from "zustand";
import { Profile, Skill, Education, Experience, Portfolio } from "@/lib/api";

interface ProfileState {
  profile: Profile | null;
  isLoading: boolean;
  error: string | null;
}

interface ProfileActions {
  setProfile: (profile: Profile | null) => void;
  updateProfile: (profileData: Partial<Profile>) => void;
  addSkill: (skill: Skill) => void;
  removeSkill: (skillId: number) => void;
  addEducation: (education: Education) => void;
  updateEducation: (eduId: number, education: Partial<Education>) => void;
  removeEducation: (eduId: number) => void;
  addExperience: (experience: Experience) => void;
  updateExperience: (expId: number, experience: Partial<Experience>) => void;
  removeExperience: (expId: number) => void;
  addPortfolio: (portfolio: Portfolio) => void;
  removePortfolio: (portfolioId: number) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearProfile: () => void;
}

type ProfileStore = ProfileState & ProfileActions;

export const useProfileStore = create<ProfileStore>((set, get) => ({
  // Initial state
  profile: null,
  isLoading: false,
  error: null,

  // Actions
  setProfile: (profile: Profile | null) => {
    set({ profile, error: null });
  },

  updateProfile: (profileData: Partial<Profile>) => {
    const { profile } = get();
    if (profile) {
      set({ profile: { ...profile, ...profileData } });
    }
  },

  addSkill: (skill: Skill) => {
    const { profile } = get();
    if (profile) {
      set({
        profile: {
          ...profile,
          skills: [...profile.skills, skill],
        },
      });
    }
  },

  removeSkill: (skillId: number) => {
    const { profile } = get();
    if (profile) {
      set({
        profile: {
          ...profile,
          skills: profile.skills.filter((skill) => skill.id !== skillId),
        },
      });
    }
  },

  addEducation: (education: Education) => {
    const { profile } = get();
    if (profile) {
      set({
        profile: {
          ...profile,
          education: [...profile.education, education],
        },
      });
    }
  },

  updateEducation: (eduId: number, educationUpdate: Partial<Education>) => {
    const { profile } = get();
    if (profile) {
      set({
        profile: {
          ...profile,
          education: profile.education.map((edu) =>
            edu.id === eduId ? { ...edu, ...educationUpdate } : edu
          ),
        },
      });
    }
  },

  removeEducation: (eduId: number) => {
    const { profile } = get();
    if (profile) {
      set({
        profile: {
          ...profile,
          education: profile.education.filter((edu) => edu.id !== eduId),
        },
      });
    }
  },

  addExperience: (experience: Experience) => {
    const { profile } = get();
    if (profile) {
      set({
        profile: {
          ...profile,
          experience: [...profile.experience, experience],
        },
      });
    }
  },

  updateExperience: (expId: number, experienceUpdate: Partial<Experience>) => {
    const { profile } = get();
    if (profile) {
      set({
        profile: {
          ...profile,
          experience: profile.experience.map((exp) =>
            exp.id === expId ? { ...exp, ...experienceUpdate } : exp
          ),
        },
      });
    }
  },

  removeExperience: (expId: number) => {
    const { profile } = get();
    if (profile) {
      set({
        profile: {
          ...profile,
          experience: profile.experience.filter((exp) => exp.id !== expId),
        },
      });
    }
  },

  addPortfolio: (portfolio: Portfolio) => {
    const { profile } = get();
    if (profile) {
      set({
        profile: {
          ...profile,
          portfolio: [...profile.portfolio, portfolio],
        },
      });
    }
  },

  removePortfolio: (portfolioId: number) => {
    const { profile } = get();
    if (profile) {
      set({
        profile: {
          ...profile,
          portfolio: profile.portfolio.filter(
            (item) => item.id !== portfolioId
          ),
        },
      });
    }
  },

  setLoading: (loading: boolean) => {
    set({ isLoading: loading });
  },

  setError: (error: string | null) => {
    set({ error });
  },

  clearProfile: () => {
    set({ profile: null, error: null });
  },
}));
