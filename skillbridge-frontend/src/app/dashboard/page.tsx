"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth, useEmployerDashboardStats } from "@/hooks";
import DashboardLayout from "@/components/layouts/DashboardLayout";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Briefcase,
  Users,
  FileText,
  TrendingUp,
  Plus,
  Eye,
} from "lucide-react";
import Link from "next/link";

export default function DashboardPage() {
  const { user, isLoading } = useAuth();
  const router = useRouter();
  const { dashboardStats, isLoadingStats, fetchStats } =
    useEmployerDashboardStats();

  useEffect(() => {
    if (!isLoading && !user) {
      router.push("/login");
    }
  }, [user, isLoading, router]);

  useEffect(() => {
    if (user?.role === "EMPLOYER") {
      fetchStats();
    }
  }, [user, fetchStats]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <DashboardLayout>
      <div className="space-y-6 lg:space-y-8">
        {/* Welcome Section */}
        <div className="bg-card rounded-lg shadow-sm border p-6">
          <h1 className="text-2xl font-bold text-foreground mb-2">
            Welcome back, {user.firstName || user.username}!
          </h1>
          <p className="text-muted-foreground">
            {user.role === "JOB_SEEKER"
              ? "Find your next opportunity and advance your career."
              : "Manage your job postings and find the best talent."}
          </p>
          <Badge variant="secondary" className="mt-2">
            {user.role === "JOB_SEEKER" ? "Job Seeker" : "Employer"}
          </Badge>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 lg:gap-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {user.role === "JOB_SEEKER" ? "Applications" : "Job Posts"}
              </CardTitle>
              <FileText className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {isLoadingStats
                  ? "..."
                  : user.role === "EMPLOYER"
                  ? dashboardStats?.totalJobs || 0
                  : 0}
              </div>
              <p className="text-xs text-muted-foreground">
                {user.role === "JOB_SEEKER"
                  ? "Active applications"
                  : "Active listings"}
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {user.role === "JOB_SEEKER" ? "Interviews" : "Applications"}
              </CardTitle>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {isLoadingStats
                  ? "..."
                  : user.role === "EMPLOYER"
                  ? dashboardStats?.totalApplications || 0
                  : 0}
              </div>
              <p className="text-xs text-muted-foreground">
                {user.role === "JOB_SEEKER"
                  ? "Scheduled interviews"
                  : "Total received"}
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {user.role === "JOB_SEEKER" ? "Profile Views" : "Pending"}
              </CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {isLoadingStats
                  ? "..."
                  : user.role === "EMPLOYER"
                  ? dashboardStats?.pendingApplications || 0
                  : 0}
              </div>
              <p className="text-xs text-muted-foreground">
                {user.role === "JOB_SEEKER" ? "This month" : "Pending review"}
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {user.role === "JOB_SEEKER" ? "Saved Jobs" : "Response Rate"}
              </CardTitle>
              <Briefcase className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {isLoadingStats
                  ? "..."
                  : user.role === "EMPLOYER"
                  ? `${dashboardStats?.responseRate || 0}%`
                  : 0}
              </div>
              <p className="text-xs text-muted-foreground">
                {user.role === "JOB_SEEKER"
                  ? "Bookmarked positions"
                  : "Average response"}
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 lg:gap-6">
          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
              <CardDescription>
                {user.role === "JOB_SEEKER"
                  ? "Get started with your job search"
                  : "Manage your hiring process"}
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {user.role === "JOB_SEEKER" ? (
                <>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-primary rounded-full"></div>
                    <span className="text-sm">Complete your profile</span>
                  </div>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-primary rounded-full"></div>
                    <span className="text-sm">Browse available jobs</span>
                  </div>
                  <div className="flex items-center space-x-4">
                    <div className="w-2 h-2 bg-muted rounded-full"></div>
                    <span className="text-sm text-muted-foreground">
                      Upload your resume
                    </span>
                  </div>
                </>
              ) : (
                <>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className="w-2 h-2 bg-primary rounded-full"></div>
                      <span className="text-sm">Post a new job</span>
                    </div>
                    <Button asChild size="sm">
                      <Link href="/jobs/create">Post Job</Link>
                    </Button>
                  </div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className="w-2 h-2 bg-primary rounded-full"></div>
                      <span className="text-sm">Manage job posts</span>
                    </div>
                    <Button asChild variant="outline" size="sm">
                      <Link href="/employer/jobs">View Jobs</Link>
                    </Button>
                  </div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className="w-2 h-2 bg-primary rounded-full"></div>
                      <span className="text-sm">Update company profile</span>
                    </div>
                    <Button asChild variant="outline" size="sm">
                      <Link href="/profile">Profile</Link>
                    </Button>
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Recent Activity</CardTitle>
              <CardDescription>
                Your latest activities on the platform
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-6 text-muted-foreground">
                <p className="text-sm">No recent activity</p>
                <p className="text-xs mt-1">
                  Start exploring to see your activity here
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
}
