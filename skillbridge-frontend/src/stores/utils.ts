import { useAuthStore } from "./auth-store";

/**
 * Initialize the authentication store with persisted data
 * Call this in your app's entry point or layout
 */
export const initializeStores = () => {
  // Initialize auth store - this will check persisted data
  useAuthStore.getState().initializeAuth();
};

/**
 * Clear all stores (useful for logout or reset)
 */
export const clearAllStores = () => {
  useAuthStore.getState().clearAuth();
  // Add other store clear methods as needed
};

/**
 * Utility to get auth token for API calls
 */
export const getAuthToken = () => {
  return useAuthStore.getState().token;
};

/**
 * Utility to check if user is authenticated
 */
export const isAuthenticated = () => {
  return useAuthStore.getState().isAuthenticated;
};

/**
 * Utility to get current user
 */
export const getCurrentUser = () => {
  return useAuthStore.getState().user;
};
