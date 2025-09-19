"use client";

import { useEffect, useState } from "react";
import { useAuthStore } from "@/stores";

export function HydrationProvider({ children }: { children: React.ReactNode }) {
  const [isHydrated, setIsHydrated] = useState(false);
  const setLoading = useAuthStore((state) => state.setLoading);

  useEffect(() => {
    // Ensure Zustand store is hydrated before rendering
    const unsubscribe = useAuthStore.persist.onFinishHydration(() => {
      setIsHydrated(true);
      setLoading(false);
    });

    // Handle case where hydration is already complete
    if (useAuthStore.persist.hasHydrated()) {
      setIsHydrated(true);
      setLoading(false);
    }

    return unsubscribe;
  }, [setLoading]);

  if (!isHydrated) {
    // Show loading during hydration
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
      </div>
    );
  }

  return <>{children}</>;
}
