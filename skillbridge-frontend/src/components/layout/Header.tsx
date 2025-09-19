"use client";

import Link from "next/link";
import { RoleBasedNavigation } from "@/components/auth/RoleBasedNavigation";
import { ThemeToggle } from "@/components/theme-toggle";

export function Header() {
  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <div className="flex items-center space-x-2">
            <Link href="/" className="flex items-center space-x-2">
              <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
                <span className="text-primary-foreground font-bold text-sm">
                  SB
                </span>
              </div>
              <span className="hidden sm:inline-block font-bold text-xl">
                SkillBridge
              </span>
            </Link>
          </div>

          {/* Navigation */}
          <div className="flex-1 mx-8">
            <RoleBasedNavigation />
          </div>

          {/* Theme Toggle */}
          <div className="flex items-center space-x-2">
            <ThemeToggle />
          </div>
        </div>
      </div>
    </header>
  );
}
