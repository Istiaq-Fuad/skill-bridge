"use client";

import { useState, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/hooks";
import { useJob, useApplyForJob } from "@/hooks/api";
import { useJobsStore } from "@/stores";
import DashboardLayout from "@/components/layouts/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
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
  ArrowLeft,
  Building,
  MapPin,
  DollarSign,
  Calendar,
  Users,
  CheckCircle,
} from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";

interface JobDetailPageProps {
  params: Promise<{
    id: string;
  }>;
}

export default function JobDetailPage({ params }: JobDetailPageProps) {
  const resolvedParams = use(params);
  const [applied, setApplied] = useState(false);
  const [applying, setApplying] = useState(false);
  const [showApplicationDialog, setShowApplicationDialog] = useState(false);
  const { user, isLoading: authLoading } = useAuth();
  const { job } = useJob(parseInt(resolvedParams.id));
  const { applyForJob } = useApplyForJob();
  const { isLoading: jobLoading, error: jobError } = useJobsStore();
  const router = useRouter();

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login");
      return;
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (jobError) {
      toast.error("Error fetching job details");
      router.push("/jobs");
    }
  }, [jobError, router]);

  const handleApply = async () => {
    if (!job) return;

    try {
      setApplying(true);
      const result = await applyForJob(job.id);
      if (result.success) {
        setApplied(true);
        setShowApplicationDialog(false);
        toast.success("Application submitted successfully!");
      } else {
        toast.error(result.error || "Failed to submit application");
      }
    } catch {
      toast.error("Error submitting application");
    } finally {
      setApplying(false);
    }
  };

  if (authLoading || jobLoading) {
    return (
      <DashboardLayout>
        <div className="space-y-6">
          <div className="flex items-center gap-4">
            <Skeleton className="h-8 w-8" />
            <Skeleton className="h-8 w-32" />
          </div>
          <Card>
            <CardHeader>
              <Skeleton className="h-8 w-3/4" />
              <div className="space-y-2">
                <Skeleton className="h-4 w-1/2" />
                <Skeleton className="h-4 w-1/3" />
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-3/4" />
              <div className="flex gap-2">
                <Skeleton className="h-6 w-16" />
                <Skeleton className="h-6 w-16" />
                <Skeleton className="h-6 w-16" />
              </div>
            </CardContent>
          </Card>
        </div>
      </DashboardLayout>
    );
  }

  if (!user || !job) {
    return null;
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Back Button */}
        <Button variant="ghost" asChild className="mb-4">
          <Link href="/jobs">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Jobs
          </Link>
        </Button>

        {/* Job Header */}
        <Card>
          <CardHeader>
            <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
              <div className="flex-1">
                <CardTitle className="text-2xl lg:text-3xl mb-3">
                  {job.title}
                </CardTitle>
                <div className="flex flex-wrap items-center gap-4 text-muted-foreground">
                  <div className="flex items-center">
                    <Building className="h-5 w-5 mr-2" />
                    <span className="font-medium">{job.company}</span>
                  </div>
                  <div className="flex items-center">
                    <MapPin className="h-5 w-5 mr-2" />
                    <span>{job.location}</span>
                  </div>
                  {job.salary && (
                    <div className="flex items-center">
                      <DollarSign className="h-5 w-5 mr-2" />
                      <span className="font-medium">
                        ${job.salary.toLocaleString()}/year
                      </span>
                    </div>
                  )}
                  <div className="flex items-center">
                    <Calendar className="h-5 w-5 mr-2" />
                    <span>
                      Posted {new Date(job.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                </div>
              </div>

              <div className="flex flex-col gap-3">
                {user.role === "JOB_SEEKER" && (
                  <>
                    {applied ? (
                      <Alert className="border-green-200 bg-green-50">
                        <CheckCircle className="h-4 w-4 text-green-600" />
                        <AlertDescription className="text-green-800">
                          Application submitted successfully!
                        </AlertDescription>
                      </Alert>
                    ) : (
                      <Dialog
                        open={showApplicationDialog}
                        onOpenChange={setShowApplicationDialog}
                      >
                        <DialogTrigger asChild>
                          <Button size="lg" className="w-full lg:w-auto">
                            Apply Now
                          </Button>
                        </DialogTrigger>
                        <DialogContent>
                          <DialogHeader>
                            <DialogTitle>Apply for {job.title}</DialogTitle>
                            <DialogDescription>
                              Are you sure you want to apply for this position
                              at {job.company}?
                            </DialogDescription>
                          </DialogHeader>
                          <div className="py-4">
                            <Alert>
                              <AlertDescription>
                                Your profile information will be shared with the
                                employer. Make sure your profile is complete and
                                up-to-date.
                              </AlertDescription>
                            </Alert>
                          </div>
                          <DialogFooter>
                            <Button
                              variant="outline"
                              onClick={() => setShowApplicationDialog(false)}
                            >
                              Cancel
                            </Button>
                            <Button onClick={handleApply} disabled={applying}>
                              {applying
                                ? "Submitting..."
                                : "Submit Application"}
                            </Button>
                          </DialogFooter>
                        </DialogContent>
                      </Dialog>
                    )}
                    <Button
                      variant="outline"
                      size="lg"
                      className="w-full lg:w-auto"
                    >
                      Save Job
                    </Button>
                  </>
                )}

                {user.role === "EMPLOYER" && job.employerId === user.id && (
                  <div className="flex flex-col gap-2">
                    <Button asChild size="lg">
                      <Link href={`/jobs/${job.id}/edit`}>Edit Job</Link>
                    </Button>
                    <Button asChild variant="outline" size="lg">
                      <Link href={`/jobs/${job.id}/applications`}>
                        <Users className="mr-2 h-4 w-4" />
                        View Applications
                      </Link>
                    </Button>
                  </div>
                )}
              </div>
            </div>
          </CardHeader>
        </Card>

        {/* Job Details */}
        <div className="grid lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            {/* Description */}
            <Card>
              <CardHeader>
                <CardTitle>Job Description</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="prose max-w-none">
                  <p className="text-card-foreground leading-relaxed whitespace-pre-wrap">
                    {job.description}
                  </p>
                </div>
              </CardContent>
            </Card>

            {/* Requirements */}
            {job.requirements && job.requirements.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Requirements</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {job.requirements.map((requirement, index) => (
                      <div key={index} className="flex items-start">
                        <div className="w-2 h-2 bg-primary rounded-full mt-2 mr-3 flex-shrink-0"></div>
                        <span className="text-card-foreground">
                          {requirement}
                        </span>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Quick Info */}
            <Card>
              <CardHeader>
                <CardTitle>Job Information</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <dt className="text-sm font-medium text-muted-foreground mb-1">
                    Company
                  </dt>
                  <dd className="text-sm text-foreground">{job.company}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-muted-foreground mb-1">
                    Location
                  </dt>
                  <dd className="text-sm text-foreground">{job.location}</dd>
                </div>
                {job.salary && (
                  <div>
                    <dt className="text-sm font-medium text-muted-foreground mb-1">
                      Salary
                    </dt>
                    <dd className="text-sm text-foreground">
                      ${job.salary.toLocaleString()}/year
                    </dd>
                  </div>
                )}
                <div>
                  <dt className="text-sm font-medium text-muted-foreground mb-1">
                    Posted
                  </dt>
                  <dd className="text-sm text-foreground">
                    {new Date(job.createdAt).toLocaleDateString()}
                  </dd>
                </div>
              </CardContent>
            </Card>

            {/* Skills/Tags */}
            {job.requirements && job.requirements.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Skills & Requirements</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex flex-wrap gap-2">
                    {job.requirements.map((requirement, index) => (
                      <Badge
                        key={index}
                        variant="secondary"
                        className="text-xs"
                      >
                        {requirement}
                      </Badge>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Similar Jobs */}
            <Card>
              <CardHeader>
                <CardTitle>Similar Jobs</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-center py-6 text-muted-foreground">
                  <p className="text-sm">No similar jobs found</p>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
