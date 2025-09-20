"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/hooks";
import { useUserApplications, useUpdateApplicationStatus } from "@/hooks/api";
import DashboardLayout from "@/components/layouts/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { JobApplication } from "@/lib/api";
import { Calendar, Building, MapPin, Eye, MoreHorizontal } from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";

export default function ApplicationsPage() {
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const { user, isLoading } = useAuth();
  const {
    userApplications: applications,
    isLoading: loading,
    refreshUserApplications,
  } = useUserApplications(user?.id || 0);
  const { updateApplicationStatus } = useUpdateApplicationStatus();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !user) {
      router.push("/login");
      return;
    }
  }, [user, isLoading, router]);

  const handleStatusUpdate = async (
    applicationId: number,
    newStatus: JobApplication["status"]
  ) => {
    try {
      const result = await updateApplicationStatus(applicationId, newStatus);
      if (result.success) {
        toast.success("Application status updated");
        // Refresh the applications to show the updated status
        refreshUserApplications();
      } else {
        toast.error(result.error || "Failed to update status");
      }
    } catch {
      toast.error("Error updating status");
    }
  };

  const getStatusBadgeVariant = (status: JobApplication["status"]) => {
    switch (status) {
      case "PENDING":
        return "default";
      case "REVIEWED":
        return "secondary";
      case "ACCEPTED":
        return "default"; // Success variant would be better but using default
      case "REJECTED":
        return "destructive";
      default:
        return "default";
    }
  };

  const filteredApplications =
    statusFilter === "all"
      ? applications
      : applications.filter((app) => app.status === statusFilter);

  if (isLoading || loading) {
    return (
      <DashboardLayout>
        <div className="space-y-6">
          <div className="flex justify-between items-center">
            <Skeleton className="h-8 w-48" />
            <Skeleton className="h-10 w-32" />
          </div>
          <Card>
            <CardHeader>
              <Skeleton className="h-6 w-32" />
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, i) => (
                  <div key={i} className="flex items-center space-x-4">
                    <Skeleton className="h-4 w-1/4" />
                    <Skeleton className="h-4 w-1/4" />
                    <Skeleton className="h-4 w-1/4" />
                    <Skeleton className="h-4 w-1/4" />
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      </DashboardLayout>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-foreground">
              {user.role === "JOB_SEEKER"
                ? "My Applications"
                : "Job Applications"}
            </h1>
            <p className="text-muted-foreground">
              {user.role === "JOB_SEEKER"
                ? "Track your job applications and their status"
                : "Manage applications for your job postings"}
            </p>
          </div>

          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Applications</SelectItem>
              <SelectItem value="PENDING">Pending</SelectItem>
              <SelectItem value="REVIEWED">Reviewed</SelectItem>
              <SelectItem value="ACCEPTED">Accepted</SelectItem>
              <SelectItem value="REJECTED">Rejected</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Applications Table */}
        <Card>
          <CardHeader>
            <CardTitle>Applications ({filteredApplications.length})</CardTitle>
          </CardHeader>
          <CardContent>
            {filteredApplications.length === 0 ? (
              <div className="text-center py-12">
                <div className="w-12 h-12 bg-muted rounded-lg flex items-center justify-center mx-auto mb-4">
                  <Building className="h-6 w-6 text-muted-foreground" />
                </div>
                <h3 className="text-lg font-medium text-foreground mb-2">
                  No applications found
                </h3>
                <p className="text-muted-foreground mb-4">
                  {user.role === "JOB_SEEKER"
                    ? "You haven't applied to any jobs yet."
                    : "No applications received yet."}
                </p>
                {user.role === "JOB_SEEKER" && (
                  <Button asChild>
                    <Link href="/jobs">Browse Jobs</Link>
                  </Button>
                )}
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Job Title</TableHead>
                    <TableHead>Company</TableHead>
                    <TableHead>Location</TableHead>
                    <TableHead>Applied Date</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredApplications.map((application) => (
                    <TableRow key={application.id}>
                      <TableCell className="font-medium">
                        {application.job ? (
                          <Link
                            href={`/jobs/${application.job.id}`}
                            className="hover:text-primary transition-colors"
                          >
                            {application.job.title}
                          </Link>
                        ) : (
                          `Job #${application.jobId}`
                        )}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center">
                          <Building className="h-4 w-4 mr-2 text-muted-foreground" />
                          {application.job?.company || "N/A"}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center">
                          <MapPin className="h-4 w-4 mr-2 text-muted-foreground" />
                          {application.job?.location || "N/A"}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center">
                          <Calendar className="h-4 w-4 mr-2 text-muted-foreground" />
                          {new Date(application.appliedAt).toLocaleDateString()}
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant={getStatusBadgeVariant(application.status)}
                        >
                          {application.status.toLowerCase()}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          {application.job && (
                            <Button variant="ghost" size="sm" asChild>
                              <Link href={`/jobs/${application.job.id}`}>
                                <Eye className="h-4 w-4" />
                              </Link>
                            </Button>
                          )}

                          {user.role === "EMPLOYER" && (
                            <Select
                              value={application.status}
                              onValueChange={(
                                value: JobApplication["status"]
                              ) => handleStatusUpdate(application.id, value)}
                            >
                              <SelectTrigger className="w-[120px] h-8">
                                <MoreHorizontal className="h-4 w-4" />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="PENDING">Pending</SelectItem>
                                <SelectItem value="REVIEWED">
                                  Reviewed
                                </SelectItem>
                                <SelectItem value="ACCEPTED">
                                  Accepted
                                </SelectItem>
                                <SelectItem value="REJECTED">
                                  Rejected
                                </SelectItem>
                              </SelectContent>
                            </Select>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>

        {/* Stats Cards for Employers */}
        {user.role === "EMPLOYER" && applications.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Total Applications
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{applications.length}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Pending Review
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {
                    applications.filter((app) => app.status === "PENDING")
                      .length
                  }
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Accepted
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-green-600">
                  {
                    applications.filter((app) => app.status === "ACCEPTED")
                      .length
                  }
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Rejected
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-red-600">
                  {
                    applications.filter((app) => app.status === "REJECTED")
                      .length
                  }
                </div>
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}
