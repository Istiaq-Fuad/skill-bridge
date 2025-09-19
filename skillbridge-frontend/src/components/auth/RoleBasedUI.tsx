"use client";

import { useAuth } from "@/hooks";
import { hasPermission, roleChecks } from "@/lib/permissions";
import { User } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { AlertTriangle, Lock } from "lucide-react";
import { Alert, AlertDescription } from "@/components/ui/alert";

// Generic component for role-based rendering
interface RoleBasedRenderProps {
  user?: User | null;
  allowedRoles?: Array<"JOB_SEEKER" | "EMPLOYER" | "ADMIN">;
  requiredPermission?: keyof import("@/lib/permissions").Permission;
  children: React.ReactNode;
  fallback?: React.ReactNode;
  requireAuth?: boolean;
}

export function RoleBasedRender({
  user: propUser,
  allowedRoles,
  requiredPermission,
  children,
  fallback = null,
  requireAuth = false,
}: RoleBasedRenderProps) {
  const { user: contextUser } = useAuth();
  const user = propUser ?? contextUser;

  // Check authentication requirement
  if (requireAuth && !user) {
    return <>{fallback}</>;
  }

  // Check role-based access
  if (allowedRoles && (!user || !allowedRoles.includes(user.role))) {
    return <>{fallback}</>;
  }

  // Check permission-based access
  if (requiredPermission && !hasPermission(user, requiredPermission)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}

// Specific components for common role checks
export const JobSeekerOnly: React.FC<{
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ children, fallback = null }) => (
  <RoleBasedRender allowedRoles={["JOB_SEEKER"]} fallback={fallback}>
    {children}
  </RoleBasedRender>
);

export const EmployerOnly: React.FC<{
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ children, fallback = null }) => (
  <RoleBasedRender allowedRoles={["EMPLOYER", "ADMIN"]} fallback={fallback}>
    {children}
  </RoleBasedRender>
);

export const AdminOnly: React.FC<{
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ children, fallback = null }) => (
  <RoleBasedRender allowedRoles={["ADMIN"]} fallback={fallback}>
    {children}
  </RoleBasedRender>
);

export const AuthenticatedOnly: React.FC<{
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ children, fallback = null }) => (
  <RoleBasedRender requireAuth={true} fallback={fallback}>
    {children}
  </RoleBasedRender>
);

// Permission-based components
export const CanCreateJobs: React.FC<{
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ children, fallback = null }) => (
  <RoleBasedRender requiredPermission="canCreateJobs" fallback={fallback}>
    {children}
  </RoleBasedRender>
);

export const CanApplyToJobs: React.FC<{
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ children, fallback = null }) => (
  <RoleBasedRender requiredPermission="canApplyToJobs" fallback={fallback}>
    {children}
  </RoleBasedRender>
);

export const CanManageApplications: React.FC<{
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ children, fallback = null }) => (
  <RoleBasedRender
    requiredPermission="canManageApplications"
    fallback={fallback}
  >
    {children}
  </RoleBasedRender>
);

// Access denied message component
interface AccessDeniedProps {
  message?: string;
  showLoginButton?: boolean;
  requiredRole?: string;
}

export function AccessDenied({
  message = "You don't have permission to access this content.",
  showLoginButton = false,
  requiredRole,
}: AccessDeniedProps) {
  const { user } = useAuth();

  return (
    <div className="flex flex-col items-center justify-center p-8 text-center">
      <div className="rounded-full bg-red-100 p-3 mb-4">
        <Lock className="h-6 w-6 text-red-600" />
      </div>
      <h3 className="text-lg font-semibold mb-2">Access Denied</h3>
      <p className="text-gray-600 mb-4 max-w-md">
        {message}
        {requiredRole && ` This feature requires ${requiredRole} access.`}
      </p>
      {showLoginButton && !user && (
        <Button asChild>
          <a href="/login">Login to Continue</a>
        </Button>
      )}
    </div>
  );
}

// Role indicator badge
export function RoleBadge({ user: propUser }: { user?: User | null }) {
  const { user: contextUser } = useAuth();
  const user = propUser ?? contextUser;

  if (!user) return null;

  const getRoleColor = (role: string) => {
    switch (role) {
      case "ADMIN":
        return "bg-red-100 text-red-800 border-red-200";
      case "EMPLOYER":
        return "bg-blue-100 text-blue-800 border-blue-200";
      case "JOB_SEEKER":
        return "bg-green-100 text-green-800 border-green-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  const getRoleLabel = (role: string) => {
    switch (role) {
      case "JOB_SEEKER":
        return "Job Seeker";
      case "EMPLOYER":
        return "Employer";
      case "ADMIN":
        return "Administrator";
      default:
        return role;
    }
  };

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getRoleColor(
        user.role
      )}`}
    >
      {getRoleLabel(user.role)}
    </span>
  );
}

// Feature availability notice
interface FeatureNoticeProps {
  requiredRole: "JOB_SEEKER" | "EMPLOYER" | "ADMIN";
  featureName: string;
  description?: string;
}

export function FeatureNotice({
  requiredRole,
  featureName,
  description,
}: FeatureNoticeProps) {
  const { user } = useAuth();

  if (!user || user.role === requiredRole) return null;

  const getRoleLabel = (role: string) => {
    switch (role) {
      case "JOB_SEEKER":
        return "Job Seeker";
      case "EMPLOYER":
        return "Employer";
      case "ADMIN":
        return "Administrator";
      default:
        return role;
    }
  };

  return (
    <Alert className="border-amber-200 bg-amber-50">
      <AlertTriangle className="h-4 w-4 text-amber-600" />
      <AlertDescription className="text-amber-800">
        <strong>{featureName}</strong> is only available for{" "}
        {getRoleLabel(requiredRole)} accounts.
        {description && (
          <span className="block mt-1 text-sm">{description}</span>
        )}
      </AlertDescription>
    </Alert>
  );
}

// Hook for role-based UI logic
export function useRoleBasedUI() {
  const { user } = useAuth();

  return {
    user,
    isJobSeeker: roleChecks.isJobSeeker(user),
    isEmployer: roleChecks.isEmployer(user),
    isAdmin: roleChecks.isAdmin(user),
    isAuthenticated: roleChecks.isAuthenticated(user),
    canCreateJobs: hasPermission(user, "canCreateJobs"),
    canApplyToJobs: hasPermission(user, "canApplyToJobs"),
    canManageApplications: hasPermission(user, "canManageApplications"),
    canViewProfiles: hasPermission(user, "canViewProfiles"),
    canAccessAdminPanel: hasPermission(user, "canAccessAdminPanel"),
  };
}
