"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  useAuthStore,
  useUser,
  useIsAuthenticated,
  useJobsStore,
  useJobs,
  useJobsLoading,
  useApplicationsStore,
  useUserApplications,
} from "@/stores";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";

export default function DashboardWithZustand() {
  const router = useRouter();

  // Auth state and actions
  const logout = useAuthStore((state) => state.logout);
  const user = useUser();
  const isAuthenticated = useIsAuthenticated();

  // Jobs state and actions
  const { fetchJobs, setSearchFilters } = useJobsStore();
  const jobs = useJobs();
  const jobsLoading = useJobsLoading();

  // Applications state and actions
  const { fetchUserApplications, applyForJob } = useApplicationsStore();
  const applications = useUserApplications();

  // Redirect if not authenticated
  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/login");
    }
  }, [isAuthenticated, router]);

  // Fetch initial data
  useEffect(() => {
    if (isAuthenticated && user) {
      // Fetch recent jobs
      fetchJobs({ page: 0, size: 5 });

      // Fetch user's applications
      fetchUserApplications(user.id);
    }
  }, [isAuthenticated, user, fetchJobs, fetchUserApplications]);

  const handleLogout = () => {
    logout();
    toast.success("Logged out successfully");
    router.push("/login");
  };

  const handleApplyForJob = async (jobId: number) => {
    const result = await applyForJob(jobId);
    if (result.success) {
      toast.success("Applied for job successfully!");
    } else {
      toast.error(result.error || "Failed to apply for job");
    }
  };

  const handleSearchJobs = (query: string) => {
    setSearchFilters({ search: query });
    fetchJobs({ search: query, page: 0, size: 10 });
  };

  if (!isAuthenticated || !user) {
    return <div>Loading...</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold">
            Welcome back, {user.firstName || user.username}!
          </h1>
          <p className="text-muted-foreground">
            Here&apos;s what&apos;s happening with your job search
          </p>
        </div>
        <Button variant="outline" onClick={handleLogout}>
          Logout
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* User Applications */}
        <Card>
          <CardHeader>
            <CardTitle>My Applications</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {applications.length === 0 ? (
                <p className="text-muted-foreground">No applications yet</p>
              ) : (
                applications.slice(0, 3).map((application) => (
                  <div
                    key={application.id}
                    className="flex justify-between items-center"
                  >
                    <span className="text-sm">{application.job?.title}</span>
                    <Badge
                      variant={
                        application.status === "ACCEPTED"
                          ? "default"
                          : application.status === "REJECTED"
                          ? "destructive"
                          : "secondary"
                      }
                    >
                      {application.status}
                    </Badge>
                  </div>
                ))
              )}
            </div>
            <Button
              variant="link"
              className="mt-4 p-0"
              onClick={() => router.push("/applications")}
            >
              View all applications
            </Button>
          </CardContent>
        </Card>

        {/* Recent Jobs */}
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle>Recent Job Opportunities</CardTitle>
          </CardHeader>
          <CardContent>
            {jobsLoading ? (
              <div>Loading jobs...</div>
            ) : (
              <div className="space-y-4">
                {jobs.slice(0, 3).map((job) => (
                  <div key={job.id} className="border rounded-lg p-4">
                    <div className="flex justify-between items-start mb-2">
                      <h3 className="font-semibold">{job.title}</h3>
                      <Badge variant="outline">{job.company}</Badge>
                    </div>
                    <p className="text-sm text-muted-foreground mb-2">
                      {job.location}
                    </p>
                    <p className="text-sm line-clamp-2 mb-3">
                      {job.description}
                    </p>
                    <div className="flex justify-between items-center">
                      <div className="flex gap-2">
                        {job.requirements.slice(0, 2).map((req, index) => (
                          <Badge
                            key={index}
                            variant="secondary"
                            className="text-xs"
                          >
                            {req}
                          </Badge>
                        ))}
                      </div>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => router.push(`/jobs/${job.id}`)}
                        >
                          View Details
                        </Button>
                        <Button
                          size="sm"
                          onClick={() => handleApplyForJob(job.id)}
                          disabled={applications.some(
                            (app) => app.jobId === job.id
                          )}
                        >
                          {applications.some((app) => app.jobId === job.id)
                            ? "Applied"
                            : "Apply"}
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
            <Button
              variant="link"
              className="mt-4 p-0"
              onClick={() => router.push("/jobs")}
            >
              Browse all jobs
            </Button>
          </CardContent>
        </Card>

        {/* Quick Actions */}
        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Button
              className="w-full justify-start"
              variant="outline"
              onClick={() => router.push("/profile")}
            >
              Update Profile
            </Button>
            <Button
              className="w-full justify-start"
              variant="outline"
              onClick={() => handleSearchJobs("React")}
            >
              Find React Jobs
            </Button>
            <Button
              className="w-full justify-start"
              variant="outline"
              onClick={() => handleSearchJobs("Remote")}
            >
              Find Remote Jobs
            </Button>
          </CardContent>
        </Card>

        {/* Profile Completion */}
        <Card>
          <CardHeader>
            <CardTitle>Profile Status</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-sm">Basic Info</span>
                <Badge variant="default">Complete</Badge>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm">Skills</span>
                <Badge variant="secondary">Needs Update</Badge>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm">Experience</span>
                <Badge variant="secondary">Needs Update</Badge>
              </div>
            </div>
            <Button
              variant="link"
              className="mt-4 p-0"
              onClick={() => router.push("/profile")}
            >
              Complete your profile
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
