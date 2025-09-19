/**
 * Authentication utilities
 */

/**
 * Clear all authentication data from localStorage
 * This is useful when JWT signature errors occur
 */
export function clearAuthData(): void {
  if (typeof window !== "undefined") {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    localStorage.removeItem("auth-storage");
    console.log("Authentication data cleared from localStorage");
  }
}

/**
 * Check if there are any stored tokens that might be invalid
 */
export function hasStoredAuthData(): boolean {
  if (typeof window === "undefined") return false;

  return !!(
    localStorage.getItem("token") ||
    localStorage.getItem("user") ||
    localStorage.getItem("auth-storage")
  );
}

/**
 * Validate JWT token format (basic check, not signature validation)
 */
export function isValidJWTFormat(token: string): boolean {
  if (!token || typeof token !== "string") return false;

  const parts = token.split(".");
  if (parts.length !== 3) return false;

  try {
    // Try to decode the payload to check basic format
    const payload = JSON.parse(
      atob(parts[1].replace(/-/g, "+").replace(/_/g, "/"))
    );
    return !!(payload.sub || payload.username);
  } catch {
    return false;
  }
}
