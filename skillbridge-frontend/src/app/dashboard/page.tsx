"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth, useEmployerDashboardStats } from "@/hooks";
import DashboardLayout from "@/components/layouts/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Briefcase, Users, Brain, Sparkles, Clock } from "lucide-react";
import { apiClient } from "@/lib/api";

interface JobMatch {
  jobId: number;
  title: string;
  company: string;
  matchScore: number;
}

interface ApiJob {
  jobId: string;
  jobTitle: string;
  company: string;
  compatibilityScore: number;
  skillsMatch: number;
  experienceMatch: number;
  locationMatch: number;
  salaryMatch: number;
  reasons: string[];
}

export default function DashboardPage() {
  const { user, isLoading } = useAuth();
  const router = useRouter();
  const { dashboardStats, isLoadingStats, fetchStats } =
    useEmployerDashboardStats();
  const [activeTab, setActiveTab] = useState("overview");
  const [aiRecommendations, setAiRecommendations] = useState<string>("");
  const [aiJobs, setAiJobs] = useState<JobMatch[]>([]);
  const [aiLoading, setAiLoading] = useState(false);

  useEffect(() => {
    if (!isLoading && !user) {
      router.push("/login");
    }
  }, [user, isLoading, router]);

  useEffect(() => {
    if (user?.role === "EMPLOYER") {
      fetchStats();
      fetchAiRecommendations();
    }
    if (user?.role === "JOB_SEEKER") {
      fetchAiJobs();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user, fetchStats]);

  const fetchAiRecommendations = async () => {
    setAiLoading(true);
    try {
      const res = await apiClient.getEnhancedDashboard();
      if (res.success && res.data?.aiRecommendations) {
        setAiRecommendations(res.data.aiRecommendations);
      }
    } catch {
      setAiRecommendations("");
    } finally {
      setAiLoading(false);
    }
  };

  const fetchAiJobs = async () => {
    if (!user) return;

    setAiLoading(true);
    try {
      const res = await apiClient.findMatchingJobs(user.id);
      if (res.success && res.data?.jobs) {
        // Map the API response to our expected format
        setAiJobs(
          res.data.jobs.map((job: ApiJob) => ({
            jobId: parseInt(job.jobId),
            title: job.jobTitle,
            company: job.company,
            matchScore: job.compatibilityScore,
          }))
        );
      }
    } catch {
      setAiJobs([]);
    } finally {
      setAiLoading(false);
    }
  };

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
      <div className="space-y-6 max-w-7xl mx-auto p-6">
        {/* Welcome Section */}
        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-lg border p-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-foreground mb-2">
                Welcome back, {user.firstName || user.username || "User"}!
              </h1>
              <p className="text-muted-foreground text-lg">
                {user.role === "JOB_SEEKER"
                  ? "Discover opportunities with AI-powered job matching"
                  : user.role === "EMPLOYER"
                  ? "Manage your talent acquisition with intelligent tools"
                  : "Oversee platform operations and analytics"}
              </p>
            </div>
            <div className="flex items-center gap-3">
              <Badge variant="secondary" className="text-sm px-3 py-1">
                {user.role === "JOB_SEEKER"
                  ? "Job Seeker"
                  : user.role === "EMPLOYER"
                  ? "Employer"
                  : "Administrator"}
              </Badge>
              <Sparkles className="h-8 w-8 text-primary" />
            </div>
          </div>
        </div>

        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="ai">AI Features</TabsTrigger>
          </TabsList>

          {/* Overview Tab - Only dynamic stats */}
          <TabsContent value="overview" className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
              {user.role === "EMPLOYER" && (
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">
                      Active Jobs
                    </CardTitle>
                    <Briefcase className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">
                      {isLoadingStats ? "..." : dashboardStats?.totalJobs || 0}
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Published listings
                    </p>
                  </CardContent>
                </Card>
              )}
              {user.role === "EMPLOYER" && (
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">
                      Applications
                    </CardTitle>
                    <Users className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">
                      {isLoadingStats
                        ? "..."
                        : dashboardStats?.totalApplications || 0}
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Total applications received
                    </p>
                  </CardContent>
                </Card>
              )}
              {user.role === "EMPLOYER" && (
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">
                      Pending Reviews
                    </CardTitle>
                    <Clock className="h-4 w-4 text-orange-500" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">
                      {isLoadingStats
                        ? "..."
                        : dashboardStats?.pendingApplications || 0}
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Need your attention
                    </p>
                  </CardContent>
                </Card>
              )}
              {user.role === "JOB_SEEKER" && (
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">
                      AI-Recommended Jobs
                    </CardTitle>
                    <Brain className="h-4 w-4 text-purple-600" />
                  </CardHeader>
                  <CardContent>
                    {aiLoading ? (
                      <div className="text-xs text-muted-foreground">
                        Loading...
                      </div>
                    ) : (
                      <div>
                        {aiJobs.length > 0 ? (
                          <ul className="list-disc ml-4">
                            {aiJobs.map((job) => (
                              <li key={job.jobId}>
                                {job.title} @ {job.company} ({job.matchScore}%
                                match)
                              </li>
                            ))}
                          </ul>
                        ) : (
                          <div className="text-xs text-muted-foreground">
                            No recommendations yet.
                          </div>
                        )}
                      </div>
                    )}
                  </CardContent>
                </Card>
              )}
            </div>
          </TabsContent>

          {/* AI Features Tab - Dynamic AI recommendations and analytics */}
          <TabsContent value="ai" className="space-y-6">
            {user.role === "EMPLOYER" && (
              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">
                    AI Recommendations
                  </CardTitle>
                  <Sparkles className="h-4 w-4 text-yellow-600" />
                </CardHeader>
                <CardContent>
                  {aiLoading ? (
                    <div className="text-xs text-muted-foreground">
                      Loading...
                    </div>
                  ) : (
                    <div>{aiRecommendations || "No recommendations yet."}</div>
                  )}
                </CardContent>
              </Card>
            )}
            {/* Add more dynamic AI features here as needed */}
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
}
