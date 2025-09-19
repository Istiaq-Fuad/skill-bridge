/**
 * Centralized error handling utilities for consistent error management
 */

export interface ErrorInfo {
  message: string;
  code?: string;
  statusCode?: number;
  details?: unknown;
}

export class ApiError extends Error {
  public code?: string;
  public statusCode?: number;
  public details?: unknown;

  constructor(
    message: string,
    code?: string,
    statusCode?: number,
    details?: unknown
  ) {
    super(message);
    this.name = "ApiError";
    this.code = code;
    this.statusCode = statusCode;
    this.details = details;
  }
}

/**
 * Handle API response errors consistently
 */
export function handleApiError(error: unknown): ErrorInfo {
  // Handle ApiError instances
  if (error instanceof ApiError) {
    return {
      message: error.message,
      code: error.code,
      statusCode: error.statusCode,
      details: error.details,
    };
  }

  // Handle generic Error instances
  if (error instanceof Error) {
    return {
      message: error.message,
      details: error,
    };
  }

  // Handle string errors
  if (typeof error === "string") {
    return {
      message: error,
    };
  }

  // Handle unknown error types
  return {
    message: "An unexpected error occurred",
    details: error,
  };
}

/**
 * Format API error messages for user display
 */
export function formatErrorMessage(error: ErrorInfo): string {
  // Handle authentication errors
  if (error.statusCode === 401) {
    return "Authentication required. Please log in again.";
  }

  // Handle authorization errors
  if (error.statusCode === 403) {
    return "You do not have permission to perform this action.";
  }

  // Handle not found errors
  if (error.statusCode === 404) {
    return "The requested resource was not found.";
  }

  // Handle server errors
  if (error.statusCode && error.statusCode >= 500) {
    return "Server error. Please try again later.";
  }

  // Handle network errors
  if (error.message.toLowerCase().includes("network")) {
    return "Network error. Please check your connection and try again.";
  }

  // Return the original message for other cases
  return error.message || "An error occurred";
}

/**
 * Log errors consistently across the application
 */
export function logError(error: ErrorInfo, context?: string) {
  const logMessage = context ? `[${context}] ${error.message}` : error.message;

  if (error.statusCode && error.statusCode >= 500) {
    console.error(logMessage, error.details);
  } else if (error.statusCode === 404) {
    console.warn(logMessage);
  } else {
    console.error(logMessage);
  }
}

/**
 * Create a standardized error for hooks and components
 */
export function createStandardError(
  error: unknown,
  context?: string
): { message: string; details: ErrorInfo } {
  const errorInfo = handleApiError(error);
  const userMessage = formatErrorMessage(errorInfo);

  if (context) {
    logError(errorInfo, context);
  }

  return {
    message: userMessage,
    details: errorInfo,
  };
}

/**
 * Retry utility for failed API calls
 */
export async function withRetry<T>(
  operation: () => Promise<T>,
  maxRetries: number = 3,
  delay: number = 1000
): Promise<T> {
  let lastError: unknown;

  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await operation();
    } catch (error) {
      lastError = error;

      // Don't retry on client errors (4xx) except 429 (rate limit)
      if (error instanceof ApiError && error.statusCode) {
        if (
          error.statusCode >= 400 &&
          error.statusCode < 500 &&
          error.statusCode !== 429
        ) {
          throw error;
        }
      }

      // Don't retry on the last attempt
      if (attempt === maxRetries) {
        break;
      }

      // Wait before retrying with exponential backoff
      await new Promise((resolve) =>
        setTimeout(resolve, delay * Math.pow(2, attempt - 1))
      );
    }
  }

  throw lastError;
}
