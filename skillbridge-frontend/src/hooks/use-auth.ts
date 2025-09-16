"use client";

import { useCallback } from "react";
import { useAuthStore } from "@/stores";

// Compatibility hook to match the original AuthContext interface
// This ensures no breaking changes during migration
export function useAuth() {
  // Use individual selectors to avoid creating new objects
  const user = useAuthStore((state) => state.user);
  const token = useAuthStore((state) => state.token);
  const isLoading = useAuthStore((state) => state.isLoading);
  const login = useAuthStore((state) => state.login);
  const register = useAuthStore((state) => state.register);
  const logout = useAuthStore((state) => state.logout);
  const updateUser = useAuthStore((state) => state.updateUser);

  // Memoize the return object to prevent infinite re-renders
  return useCallback(
    () => ({
      user,
      token,
      isLoading,
      login,
      register,
      logout,
      updateUser,
    }),
    [user, token, isLoading, login, register, logout, updateUser]
  )();
}
