"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth, useEmployerJobs } from "@/hooks";
import DashboardLayout from "@/components/layouts/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Plus,
  MoreVertical,
  Edit,
  Trash2,
  Users,
  MapPin,
  DollarSign,
  Calendar,
  Eye,
} from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";
import { apiClient } from "@/lib/api";
import { CanCreateJobs } from "@/components/auth/RoleBasedUI";

export default function EmployerJobsPage() {
  const { user, isLoading: authLoading } = useAuth();
  const { employerJobs, isLoading, error, fetchEmployerJobs } =
    useEmployerJobs();
  const [deletingJobId, setDeletingJobId] = useState<number | null>(null);
  const router = useRouter();

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login");
      return;
    }

    if (user && user.role !== "EMPLOYER") {
      router.push("/dashboard");
      return;
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (user?.role === "EMPLOYER") {
      fetchEmployerJobs();
    }
  }, [user, fetchEmployerJobs]);

  const handleDeleteJob = async (jobId: number) => {
    try {
      setDeletingJobId(jobId);
      const response = await apiClient.deleteJob(jobId);

      if (response.success) {
        toast.success("Job deleted successfully");
        fetchEmployerJobs(); // Refresh the list
      } else {
        toast.error(response.error || "Failed to delete job");
      }
    } catch {
      toast.error("Failed to delete job");
    } finally {
      setDeletingJobId(null);
    }
  };

  const formatSalary = (salary?: number) => {
    if (!salary) return "Salary not specified";
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
      maximumFractionDigits: 0,
    }).format(salary);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  if (authLoading || isLoading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        </div>
      </DashboardLayout>
    );
  }

  if (!user || user.role !== "EMPLOYER") {
    return null;
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-foreground">My Job Posts</h1>
            <p className="text-muted-foreground">
              Manage your job postings and track applications
            </p>
          </div>
          <CanCreateJobs>
            <Button asChild>
              <Link href="/jobs/create">
                <Plus className="mr-2 h-4 w-4" />
                Post New Job
              </Link>
            </Button>
          </CanCreateJobs>
        </div>

        {/* Error State */}
        {error && (
          <Card className="border-red-200 bg-red-50">
            <CardContent className="pt-6">
              <div className="text-center text-red-600">
                <p className="text-sm">{error}</p>
                <Button
                  variant="outline"
                  size="sm"
                  className="mt-2"
                  onClick={fetchEmployerJobs}
                >
                  Try Again
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Empty State */}
        {!error && employerJobs.length === 0 && (
          <Card>
            <CardContent className="pt-6">
              <div className="text-center py-12">
                <div className="w-12 h-12 bg-muted rounded-lg flex items-center justify-center mx-auto mb-4">
                  <Plus className="h-6 w-6 text-muted-foreground" />
                </div>
                <h3 className="text-lg font-medium text-foreground mb-2">
                  No job posts yet
                </h3>
                <p className="text-muted-foreground mb-4">
                  Start attracting talent by posting your first job.
                </p>
                <CanCreateJobs>
                  <Button asChild>
                    <Link href="/jobs/create">Post Your First Job</Link>
                  </Button>
                </CanCreateJobs>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Jobs List */}
        {employerJobs.length > 0 && (
          <div className="grid gap-4">
            {employerJobs.map((job) => (
              <Card key={job.id} className="hover:shadow-md transition-shadow">
                <CardHeader>
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <CardTitle className="text-xl mb-2">
                        {job.title}
                      </CardTitle>
                      <div className="flex flex-wrap gap-4 text-sm text-muted-foreground mb-2">
                        <div className="flex items-center gap-1">
                          <MapPin className="h-4 w-4" />
                          {job.location}
                        </div>
                        {job.salary && (
                          <div className="flex items-center gap-1">
                            <DollarSign className="h-4 w-4" />
                            {formatSalary(job.salary)}
                          </div>
                        )}
                        <div className="flex items-center gap-1">
                          <Calendar className="h-4 w-4" />
                          Posted {formatDate(job.createdAt)}
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge variant="secondary">Active</Badge>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="sm">
                            <MoreVertical className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem asChild>
                            <Link href={`/jobs/${job.id}`}>
                              <Eye className="mr-2 h-4 w-4" />
                              View Job
                            </Link>
                          </DropdownMenuItem>
                          <DropdownMenuItem asChild>
                            <Link href={`/jobs/${job.id}/edit`}>
                              <Edit className="mr-2 h-4 w-4" />
                              Edit Job
                            </Link>
                          </DropdownMenuItem>
                          <DropdownMenuItem asChild>
                            <Link href={`/jobs/${job.id}/applications`}>
                              <Users className="mr-2 h-4 w-4" />
                              View Applications
                            </Link>
                          </DropdownMenuItem>
                          <Dialog>
                            <DialogTrigger asChild>
                              <DropdownMenuItem
                                className="text-red-600"
                                onSelect={(e) => e.preventDefault()}
                              >
                                <Trash2 className="mr-2 h-4 w-4" />
                                Delete Job
                              </DropdownMenuItem>
                            </DialogTrigger>
                            <DialogContent>
                              <DialogHeader>
                                <DialogTitle>Delete Job Post</DialogTitle>
                                <DialogDescription>
                                  Are you sure you want to delete &quot;
                                  {job.title}&quot;? This action cannot be
                                  undone and will also remove all associated
                                  applications.
                                </DialogDescription>
                              </DialogHeader>
                              <DialogFooter>
                                <Button variant="outline">Cancel</Button>
                                <Button
                                  onClick={() => handleDeleteJob(job.id)}
                                  disabled={deletingJobId === job.id}
                                  variant="destructive"
                                >
                                  {deletingJobId === job.id
                                    ? "Deleting..."
                                    : "Delete"}
                                </Button>
                              </DialogFooter>
                            </DialogContent>
                          </Dialog>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground text-sm line-clamp-2 mb-4">
                    {job.description}
                  </p>
                  <div className="flex justify-between items-center">
                    <div className="flex gap-2">
                      {job.requirements.slice(0, 3).map((req, index) => (
                        <Badge
                          key={index}
                          variant="outline"
                          className="text-xs"
                        >
                          {req}
                        </Badge>
                      ))}
                      {job.requirements.length > 3 && (
                        <Badge variant="outline" className="text-xs">
                          +{job.requirements.length - 3} more
                        </Badge>
                      )}
                    </div>
                    <div className="flex gap-2">
                      <Button variant="outline" size="sm" asChild>
                        <Link href={`/jobs/${job.id}/applications`}>
                          <Users className="mr-2 h-4 w-4" />
                          Applications
                        </Link>
                      </Button>
                      <Button size="sm" asChild>
                        <Link href={`/jobs/${job.id}/edit`}>
                          <Edit className="mr-2 h-4 w-4" />
                          Edit
                        </Link>
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}
