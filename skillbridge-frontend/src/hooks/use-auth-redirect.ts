"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { authManager } from "@/lib/auth-manager";
import { useAuth } from "./use-auth";

/**
 * Hook to handle automatic logout and redirect when auth state changes
 */
export function useAuthRedirect() {
  const router = useRouter();
  const { logout } = useAuth();

  useEffect(() => {
    const handleAuthChange = () => {
      // Check if user is no longer authenticated
      if (!authManager.isAuthenticated()) {
        // Clear auth state
        logout();

        // Redirect to login page
        const currentPath = window.location.pathname;
        if (
          currentPath !== "/login" &&
          currentPath !== "/register" &&
          currentPath !== "/"
        ) {
          router.push("/login");
        }
      }
    };

    // Listen for auth changes
    authManager.addListener(handleAuthChange);

    // Cleanup listener on unmount
    return () => {
      authManager.removeListener(handleAuthChange);
    };
  }, [router, logout]);
}
