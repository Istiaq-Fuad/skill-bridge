"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/hooks";
import { shouldShowNavItem, roleChecks } from "@/lib/permissions";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  Briefcase,
  FileText,
  User,
  Settings,
  LogOut,
  Plus,
  Users,
  Shield,
} from "lucide-react";

interface NavigationItem {
  href: string;
  label: string;
  icon?: React.ReactNode;
  permission?: string;
  roles?: Array<"JOB_SEEKER" | "EMPLOYER" | "ADMIN">;
  exact?: boolean;
}

const navigationItems: NavigationItem[] = [
  {
    href: "/dashboard",
    label: "Dashboard",
    icon: <Briefcase className="h-4 w-4" />,
  },
  {
    href: "/jobs",
    label: "Browse Jobs",
    icon: <Briefcase className="h-4 w-4" />,
    permission: "jobs",
  },
  {
    href: "/jobs/create",
    label: "Post Job",
    icon: <Plus className="h-4 w-4" />,
    permission: "create-job",
    roles: ["EMPLOYER", "ADMIN"],
  },
  {
    href: "/applications",
    label: "Applications",
    icon: <FileText className="h-4 w-4" />,
    permission: "applications",
  },
  {
    href: "/profile",
    label: "Profile",
    icon: <User className="h-4 w-4" />,
    permission: "profile",
  },
  {
    href: "/admin",
    label: "Admin Panel",
    icon: <Shield className="h-4 w-4" />,
    permission: "admin",
    roles: ["ADMIN"],
  },
];

export function RoleBasedNavigation() {
  const { user, logout } = useAuth();
  const pathname = usePathname();

  if (!user) {
    return (
      <nav className="flex items-center space-x-4">
        <Link href="/jobs">
          <Button variant="ghost">Browse Jobs</Button>
        </Link>
        <Link href="/login">
          <Button variant="ghost">Login</Button>
        </Link>
        <Link href="/register">
          <Button>Sign Up</Button>
        </Link>
      </nav>
    );
  }

  const visibleItems = navigationItems.filter((item) => {
    if (item.permission && !shouldShowNavItem(user, item.permission)) {
      return false;
    }
    if (item.roles && !item.roles.includes(user.role)) {
      return false;
    }
    return true;
  });

  const getRoleColor = (role: string) => {
    switch (role) {
      case "ADMIN":
        return "bg-red-100 text-red-800";
      case "EMPLOYER":
        return "bg-blue-100 text-blue-800";
      case "JOB_SEEKER":
        return "bg-green-100 text-green-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const getRoleLabel = (role: string) => {
    switch (role) {
      case "JOB_SEEKER":
        return "Job Seeker";
      case "EMPLOYER":
        return "Employer";
      case "ADMIN":
        return "Admin";
      default:
        return role;
    }
  };

  return (
    <nav className="flex items-center justify-between w-full">
      {/* Main Navigation */}
      <div className="flex items-center space-x-1">
        {visibleItems.map((item) => {
          const isActive = item.exact
            ? pathname === item.href
            : pathname.startsWith(item.href);

          return (
            <Link key={item.href} href={item.href}>
              <Button
                variant={isActive ? "default" : "ghost"}
                size="sm"
                className="flex items-center space-x-2"
              >
                {item.icon}
                <span>{item.label}</span>
              </Button>
            </Link>
          );
        })}
      </div>

      {/* User Menu */}
      <div className="flex items-center space-x-3">
        {/* Role Badge */}
        <Badge className={getRoleColor(user.role)}>
          {getRoleLabel(user.role)}
        </Badge>

        {/* User Dropdown */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              className="relative h-8 w-8 rounded-full p-0"
            >
              <Avatar className="h-8 w-8">
                <AvatarImage
                  src={`/avatars/${user.id}.png`}
                  alt={user.username}
                />
                <AvatarFallback>
                  {user.firstName?.[0] || user.username[0].toUpperCase()}
                </AvatarFallback>
              </Avatar>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent className="w-56" align="end" forceMount>
            <DropdownMenuLabel className="font-normal">
              <div className="flex flex-col space-y-1">
                <p className="text-sm font-medium leading-none">
                  {user.firstName
                    ? `${user.firstName} ${user.lastName || ""}`
                    : user.username}
                </p>
                <p className="text-xs leading-none text-muted-foreground">
                  {user.email}
                </p>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />

            <DropdownMenuItem asChild>
              <Link href="/profile" className="flex items-center">
                <User className="mr-2 h-4 w-4" />
                <span>Profile</span>
              </Link>
            </DropdownMenuItem>

            <DropdownMenuItem asChild>
              <Link href="/settings" className="flex items-center">
                <Settings className="mr-2 h-4 w-4" />
                <span>Settings</span>
              </Link>
            </DropdownMenuItem>

            {roleChecks.isAdmin(user) && (
              <>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild>
                  <Link href="/admin/users" className="flex items-center">
                    <Users className="mr-2 h-4 w-4" />
                    <span>Manage Users</span>
                  </Link>
                </DropdownMenuItem>
              </>
            )}

            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={logout} className="text-red-600">
              <LogOut className="mr-2 h-4 w-4" />
              <span>Log out</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </nav>
  );
}

// Breadcrumb navigation with role-based access
interface BreadcrumbItem {
  label: string;
  href?: string;
  current?: boolean;
}

interface RoleBreadcrumbsProps {
  items: BreadcrumbItem[];
}

export function RoleBreadcrumbs({ items }: RoleBreadcrumbsProps) {
  const { user } = useAuth();

  const filteredItems = items.filter((item) => {
    if (!item.href) return true;
    return shouldShowNavItem(user, item.href.split("/")[1] || "");
  });

  return (
    <nav className="flex" aria-label="Breadcrumb">
      <ol className="flex items-center space-x-4">
        {filteredItems.map((item, index) => (
          <li key={index}>
            <div className="flex items-center">
              {index > 0 && (
                <svg
                  className="flex-shrink-0 h-5 w-5 text-gray-400"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                  aria-hidden="true"
                >
                  <path d="M5.555 17.776l8-16 .894.448-8 16-.894-.448z" />
                </svg>
              )}
              {item.href && !item.current ? (
                <Link
                  href={item.href}
                  className="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                >
                  {item.label}
                </Link>
              ) : (
                <span
                  className="ml-4 text-sm font-medium text-gray-900"
                  aria-current={item.current ? "page" : undefined}
                >
                  {item.label}
                </span>
              )}
            </div>
          </li>
        ))}
      </ol>
    </nav>
  );
}
