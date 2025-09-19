"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import { apiClient, User } from "@/lib/api";
import { clearAuthData, isValidJWTFormat } from "@/lib/auth-utils";

interface AuthState {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
}

interface AuthActions {
  login: (
    username: string,
    password: string
  ) => Promise<{ success: boolean; error?: string }>;
  register: (data: {
    username: string;
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
    role: "JOB_SEEKER" | "EMPLOYER";
  }) => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
  updateUser: (userData: Partial<User>) => void;
  setLoading: (loading: boolean) => void;
  hydrate: () => void;
}

type AuthStore = AuthState & AuthActions;

export const useAuthStore = create<AuthStore>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      token: null,
      isLoading: true,
      isAuthenticated: false,

      // Actions
      login: async (username: string, password: string) => {
        try {
          set({ isLoading: true });
          const response = await apiClient.login({ username, password });

          if (response.success && response.data) {
            const { token: newToken, user: userData } = response.data;
            set({
              token: newToken,
              user: userData,
              isAuthenticated: true,
              isLoading: false,
            });
            return { success: true };
          } else {
            set({ isLoading: false });
            return { success: false, error: response.error || "Login failed" };
          }
        } catch (error) {
          set({ isLoading: false });
          return {
            success: false,
            error: error instanceof Error ? error.message : "Login failed",
          };
        }
      },

      register: async (data: {
        username: string;
        email: string;
        password: string;
        firstName?: string;
        lastName?: string;
        role: "JOB_SEEKER" | "EMPLOYER";
      }) => {
        try {
          set({ isLoading: true });
          const response = await apiClient.register(data);

          if (response.success && response.data) {
            const { token: newToken, user: userData } = response.data;
            set({
              token: newToken,
              user: userData,
              isAuthenticated: true,
              isLoading: false,
            });
            return { success: true };
          } else {
            set({ isLoading: false });
            return {
              success: false,
              error: response.error || "Registration failed",
            };
          }
        } catch (error) {
          set({ isLoading: false });
          return {
            success: false,
            error:
              error instanceof Error ? error.message : "Registration failed",
          };
        }
      },

      logout: () => {
        // Clear all auth data from localStorage
        clearAuthData();

        set({
          user: null,
          token: null,
          isAuthenticated: false,
          isLoading: false,
        });
      },

      updateUser: (userData: Partial<User>) => {
        const { user } = get();
        if (user) {
          const updatedUser = { ...user, ...userData };
          set({ user: updatedUser });
        }
      },

      setLoading: (loading: boolean) => {
        set({ isLoading: loading });
      },

      hydrate: () => {
        const { token, user } = get();

        // Validate token format and expiration before using it
        if (token && user && isValidJWTFormat(token)) {
          try {
            // Check if token is expired
            const parts = token.split(".");
            const payload = JSON.parse(
              atob(parts[1].replace(/-/g, "+").replace(/_/g, "/"))
            );

            if (payload.exp && payload.exp * 1000 > Date.now()) {
              set({ isAuthenticated: true, isLoading: false });
            } else {
              // Token expired, clear auth data
              clearAuthData();
              set({
                user: null,
                token: null,
                isAuthenticated: false,
                isLoading: false,
              });
            }
          } catch {
            // Invalid token, clear auth data
            clearAuthData();
            set({
              user: null,
              token: null,
              isAuthenticated: false,
              isLoading: false,
            });
          }
        } else {
          set({ isAuthenticated: false, isLoading: false });
        }
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        user: state.user,
        token: state.token,
      }),
      onRehydrateStorage: () => (state) => {
        if (state) {
          state.hydrate();
        }
      },
    }
  )
);
