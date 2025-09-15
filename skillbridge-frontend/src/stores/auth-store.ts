import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import { apiClient, User } from "@/lib/api";

interface AuthState {
  // State
  user: User | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;

  // Actions
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
  initializeAuth: () => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  devtools(
    persist(
      (set, get) => ({
        // Initial state
        user: null,
        token: null,
        isLoading: true,
        isAuthenticated: false,

        // Actions
        login: async (username: string, password: string) => {
          set({ isLoading: true });

          try {
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
              return {
                success: false,
                error: response.error || "Login failed",
              };
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
          set({ isLoading: true });

          try {
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

        initializeAuth: () => {
          const { token, user } = get();
          if (token && user) {
            set({ isAuthenticated: true, isLoading: false });
          } else {
            set({ isAuthenticated: false, isLoading: false });
          }
        },

        clearAuth: () => {
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
          });
        },
      }),
      {
        name: "auth-storage",
        partialize: (state) => ({
          user: state.user,
          token: state.token,
        }),
      }
    ),
    {
      name: "auth-store",
    }
  )
);

// Selectors for optimized re-renders
export const useUser = () => useAuthStore((state) => state.user);
export const useToken = () => useAuthStore((state) => state.token);
export const useIsAuthenticated = () =>
  useAuthStore((state) => state.isAuthenticated);
export const useIsLoading = () => useAuthStore((state) => state.isLoading);
