// Role-based permissions and access control utilities
import { User } from "./api";

export type UserRole = "JOB_SEEKER" | "EMPLOYER" | "ADMIN";

export interface Permission {
  // Job-related permissions
  canViewJobs: boolean;
  canCreateJobs: boolean;
  canEditJobs: boolean;
  canDeleteJobs: boolean;

  // Application-related permissions
  canApplyToJobs: boolean;
  canViewApplications: boolean;
  canManageApplications: boolean;

  // Profile-related permissions
  canViewProfiles: boolean;
  canEditOwnProfile: boolean;
  canEditAnyProfile: boolean;

  // User management permissions
  canViewAllUsers: boolean;
  canCreateUsers: boolean;
  canEditUsers: boolean;
  canDeleteUsers: boolean;

  // System permissions
  canAccessAdminPanel: boolean;
}

export const ROLE_PERMISSIONS: Record<UserRole, Permission> = {
  JOB_SEEKER: {
    // Job-related permissions
    canViewJobs: true,
    canCreateJobs: false,
    canEditJobs: false,
    canDeleteJobs: false,

    // Application-related permissions
    canApplyToJobs: true,
    canViewApplications: true, // Own applications only
    canManageApplications: false,

    // Profile-related permissions
    canViewProfiles: true,
    canEditOwnProfile: true,
    canEditAnyProfile: false,

    // User management permissions
    canViewAllUsers: false,
    canCreateUsers: false,
    canEditUsers: false,
    canDeleteUsers: false,

    // System permissions
    canAccessAdminPanel: false,
  },

  EMPLOYER: {
    // Job-related permissions
    canViewJobs: true,
    canCreateJobs: true,
    canEditJobs: true, // Own jobs only
    canDeleteJobs: true, // Own jobs only

    // Application-related permissions
    canApplyToJobs: false,
    canViewApplications: true, // Applications to their jobs
    canManageApplications: true, // Can update status

    // Profile-related permissions
    canViewProfiles: true,
    canEditOwnProfile: true,
    canEditAnyProfile: false,

    // User management permissions
    canViewAllUsers: false,
    canCreateUsers: false,
    canEditUsers: false,
    canDeleteUsers: false,

    // System permissions
    canAccessAdminPanel: false,
  },

  ADMIN: {
    // Job-related permissions
    canViewJobs: true,
    canCreateJobs: true,
    canEditJobs: true,
    canDeleteJobs: true,

    // Application-related permissions
    canApplyToJobs: false, // Admins typically don't apply
    canViewApplications: true,
    canManageApplications: true,

    // Profile-related permissions
    canViewProfiles: true,
    canEditOwnProfile: true,
    canEditAnyProfile: true,

    // User management permissions
    canViewAllUsers: true,
    canCreateUsers: true,
    canEditUsers: true,
    canDeleteUsers: true,

    // System permissions
    canAccessAdminPanel: true,
  },
};

export function getPermissions(role: UserRole): Permission {
  return ROLE_PERMISSIONS[role];
}

export function hasPermission(
  user: User | null,
  permission: keyof Permission
): boolean {
  if (!user) return false;
  const permissions = getPermissions(user.role);
  return permissions[permission];
}

export function canAccessRoute(user: User | null, routePath: string): boolean {
  if (!user) {
    // Public routes that don't require authentication
    const publicRoutes = ["/", "/login", "/register", "/jobs"];
    return publicRoutes.some((route) => routePath.startsWith(route));
  }

  const permissions = getPermissions(user.role);

  // Route-based access control
  switch (true) {
    // Admin routes
    case routePath.startsWith("/admin"):
      return permissions.canAccessAdminPanel;

    // Dashboard routes (authenticated users only)
    case routePath.startsWith("/dashboard"):
      return true; // All authenticated users can access dashboard

    // Job management routes
    case routePath.startsWith("/jobs/create"):
    case routePath.startsWith("/jobs/new"):
      return permissions.canCreateJobs;

    case routePath.startsWith("/jobs/edit/"):
    case routePath.startsWith("/jobs/") && routePath.includes("/edit"):
      return permissions.canEditJobs;

    // Application routes
    case routePath.startsWith("/applications"):
      return permissions.canViewApplications;

    // Profile routes
    case routePath.startsWith("/profile"):
      return permissions.canViewProfiles;

    // Public job viewing routes
    case routePath.startsWith("/jobs"):
      return permissions.canViewJobs;

    default:
      return true; // Allow access to other routes for authenticated users
  }
}

export function getAccessibleRoutes(user: User | null): string[] {
  const allRoutes = [
    "/",
    "/jobs",
    "/jobs/create",
    "/applications",
    "/profile",
    "/dashboard",
    "/admin",
  ];

  return allRoutes.filter((route) => canAccessRoute(user, route));
}

export function shouldShowNavItem(user: User | null, navItem: string): boolean {
  if (!user) return false;

  const permissions = getPermissions(user.role);

  switch (navItem) {
    case "jobs":
      return permissions.canViewJobs;
    case "create-job":
      return permissions.canCreateJobs;
    case "applications":
      return permissions.canViewApplications;
    case "profile":
      return permissions.canViewProfiles;
    case "admin":
      return permissions.canAccessAdminPanel;
    case "dashboard":
      return true; // All authenticated users
    default:
      return true;
  }
}

// Helper functions for common checks
export const roleChecks = {
  isJobSeeker: (user: User | null): boolean => user?.role === "JOB_SEEKER",
  isEmployer: (user: User | null): boolean => user?.role === "EMPLOYER",
  isAdmin: (user: User | null): boolean => user?.role === "ADMIN",
  isAuthenticated: (user: User | null): boolean => user !== null,
  canManageJobs: (user: User | null): boolean =>
    hasPermission(user, "canCreateJobs"),
  canApply: (user: User | null): boolean =>
    hasPermission(user, "canApplyToJobs"),
};
