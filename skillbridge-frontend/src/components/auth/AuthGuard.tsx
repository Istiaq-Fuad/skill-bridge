"use client";

import { useAuthRedirect } from "@/hooks/use-auth-redirect";

/**
 * Component that handles authentication redirects and global auth state changes
 */
export function AuthGuard({ children }: { children: React.ReactNode }) {
  useAuthRedirect();

  return <>{children}</>;
}
