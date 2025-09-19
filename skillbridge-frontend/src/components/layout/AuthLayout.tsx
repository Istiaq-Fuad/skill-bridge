"use client";

import { Header } from "@/components/layout/Header";
import { AuthProvider } from "@/contexts/AuthContext";

interface AuthLayoutProps {
  children: React.ReactNode;
}

export function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <AuthProvider>
      <div className="min-h-screen bg-background">
        <Header />
        <main className="container mx-auto px-4 py-8">{children}</main>
      </div>
    </AuthProvider>
  );
}
