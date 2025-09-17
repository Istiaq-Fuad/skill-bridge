"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuth } from "@/hooks";
import { canAccessRoute, hasPermission } from "@/lib/permissions";
import { User } from "@/lib/api";

interface RouteGuardProps {
  children: React.ReactNode;
  requiredPermission?: keyof import("@/lib/permissions").Permission;
  fallbackRoute?: string;
  requireAuth?: boolean;
  allowedRoles?: Array<"JOB_SEEKER" | "EMPLOYER" | "ADMIN">;
}

export function RouteGuard({
  children,
  requiredPermission,
  fallbackRoute = "/login",
  requireAuth = true,
  allowedRoles,
}: RouteGuardProps) {
  const { user, isLoading } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (isLoading) return;

    // Check if authentication is required
    if (requireAuth && !user) {
      router.push(`${fallbackRoute}?redirect=${encodeURIComponent(pathname)}`);
      return;
    }

    // Check role-based access
    if (allowedRoles && user && !allowedRoles.includes(user.role)) {
      router.push("/dashboard");
      return;
    }

    // Check permission-based access
    if (requiredPermission && !hasPermission(user, requiredPermission)) {
      router.push("/dashboard");
      return;
    }

    // Check route-based access
    if (!canAccessRoute(user, pathname)) {
      if (!user) {
        router.push(
          `${fallbackRoute}?redirect=${encodeURIComponent(pathname)}`
        );
      } else {
        router.push("/dashboard");
      }
      return;
    }
  }, [
    user,
    isLoading,
    pathname,
    router,
    requiredPermission,
    requireAuth,
    allowedRoles,
    fallbackRoute,
  ]);

  // Show loading state while checking authentication
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary"></div>
      </div>
    );
  }

  // Don't render children if access is denied
  if (requireAuth && !user) return null;
  if (allowedRoles && user && !allowedRoles.includes(user.role)) return null;
  if (requiredPermission && !hasPermission(user, requiredPermission))
    return null;
  if (!canAccessRoute(user, pathname)) return null;

  return <>{children}</>;
}

// Higher-order component for page-level protection
export function withAuth<P extends object>(
  Component: React.ComponentType<P>,
  options?: {
    requiredPermission?: keyof import("@/lib/permissions").Permission;
    allowedRoles?: Array<"JOB_SEEKER" | "EMPLOYER" | "ADMIN">;
    fallbackRoute?: string;
  }
) {
  const AuthenticatedComponent = (props: P) => {
    return (
      <RouteGuard
        requiredPermission={options?.requiredPermission}
        allowedRoles={options?.allowedRoles}
        fallbackRoute={options?.fallbackRoute}
      >
        <Component {...props} />
      </RouteGuard>
    );
  };

  AuthenticatedComponent.displayName = `withAuth(${
    Component.displayName || Component.name
  })`;
  return AuthenticatedComponent;
}

// Specific route guards for common use cases
export const AdminGuard: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <RouteGuard allowedRoles={["ADMIN"]} fallbackRoute="/dashboard">
    {children}
  </RouteGuard>
);

export const EmployerGuard: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <RouteGuard allowedRoles={["EMPLOYER", "ADMIN"]} fallbackRoute="/dashboard">
    {children}
  </RouteGuard>
);

export const JobSeekerGuard: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <RouteGuard allowedRoles={["JOB_SEEKER", "ADMIN"]} fallbackRoute="/dashboard">
    {children}
  </RouteGuard>
);

// Component to conditionally render based on permissions
interface ConditionalRenderProps {
  user: User | null;
  permission?: keyof import("@/lib/permissions").Permission;
  roles?: Array<"JOB_SEEKER" | "EMPLOYER" | "ADMIN">;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export function ConditionalRender({
  user,
  permission,
  roles,
  children,
  fallback = null,
}: ConditionalRenderProps) {
  // Check role-based access
  if (roles && (!user || !roles.includes(user.role))) {
    return <>{fallback}</>;
  }

  // Check permission-based access
  if (permission && !hasPermission(user, permission)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}
