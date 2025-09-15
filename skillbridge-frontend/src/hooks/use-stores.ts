import { useEffect } from "react";
import { useAuthStore } from "@/stores";

/**
 * Hook to initialize Zustand stores
 * Use this in your app layout or main component
 */
export const useInitializeStores = () => {
  const initializeAuth = useAuthStore((state) => state.initializeAuth);

  useEffect(() => {
    // Initialize authentication state from persisted storage
    initializeAuth();
  }, [initializeAuth]);
};

/**
 * Hook to handle store cleanup on logout
 */
export const useStoreCleanup = () => {
  const clearAuth = useAuthStore((state) => state.clearAuth);

  return {
    clearAllStores: () => {
      clearAuth();
      // Add other store cleanup methods here as needed
    },
  };
};
