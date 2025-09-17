/**
 * Authentication-aware API hooks
 * These hooks ensure API calls only happen when authentication is ready
 */

"use client";

import { useEffect, useState } from "react";
import { useAuth } from "./use-auth";

/**
 * Hook that provides authentication readiness status
 * Returns true only when auth is loaded and either user is authenticated or it's a public endpoint
 */
export function useAuthReady(requiresAuth: boolean = true) {
  const { user, isLoading } = useAuth();
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    if (!isLoading) {
      if (requiresAuth) {
        // For protected endpoints, wait for user to be available
        setIsReady(!!user);
      } else {
        // For public endpoints, ready when loading is done
        setIsReady(true);
      }
    } else {
      setIsReady(false);
    }
  }, [isLoading, user, requiresAuth]);

  return {
    isReady,
    user,
    isLoading,
  };
}

/**
 * Hook that ensures localStorage token is synchronized
 * Useful for preventing race conditions between Zustand hydration and API calls
 */
export function useTokenReady() {
  const [tokenReady, setTokenReady] = useState(false);
  const { token, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading) {
      // Check if localStorage token matches the store token
      if (typeof window !== "undefined") {
        const localToken = localStorage.getItem("token");
        if (token === localToken) {
          setTokenReady(true);
        } else if (token && localToken !== token) {
          // Sync localStorage with store if they don't match
          localStorage.setItem("token", token);
          setTokenReady(true);
        } else if (!token && !localToken) {
          // Both are null/undefined, which is consistent
          setTokenReady(true);
        }
      } else {
        // Server-side, just use the loading state
        setTokenReady(true);
      }
    }
  }, [token, isLoading]);

  return tokenReady;
}

/**
 * Combined hook for API readiness
 * Ensures both auth and token are ready before allowing API calls
 */
export function useApiReady(requiresAuth: boolean = true) {
  const { isReady: authReady, user, isLoading } = useAuthReady(requiresAuth);
  const tokenReady = useTokenReady();

  return {
    isReady: authReady && tokenReady,
    user,
    isLoading,
    authReady,
    tokenReady,
  };
}
