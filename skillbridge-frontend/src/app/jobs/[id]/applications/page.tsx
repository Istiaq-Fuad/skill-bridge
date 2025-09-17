"use client";

import { useState, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { useAuth, useJob } from "@/hooks";
import DashboardLayout from "@/components/layouts/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { ArrowLeft, Users, Mail, Calendar, FileText, Eye } from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";
import { apiClient, JobApplication } from "@/lib/api";

interface JobApplicationsPageProps {
  params: Promise<{ id: string }>;
}

export default function JobApplicationsPage({
  params,
}: JobApplicationsPageProps) {
  const resolvedParams = use(params);
  const [applications, setApplications] = useState<JobApplication[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [updatingStatus, setUpdatingStatus] = useState<number | null>(null);
  const [selectedApplication, setSelectedApplication] =
    useState<JobApplication | null>(null);

  const { user, isLoading: authLoading } = useAuth();
  const { job } = useJob(parseInt(resolvedParams.id));
  const router = useRouter();

  // Auth checks
  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login");
      return;
    }

    if (user && user.role !== "EMPLOYER") {
      router.push("/dashboard");
      return;
    }

    // Check if user owns this job
    if (job && user && job.employerId !== user.id) {
      toast.error("You don't have permission to view these applications");
      router.push("/employer/jobs");
      return;
    }
  }, [user, authLoading, job, router]);

  // Fetch applications
  useEffect(() => {
    const fetchApplications = async () => {
      if (!job) return;

      try {
        setIsLoading(true);
        const response = await apiClient.getJobApplicationsWithDetails(job.id);

        if (response.success && response.data) {
          setApplications(response.data);
        } else {
          toast.error(response.error || "Failed to fetch applications");
        }
      } catch {
        toast.error("Failed to fetch applications");
      } finally {
        setIsLoading(false);
      }
    };

    if (job && user && job.employerId === user.id) {
      fetchApplications();
    }
  }, [job, user]);

  const handleStatusUpdate = async (
    applicationId: number,
    newStatus: JobApplication["status"]
  ) => {
    try {
      setUpdatingStatus(applicationId);
      const response = await apiClient.updateApplicationStatus(
        applicationId,
        newStatus
      );

      if (response.success && response.data) {
        setApplications((prev) =>
          prev.map((app) =>
            app.id === applicationId ? { ...app, status: newStatus } : app
          )
        );
        toast.success(`Application ${newStatus.toLowerCase()}`);
      } else {
        toast.error(response.error || "Failed to update application status");
      }
    } catch {
      toast.error("Failed to update application status");
    } finally {
      setUpdatingStatus(null);
    }
  };

  const getStatusColor = (status: JobApplication["status"]) => {
    switch (status) {
      case "PENDING":
        return "bg-yellow-100 text-yellow-800";
      case "REVIEWED":
        return "bg-blue-100 text-blue-800";
      case "ACCEPTED":
        return "bg-green-100 text-green-800";
      case "REJECTED":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  const getInitials = (
    firstName?: string,
    lastName?: string,
    username?: string
  ) => {
    if (firstName && lastName) {
      return `${firstName[0]}${lastName[0]}`.toUpperCase();
    }
    if (username) {
      return username.slice(0, 2).toUpperCase();
    }
    return "U";
  };

  if (authLoading || !job) {
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
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" asChild>
            <Link href={`/jobs/${job.id}`}>
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Job
            </Link>
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-foreground">Applications</h1>
            <p className="text-muted-foreground">
              {job.title} at {job.company}
            </p>
          </div>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">
                    Total
                  </p>
                  <p className="text-2xl font-bold">{applications.length}</p>
                </div>
                <Users className="h-4 w-4 text-muted-foreground" />
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">
                    Pending
                  </p>
                  <p className="text-2xl font-bold">
                    {
                      applications.filter((app) => app.status === "PENDING")
                        .length
                    }
                  </p>
                </div>
                <FileText className="h-4 w-4 text-muted-foreground" />
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">
                    Accepted
                  </p>
                  <p className="text-2xl font-bold">
                    {
                      applications.filter((app) => app.status === "ACCEPTED")
                        .length
                    }
                  </p>
                </div>
                <Users className="h-4 w-4 text-green-600" />
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">
                    Rejected
                  </p>
                  <p className="text-2xl font-bold">
                    {
                      applications.filter((app) => app.status === "REJECTED")
                        .length
                    }
                  </p>
                </div>
                <Users className="h-4 w-4 text-red-600" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Applications List */}
        {isLoading ? (
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
              </div>
            </CardContent>
          </Card>
        ) : applications.length === 0 ? (
          <Card>
            <CardContent className="pt-6">
              <div className="text-center py-12">
                <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium text-foreground mb-2">
                  No applications yet
                </h3>
                <p className="text-muted-foreground mb-4">
                  When candidates apply to this job, they&apos;ll appear here.
                </p>
              </div>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-4">
            {applications.map((application) => (
              <Card
                key={application.id}
                className="hover:shadow-md transition-shadow"
              >
                <CardContent className="pt-6">
                  <div className="flex items-start justify-between">
                    <div className="flex items-start space-x-4">
                      <Avatar className="h-12 w-12">
                        <AvatarFallback>
                          {getInitials(
                            application.user?.firstName,
                            application.user?.lastName,
                            application.user?.username
                          )}
                        </AvatarFallback>
                      </Avatar>
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 className="text-lg font-semibold">
                            {application.user?.firstName &&
                            application.user?.lastName
                              ? `${application.user.firstName} ${application.user.lastName}`
                              : application.user?.username}
                          </h3>
                          <Badge className={getStatusColor(application.status)}>
                            {application.status}
                          </Badge>
                        </div>
                        <div className="flex items-center text-sm text-muted-foreground space-x-4 mb-2">
                          <div className="flex items-center gap-1">
                            <Mail className="h-4 w-4" />
                            {application.user?.email}
                          </div>
                          <div className="flex items-center gap-1">
                            <Calendar className="h-4 w-4" />
                            Applied {formatDate(application.appliedAt)}
                          </div>
                        </div>
                        {application.coverLetter && (
                          <p className="text-sm text-muted-foreground line-clamp-2">
                            {application.coverLetter}
                          </p>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Dialog>
                        <DialogTrigger asChild>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setSelectedApplication(application)}
                          >
                            <Eye className="mr-2 h-4 w-4" />
                            View Profile
                          </Button>
                        </DialogTrigger>
                        <DialogContent className="max-w-2xl">
                          <DialogHeader>
                            <DialogTitle>Candidate Profile</DialogTitle>
                            <DialogDescription>
                              {application.user?.firstName &&
                              application.user?.lastName
                                ? `${application.user.firstName} ${application.user.lastName}`
                                : application.user?.username}
                            </DialogDescription>
                          </DialogHeader>
                          {selectedApplication && (
                            <div className="space-y-4">
                              <div className="flex items-center space-x-4">
                                <Avatar className="h-16 w-16">
                                  <AvatarFallback>
                                    {getInitials(
                                      selectedApplication.user?.firstName,
                                      selectedApplication.user?.lastName,
                                      selectedApplication.user?.username
                                    )}
                                  </AvatarFallback>
                                </Avatar>
                                <div>
                                  <h3 className="text-lg font-semibold">
                                    {selectedApplication.user?.firstName &&
                                    selectedApplication.user?.lastName
                                      ? `${selectedApplication.user.firstName} ${selectedApplication.user.lastName}`
                                      : selectedApplication.user?.username}
                                  </h3>
                                  <p className="text-muted-foreground">
                                    {selectedApplication.user?.email}
                                  </p>
                                </div>
                              </div>

                              {selectedApplication.coverLetter && (
                                <div>
                                  <h4 className="font-medium mb-2">
                                    Cover Letter
                                  </h4>
                                  <p className="text-sm text-muted-foreground bg-muted p-3 rounded-md">
                                    {selectedApplication.coverLetter}
                                  </p>
                                </div>
                              )}

                              {selectedApplication.profile && (
                                <div>
                                  <h4 className="font-medium mb-2">
                                    Profile Information
                                  </h4>
                                  {selectedApplication.profile.bio && (
                                    <p className="text-sm text-muted-foreground mb-3">
                                      {selectedApplication.profile.bio}
                                    </p>
                                  )}

                                  {selectedApplication.profile.skills.length >
                                    0 && (
                                    <div className="mb-3">
                                      <p className="text-sm font-medium mb-2">
                                        Skills
                                      </p>
                                      <div className="flex flex-wrap gap-2">
                                        {selectedApplication.profile.skills.map(
                                          (skill) => (
                                            <Badge
                                              key={skill.id}
                                              variant="outline"
                                            >
                                              {skill.name}
                                            </Badge>
                                          )
                                        )}
                                      </div>
                                    </div>
                                  )}

                                  {selectedApplication.profile.experience
                                    .length > 0 && (
                                    <div className="mb-3">
                                      <p className="text-sm font-medium mb-2">
                                        Experience
                                      </p>
                                      <div className="space-y-2">
                                        {selectedApplication.profile.experience
                                          .slice(0, 3)
                                          .map((exp) => (
                                            <div
                                              key={exp.id}
                                              className="text-sm"
                                            >
                                              <p className="font-medium">
                                                {exp.position} at {exp.company}
                                              </p>
                                              <p className="text-muted-foreground">
                                                {formatDate(exp.startDate)} -{" "}
                                                {exp.endDate
                                                  ? formatDate(exp.endDate)
                                                  : "Present"}
                                              </p>
                                            </div>
                                          ))}
                                      </div>
                                    </div>
                                  )}
                                </div>
                              )}
                            </div>
                          )}
                        </DialogContent>
                      </Dialog>

                      <Select
                        value={application.status}
                        onValueChange={(value) =>
                          handleStatusUpdate(
                            application.id,
                            value as JobApplication["status"]
                          )
                        }
                        disabled={updatingStatus === application.id}
                      >
                        <SelectTrigger className="w-[140px]">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="PENDING">Pending</SelectItem>
                          <SelectItem value="REVIEWED">Reviewed</SelectItem>
                          <SelectItem value="ACCEPTED">Accepted</SelectItem>
                          <SelectItem value="REJECTED">Rejected</SelectItem>
                        </SelectContent>
                      </Select>
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
