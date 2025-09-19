/**
 * Auth utilities and token management
 */

import { isValidToken } from "./jwt";

export class AuthManager {
  private static instance: AuthManager;
  private listeners: (() => void)[] = [];

  private constructor() {}

  static getInstance(): AuthManager {
    if (!AuthManager.instance) {
      AuthManager.instance = new AuthManager();
    }
    return AuthManager.instance;
  }

  // Add listener for auth state changes
  addListener(callback: () => void) {
    this.listeners.push(callback);
  }

  // Remove listener
  removeListener(callback: () => void) {
    this.listeners = this.listeners.filter((l) => l !== callback);
  }

  // Notify all listeners of auth state change
  private notify() {
    this.listeners.forEach((callback) => callback());
  }

  // Clear all auth data and notify listeners
  clearAuth() {
    if (typeof window !== "undefined") {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
    }
    this.notify();
  }

  // Get current token
  getToken(): string | null {
    if (typeof window === "undefined") {
      return null;
    }
    return localStorage.getItem("token");
  }

  // Check if user is authenticated with a valid token
  isAuthenticated(): boolean {
    const token = this.getToken();
    return token ? isValidToken(token) : false;
  }

  // Set new auth data
  setAuth(token: string, user: object) {
    if (typeof window !== "undefined") {
      localStorage.setItem("token", token);
      localStorage.setItem("user", JSON.stringify(user));
    }
    this.notify();
  }
}

export const authManager = AuthManager.getInstance();
