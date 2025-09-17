/**
 * JWT token utilities for validation and decoding
 */

export interface JWTPayload {
  sub: string; // subject (user id)
  exp: number; // expiration time
  iat: number; // issued at
  username?: string;
  role?: string;
  [key: string]: unknown;
}

/**
 * Decode JWT token without verification (client-side only)
 * Note: This is for checking expiration only, not for security validation
 */
export function decodeJWT(token: string): JWTPayload | null {
  try {
    if (!token || typeof token !== "string") {
      return null;
    }

    const parts = token.split(".");
    if (parts.length !== 3) {
      return null;
    }

    const payload = parts[1];
    const decoded = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(decoded) as JWTPayload;
  } catch (error) {
    console.error("Error decoding JWT:", error);
    return null;
  }
}

/**
 * Check if a JWT token is expired
 */
export function isTokenExpired(token: string): boolean {
  const payload = decodeJWT(token);
  if (!payload || !payload.exp) {
    return true;
  }

  // Convert to milliseconds and add a 30-second buffer
  const expirationTime = payload.exp * 1000;
  const currentTime = Date.now();
  const buffer = 30 * 1000; // 30 seconds

  return currentTime >= expirationTime - buffer;
}

/**
 * Get the expiration time of a JWT token
 */
export function getTokenExpiration(token: string): Date | null {
  const payload = decodeJWT(token);
  if (!payload || !payload.exp) {
    return null;
  }

  return new Date(payload.exp * 1000);
}

/**
 * Check if token is valid (exists and not expired)
 */
export function isValidToken(token: string | null): boolean {
  if (!token || typeof token !== "string" || !token.trim()) {
    return false;
  }

  return !isTokenExpired(token);
}
