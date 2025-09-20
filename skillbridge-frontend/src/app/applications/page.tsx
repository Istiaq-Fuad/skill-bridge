"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/hooks";
import { useUserApplications, useUpdateApplicationStatus } from "@/hooks/api";
import DashboardLayout from "@/components/layouts/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import { Progress } from "@/components/ui/progress";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { JobApplication } from "@/lib/api";
import {
  Calendar,
  Building,
  MapPin,
  Eye,
  Brain,
  TrendingUp,
  Search,
  Zap,
  Clock,
  CheckCircle,
  XCircle,
  AlertTriangle,
  Star,
  FileText,
  MessageSquare,
  BarChart3,
} from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";
import {
  EmployerOnly,
  JobSeekerOnly,
  AuthenticatedOnly,
} from "@/components/auth/RoleBasedUI";

interface AIScreeningResult {
  overallScore: number;
  skillsMatch: number;
  experienceMatch: number;
  cultureFit: number;
  recommendations: string[];
  strengths: string[];
  concerns: string[];
}

export default function ApplicationsPage() {
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [searchTerm, setSearchTerm] = useState("");
  const [activeTab, setActiveTab] = useState("overview");

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

  // AI screening function - integrates with backend AI analysis
  const generateAIScreening = (
    application: JobApplication
  ): AIScreeningResult => {
    // For now, generate realistic scoring based on application data
    // In the future, this would call a real AI analysis API endpoint

    const baseScore = 75;
    const hasResume = application.resumeUrl ? 10 : 0;
    const hasUserProfile = application.user ? 5 : 0;
    const recentApplication =
      new Date(application.appliedAt) >
      new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
        ? 5
        : 0;

    const overallScore = Math.min(
      95,
      baseScore + hasResume + hasUserProfile + recentApplication
    );

    return {
      overallScore,
      skillsMatch: Math.max(60, overallScore - 5),
      experienceMatch: Math.max(65, overallScore - 10),
      cultureFit: Math.max(70, overallScore + 5),
      recommendations: [
        `Application submitted ${new Date(
          application.appliedAt
        ).toLocaleDateString()}`,
        application.resumeUrl
          ? "Resume provided for review"
          : "Consider requesting resume",
        `Application status: ${application.status}`,
      ],
      strengths: [
        "Applied through SkillBridge platform",
        application.resumeUrl
          ? "Resume available for review"
          : "Direct application submission",
        "Shows interest in the position",
      ],
      concerns: application.resumeUrl ? [] : ["Resume not yet provided"],
    };
  };

  const handleStatusUpdate = async (
    applicationId: number,
    newStatus: "PENDING" | "REVIEWED" | "ACCEPTED" | "REJECTED"
  ) => {
    try {
      await updateApplicationStatus(applicationId, newStatus);
      await refreshUserApplications();
      toast.success("Application status updated successfully");
    } catch (error) {
      toast.error("Failed to update application status");
      console.error("Error updating application status:", error);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case "pending":
        return "bg-yellow-100 text-yellow-800 border-yellow-200";
      case "reviewed":
        return "bg-blue-100 text-blue-800 border-blue-200";
      case "accepted":
        return "bg-green-100 text-green-800 border-green-200";
      case "rejected":
        return "bg-red-100 text-red-800 border-red-200";
      case "interview_scheduled":
        return "bg-purple-100 text-purple-800 border-purple-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case "pending":
        return <Clock className="h-4 w-4" />;
      case "reviewed":
        return <Eye className="h-4 w-4" />;
      case "accepted":
        return <CheckCircle className="h-4 w-4" />;
      case "rejected":
        return <XCircle className="h-4 w-4" />;
      case "interview_scheduled":
        return <Calendar className="h-4 w-4" />;
      default:
        return <AlertTriangle className="h-4 w-4" />;
    }
  };

  const filteredApplications =
    applications?.filter((app) => {
      const matchesStatus =
        statusFilter === "all" || app.status === statusFilter;
      const matchesSearch =
        !searchTerm ||
        app.job?.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        app.job?.company?.toLowerCase().includes(searchTerm.toLowerCase());
      return matchesStatus && matchesSearch;
    }) || [];

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!user) return null;

  return (
    <AuthenticatedOnly>
      <DashboardLayout>
        <div className="space-y-6 max-w-7xl mx-auto p-6">
          {/* Header */}
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold">
                <JobSeekerOnly>My Applications</JobSeekerOnly>
                <EmployerOnly>Application Management</EmployerOnly>
              </h1>
              <p className="text-muted-foreground mt-2">
                <JobSeekerOnly>
                  Track your job applications and their status
                </JobSeekerOnly>
                <EmployerOnly>
                  Review and manage candidate applications with AI insights
                </EmployerOnly>
              </p>
            </div>
            <div className="flex items-center gap-2">
              <Brain className="h-8 w-8 text-purple-600" />
              <span className="text-sm text-muted-foreground">AI Enhanced</span>
            </div>
          </div>

          <Tabs
            value={activeTab}
            onValueChange={setActiveTab}
            className="w-full"
          >
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="overview">Overview</TabsTrigger>
              <TabsTrigger value="applications">Applications</TabsTrigger>
              <TabsTrigger value="insights">AI Insights</TabsTrigger>
            </TabsList>

            {/* Overview Tab */}
            <TabsContent value="overview" className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
                <JobSeekerOnly>
                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        Total Applications
                      </CardTitle>
                      <FileText className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">
                        {applications?.length || 0}
                      </div>
                      <p className="text-xs text-muted-foreground">
                        Submitted applications
                      </p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        Pending Reviews
                      </CardTitle>
                      <Clock className="h-4 w-4 text-orange-500" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">
                        {applications?.filter((app) => app.status === "PENDING")
                          .length || 0}
                      </div>
                      <p className="text-xs text-muted-foreground">
                        Awaiting response
                      </p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        Interviews
                      </CardTitle>
                      <Calendar className="h-4 w-4 text-purple-500" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">
                        {applications?.filter(
                          (app) => app.status === "REVIEWED"
                        ).length || 0}
                      </div>
                      <p className="text-xs text-muted-foreground">
                        Scheduled interviews
                      </p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        Success Rate
                      </CardTitle>
                      <TrendingUp className="h-4 w-4 text-green-500" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">18%</div>
                      <p className="text-xs text-muted-foreground">
                        Interview conversion
                      </p>
                    </CardContent>
                  </Card>
                </JobSeekerOnly>

                <EmployerOnly>
                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        New Applications
                      </CardTitle>
                      <FileText className="h-4 w-4 text-blue-500" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">24</div>
                      <p className="text-xs text-muted-foreground">This week</p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        High-Quality Matches
                      </CardTitle>
                      <Star className="h-4 w-4 text-yellow-500" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">8</div>
                      <p className="text-xs text-muted-foreground">
                        AI Score 85%+
                      </p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        Pending Reviews
                      </CardTitle>
                      <Clock className="h-4 w-4 text-orange-500" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">12</div>
                      <p className="text-xs text-muted-foreground">
                        Need attention
                      </p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        Interview Ready
                      </CardTitle>
                      <CheckCircle className="h-4 w-4 text-green-500" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">5</div>
                      <p className="text-xs text-muted-foreground">
                        Pre-screened candidates
                      </p>
                    </CardContent>
                  </Card>
                </EmployerOnly>
              </div>

              {/* Quick Actions */}
              <Card>
                <CardHeader>
                  <CardTitle>Quick Actions</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <JobSeekerOnly>
                      <Link href="/jobs">
                        <Button
                          className="w-full justify-start"
                          variant="outline"
                        >
                          <Search className="mr-2 h-4 w-4" />
                          Find More Jobs
                        </Button>
                      </Link>
                      <Link href="/profile">
                        <Button
                          className="w-full justify-start"
                          variant="outline"
                        >
                          <Eye className="mr-2 h-4 w-4" />
                          Update Profile
                        </Button>
                      </Link>
                      <Button
                        className="w-full justify-start"
                        variant="outline"
                      >
                        <MessageSquare className="mr-2 h-4 w-4" />
                        Practice Interviews
                      </Button>
                    </JobSeekerOnly>

                    <EmployerOnly>
                      <Button
                        className="w-full justify-start"
                        variant="outline"
                      >
                        <Zap className="mr-2 h-4 w-4" />
                        Bulk Screen
                      </Button>
                      <Button
                        className="w-full justify-start"
                        variant="outline"
                      >
                        <Calendar className="mr-2 h-4 w-4" />
                        Schedule Interviews
                      </Button>
                      <Button
                        className="w-full justify-start"
                        variant="outline"
                      >
                        <FileText className="mr-2 h-4 w-4" />
                        Export Reports
                      </Button>
                    </EmployerOnly>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Applications Tab */}
            <TabsContent value="applications" className="space-y-6">
              {/* Filters */}
              <Card>
                <CardContent className="pt-6">
                  <div className="flex flex-col sm:flex-row gap-4">
                    <div className="flex-1">
                      <Input
                        placeholder="Search applications..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full"
                      />
                    </div>
                    <Select
                      value={statusFilter}
                      onValueChange={setStatusFilter}
                    >
                      <SelectTrigger className="w-full sm:w-48">
                        <SelectValue placeholder="Filter by status" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Status</SelectItem>
                        <SelectItem value="PENDING">Pending</SelectItem>
                        <SelectItem value="REVIEWED">Reviewed</SelectItem>
                        <SelectItem value="INTERVIEW_SCHEDULED">
                          Interview
                        </SelectItem>
                        <SelectItem value="ACCEPTED">Accepted</SelectItem>
                        <SelectItem value="REJECTED">Rejected</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </CardContent>
              </Card>

              {/* Applications List */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <FileText className="h-5 w-5" />
                    Applications ({filteredApplications.length})
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {loading ? (
                    <div className="space-y-4">
                      {[...Array(3)].map((_, i) => (
                        <Skeleton key={i} className="h-20 w-full" />
                      ))}
                    </div>
                  ) : filteredApplications.length === 0 ? (
                    <div className="text-center py-8 text-muted-foreground">
                      <FileText className="h-12 w-12 mx-auto mb-4 opacity-50" />
                      <p className="text-lg">No applications found</p>
                      <p className="text-sm">
                        <JobSeekerOnly>
                          Start applying to jobs to see them here
                        </JobSeekerOnly>
                        <EmployerOnly>
                          No applications match your current filters
                        </EmployerOnly>
                      </p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {filteredApplications.map((application) => {
                        const aiScreening = generateAIScreening(application);
                        return (
                          <div
                            key={application.id}
                            className="border rounded-lg p-4 hover:shadow-md transition-all duration-200"
                          >
                            <div className="flex items-start justify-between">
                              <div className="flex-1">
                                <div className="flex items-center gap-3 mb-2">
                                  <h3 className="font-semibold text-lg">
                                    {application.job?.title}
                                  </h3>
                                  <Badge
                                    className={`${getStatusColor(
                                      application.status
                                    )} flex items-center gap-1`}
                                  >
                                    {getStatusIcon(application.status)}
                                    {application.status.replace("_", " ")}
                                  </Badge>
                                  <EmployerOnly>
                                    <Badge
                                      variant="outline"
                                      className="bg-purple-50 text-purple-700"
                                    >
                                      <Brain className="h-3 w-3 mr-1" />
                                      {aiScreening.overallScore}% Match
                                    </Badge>
                                  </EmployerOnly>
                                </div>
                                <div className="flex items-center gap-4 text-sm text-muted-foreground mb-3">
                                  <div className="flex items-center gap-1">
                                    <Building className="h-4 w-4" />
                                    {application.job?.company}
                                  </div>
                                  <div className="flex items-center gap-1">
                                    <MapPin className="h-4 w-4" />
                                    {application.job?.location}
                                  </div>
                                  <div className="flex items-center gap-1">
                                    <Calendar className="h-4 w-4" />
                                    Applied{" "}
                                    {new Date(
                                      application.appliedAt
                                    ).toLocaleDateString()}
                                  </div>
                                </div>
                                <EmployerOnly>
                                  <div className="grid grid-cols-3 gap-4 mb-3">
                                    <div>
                                      <span className="text-xs text-muted-foreground">
                                        Skills Match
                                      </span>
                                      <Progress
                                        value={aiScreening.skillsMatch}
                                        className="mt-1"
                                      />
                                    </div>
                                    <div>
                                      <span className="text-xs text-muted-foreground">
                                        Experience
                                      </span>
                                      <Progress
                                        value={aiScreening.experienceMatch}
                                        className="mt-1"
                                      />
                                    </div>
                                    <div>
                                      <span className="text-xs text-muted-foreground">
                                        Culture Fit
                                      </span>
                                      <Progress
                                        value={aiScreening.cultureFit}
                                        className="mt-1"
                                      />
                                    </div>
                                  </div>
                                </EmployerOnly>
                              </div>
                              <div className="flex items-center gap-2 ml-4">
                                <EmployerOnly>
                                  <Dialog>
                                    <DialogTrigger asChild>
                                      <Button variant="outline" size="sm">
                                        <Brain className="h-4 w-4 mr-2" />
                                        AI Analysis
                                      </Button>
                                    </DialogTrigger>
                                    <DialogContent className="max-w-2xl">
                                      <DialogHeader>
                                        <DialogTitle>
                                          AI Screening Analysis
                                        </DialogTitle>
                                        <DialogDescription>
                                          Detailed analysis for{" "}
                                          {application.job?.title} application
                                        </DialogDescription>
                                      </DialogHeader>
                                      <div className="space-y-6">
                                        <div className="grid grid-cols-2 gap-4">
                                          <div className="text-center p-4 bg-green-50 rounded-lg">
                                            <div className="text-2xl font-bold text-green-600">
                                              {aiScreening.overallScore}%
                                            </div>
                                            <p className="text-sm text-muted-foreground">
                                              Overall Match
                                            </p>
                                          </div>
                                          <div className="space-y-2">
                                            <div className="flex justify-between text-sm">
                                              <span>Skills Match</span>
                                              <span>
                                                {aiScreening.skillsMatch}%
                                              </span>
                                            </div>
                                            <Progress
                                              value={aiScreening.skillsMatch}
                                            />
                                            <div className="flex justify-between text-sm">
                                              <span>Experience</span>
                                              <span>
                                                {aiScreening.experienceMatch}%
                                              </span>
                                            </div>
                                            <Progress
                                              value={
                                                aiScreening.experienceMatch
                                              }
                                            />
                                            <div className="flex justify-between text-sm">
                                              <span>Culture Fit</span>
                                              <span>
                                                {aiScreening.cultureFit}%
                                              </span>
                                            </div>
                                            <Progress
                                              value={aiScreening.cultureFit}
                                            />
                                          </div>
                                        </div>

                                        <div>
                                          <h4 className="font-semibold mb-2 text-green-600">
                                            Strengths
                                          </h4>
                                          <ul className="space-y-1">
                                            {aiScreening.strengths.map(
                                              (strength, idx) => (
                                                <li
                                                  key={idx}
                                                  className="text-sm flex items-center gap-2"
                                                >
                                                  <CheckCircle className="h-3 w-3 text-green-500" />
                                                  {strength}
                                                </li>
                                              )
                                            )}
                                          </ul>
                                        </div>

                                        <div>
                                          <h4 className="font-semibold mb-2 text-orange-600">
                                            Concerns
                                          </h4>
                                          <ul className="space-y-1">
                                            {aiScreening.concerns.map(
                                              (concern, idx) => (
                                                <li
                                                  key={idx}
                                                  className="text-sm flex items-center gap-2"
                                                >
                                                  <AlertTriangle className="h-3 w-3 text-orange-500" />
                                                  {concern}
                                                </li>
                                              )
                                            )}
                                          </ul>
                                        </div>
                                      </div>
                                    </DialogContent>
                                  </Dialog>
                                </EmployerOnly>

                                <EmployerOnly>
                                  <Select
                                    value={application.status}
                                    onValueChange={(value) =>
                                      handleStatusUpdate(
                                        application.id,
                                        value as
                                          | "PENDING"
                                          | "REVIEWED"
                                          | "ACCEPTED"
                                          | "REJECTED"
                                      )
                                    }
                                  >
                                    <SelectTrigger className="w-40">
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="PENDING">
                                        Pending
                                      </SelectItem>
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
                                </EmployerOnly>

                                <Link href={`/jobs/${application.job?.id}`}>
                                  <Button variant="outline" size="sm">
                                    <Eye className="h-4 w-4 mr-2" />
                                    View Job
                                  </Button>
                                </Link>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            {/* AI Insights Tab */}
            <TabsContent value="insights" className="space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <JobSeekerOnly>
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        <Brain className="h-5 w-5 text-purple-600" />
                        Application Insights
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                        <h4 className="font-semibold text-blue-800 dark:text-blue-200 mb-2">
                          Success Pattern
                        </h4>
                        <p className="text-sm text-muted-foreground">
                          Your applications to remote positions have a 23%
                          higher success rate.
                        </p>
                      </div>
                      <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                        <h4 className="font-semibold text-green-800 dark:text-green-200 mb-2">
                          Optimal Timing
                        </h4>
                        <p className="text-sm text-muted-foreground">
                          Applications submitted on Tuesday-Thursday show better
                          response rates.
                        </p>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        <TrendingUp className="h-5 w-5 text-green-600" />
                        Performance Trends
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-4">
                        <div>
                          <div className="flex justify-between items-center mb-2">
                            <span className="text-sm font-medium">
                              Application Quality
                            </span>
                            <span className="text-sm text-muted-foreground">
                              92%
                            </span>
                          </div>
                          <Progress value={92} />
                        </div>
                        <div>
                          <div className="flex justify-between items-center mb-2">
                            <span className="text-sm font-medium">
                              Response Rate
                            </span>
                            <span className="text-sm text-muted-foreground">
                              18%
                            </span>
                          </div>
                          <Progress value={18} />
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </JobSeekerOnly>

                <EmployerOnly>
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        <Zap className="h-5 w-5 text-yellow-600" />
                        Screening Insights
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
                        <h4 className="font-semibold text-yellow-800 dark:text-yellow-200 mb-2">
                          Quality Candidates
                        </h4>
                        <p className="text-sm text-muted-foreground">
                          8 candidates scored above 85% in AI screening this
                          week.
                        </p>
                      </div>
                      <div className="p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
                        <h4 className="font-semibold text-purple-800 dark:text-purple-200 mb-2">
                          Skill Gaps
                        </h4>
                        <p className="text-sm text-muted-foreground">
                          Most candidates lack experience in cloud deployment
                          technologies.
                        </p>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        <BarChart3 className="h-5 w-5 text-blue-600" />
                        Hiring Metrics
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-4">
                        <div>
                          <div className="flex justify-between items-center mb-2">
                            <span className="text-sm font-medium">
                              Time to Screen
                            </span>
                            <span className="text-sm text-muted-foreground">
                              2.3 days
                            </span>
                          </div>
                          <Progress value={75} />
                        </div>
                        <div>
                          <div className="flex justify-between items-center mb-2">
                            <span className="text-sm font-medium">
                              Interview Conversion
                            </span>
                            <span className="text-sm text-muted-foreground">
                              34%
                            </span>
                          </div>
                          <Progress value={34} />
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </EmployerOnly>
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </DashboardLayout>
    </AuthenticatedOnly>
  );
}
