"use client";

import React, { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  LineChart,
  Line,
  ResponsiveContainer,
} from "recharts";
import {
  TrendingUp,
  Users,
  Briefcase,
  FileText,
  AlertTriangle,
  CheckCircle,
  XCircle,
  RefreshCw,
  Brain,
  Shield,
  Activity,
  Zap,
} from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";
import { apiClient } from "@/lib/api";
import { useAiStore } from "@/stores/ai-store";

interface HealthMetric {
  name: string;
  status: "healthy" | "warning" | "critical";
  value: number;
  threshold: number;
  description: string;
}

interface InsightRecommendation {
  category: string;
  priority: "high" | "medium" | "low";
  title: string;
  description: string;
  action: string;
}

export function EnhancedAdminDashboard() {
  const { analyticsDashboard, setAnalyticsDashboard } = useAiStore();

  const [isLoading, setIsLoading] = useState(false);
  const [selectedPeriod, setSelectedPeriod] = useState("30");
  const [healthMetrics, setHealthMetrics] = useState<HealthMetric[]>([]);
  const [insights, setInsights] = useState<InsightRecommendation[]>([]);
  const [strategicRecommendations, setStrategicRecommendations] = useState<
    string[]
  >([]);

  useEffect(() => {
    loadAnalyticsData();
  }, [selectedPeriod]); // eslint-disable-line react-hooks/exhaustive-deps

  const loadAnalyticsData = async () => {
    setIsLoading(true);
    try {
      // Load analytics overview
      const overviewResponse = await apiClient.getAnalyticsOverview();
      if (overviewResponse.success && overviewResponse.data) {
        setAnalyticsDashboard({
          ...analyticsDashboard,
          overview: overviewResponse.data,
        });
      }

      // Load trends data
      const trendsResponse = await apiClient.getAnalyticsTrends(
        parseInt(selectedPeriod)
      );
      if (trendsResponse.success && trendsResponse.data) {
        setAnalyticsDashboard({
          ...analyticsDashboard,
          trends: trendsResponse.data,
        });
      }

      // Load health check
      const healthResponse = await apiClient.getDetailedHealthCheck();
      if (healthResponse.success && healthResponse.data) {
        setHealthMetrics(healthResponse.data.metrics || []);
      }

      // Generate platform insights
      if (overviewResponse.data && trendsResponse.data) {
        const insightsResponse = await apiClient.generatePlatformInsights({
          overview: overviewResponse.data,
          trends: trendsResponse.data,
          period: parseInt(selectedPeriod),
        });

        if (insightsResponse.success && insightsResponse.data) {
          setInsights(insightsResponse.data.recommendations || []);
        }
      }

      // Get strategic recommendations
      const strategicResponse =
        await apiClient.generateStrategicRecommendations();
      if (strategicResponse.success && strategicResponse.data) {
        setStrategicRecommendations(
          strategicResponse.data.recommendations || []
        );
      }

      toast.success("Analytics data loaded successfully");
    } catch (error) {
      console.error("Error loading analytics:", error);
      toast.error("Failed to load analytics data");
    } finally {
      setIsLoading(false);
    }
  };

  const moderateJob = async (jobId: number) => {
    try {
      const response = await apiClient.moderateJobContent(jobId);
      if (response.success) {
        toast.success("Job content moderated successfully");
      }
    } catch (error) {
      console.error("Error moderating job:", error);
      toast.error("Failed to moderate job content");
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "healthy":
        return "text-green-600 bg-green-50";
      case "warning":
        return "text-yellow-600 bg-yellow-50";
      case "critical":
        return "text-red-600 bg-red-50";
      default:
        return "text-gray-600 bg-gray-50";
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case "high":
        return "text-red-600 bg-red-50 border-red-200";
      case "medium":
        return "text-yellow-600 bg-yellow-50 border-yellow-200";
      case "low":
        return "text-green-600 bg-green-50 border-green-200";
      default:
        return "text-gray-600 bg-gray-50 border-gray-200";
    }
  };

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            AI-Enhanced Admin Dashboard
          </h1>
          <p className="text-muted-foreground">
            Advanced analytics and insights powered by AI
          </p>
        </div>

        <div className="flex items-center gap-4">
          <Select value={selectedPeriod} onValueChange={setSelectedPeriod}>
            <SelectTrigger className="w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">Last 7 days</SelectItem>
              <SelectItem value="30">Last 30 days</SelectItem>
              <SelectItem value="90">Last 90 days</SelectItem>
              <SelectItem value="365">Last year</SelectItem>
            </SelectContent>
          </Select>

          <Button onClick={loadAnalyticsData} disabled={isLoading}>
            {isLoading ? (
              <RefreshCw className="h-4 w-4 animate-spin mr-2" />
            ) : (
              <RefreshCw className="h-4 w-4 mr-2" />
            )}
            Refresh
          </Button>
        </div>
      </div>

      {/* Overview Cards */}
      {analyticsDashboard.overview && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">
                    Total Users
                  </p>
                  <p className="text-2xl font-bold">
                    {analyticsDashboard.overview.totalUsers.toLocaleString()}
                  </p>
                </div>
                <Users className="h-8 w-8 text-blue-600" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">
                    Active Jobs
                  </p>
                  <p className="text-2xl font-bold">
                    {analyticsDashboard.overview.totalJobs.toLocaleString()}
                  </p>
                </div>
                <Briefcase className="h-8 w-8 text-green-600" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">
                    Applications
                  </p>
                  <p className="text-2xl font-bold">
                    {analyticsDashboard.overview.totalApplications.toLocaleString()}
                  </p>
                </div>
                <FileText className="h-8 w-8 text-purple-600" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">
                    Success Rate
                  </p>
                  <p className="text-2xl font-bold">
                    {analyticsDashboard.overview.successRate}%
                  </p>
                </div>
                <TrendingUp className="h-8 w-8 text-orange-600" />
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      <Tabs defaultValue="analytics" className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="analytics">
            <BarChart className="h-4 w-4 mr-2" />
            Analytics
          </TabsTrigger>
          <TabsTrigger value="insights">
            <Brain className="h-4 w-4 mr-2" />
            AI Insights
          </TabsTrigger>
          <TabsTrigger value="moderation">
            <Shield className="h-4 w-4 mr-2" />
            Content Moderation
          </TabsTrigger>
          <TabsTrigger value="health">
            <Activity className="h-4 w-4 mr-2" />
            System Health
          </TabsTrigger>
        </TabsList>

        {/* Analytics Tab */}
        <TabsContent value="analytics" className="space-y-6">
          {analyticsDashboard.trends && (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>User Growth Trend</CardTitle>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={300}>
                    <LineChart
                      data={
                        analyticsDashboard.trends.userGrowth?.map(
                          (value, index) => ({
                            name:
                              analyticsDashboard.trends?.labels?.[index] ||
                              `Day ${index + 1}`,
                            users: value,
                          })
                        ) || []
                      }
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="users"
                        stroke="#8884d8"
                        strokeWidth={2}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Job Postings & Applications</CardTitle>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart
                      data={
                        analyticsDashboard.trends.jobPostings?.map(
                          (value, index) => ({
                            name:
                              analyticsDashboard.trends?.labels?.[index] ||
                              `Day ${index + 1}`,
                            jobs: value,
                            applications:
                              analyticsDashboard.trends?.applicationVolume?.[
                                index
                              ] || 0,
                          })
                        ) || []
                      }
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="jobs" fill="#8884d8" />
                      <Bar dataKey="applications" fill="#82ca9d" />
                    </BarChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>
            </div>
          )}
        </TabsContent>

        {/* AI Insights Tab */}
        <TabsContent value="insights" className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Platform Insights */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Brain className="h-5 w-5" />
                  AI-Generated Insights
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {insights.map((insight, index) => (
                    <div
                      key={index}
                      className={`p-4 rounded-lg border ${getPriorityColor(
                        insight.priority
                      )}`}
                    >
                      <div className="flex items-start justify-between mb-2">
                        <Badge className={getPriorityColor(insight.priority)}>
                          {insight.priority.toUpperCase()} PRIORITY
                        </Badge>
                        <Badge variant="outline">{insight.category}</Badge>
                      </div>
                      <h4 className="font-medium mb-1">{insight.title}</h4>
                      <p className="text-sm text-muted-foreground mb-2">
                        {insight.description}
                      </p>
                      <p className="text-sm font-medium">
                        Recommended Action: {insight.action}
                      </p>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Strategic Recommendations */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Zap className="h-5 w-5" />
                  Strategic Recommendations
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {strategicRecommendations.map((recommendation, index) => (
                    <div
                      key={index}
                      className="flex items-start gap-3 p-3 bg-muted/50 rounded-lg"
                    >
                      <TrendingUp className="h-4 w-4 text-blue-600 mt-0.5 flex-shrink-0" />
                      <p className="text-sm">{recommendation}</p>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Content Moderation Tab */}
        <TabsContent value="moderation" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Shield className="h-5 w-5" />
                AI Content Moderation
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <p className="text-sm text-muted-foreground">
                  AI-powered content moderation helps identify and flag
                  inappropriate job postings, resumes, and user-generated
                  content automatically.
                </p>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <Card>
                    <CardContent className="p-4 text-center">
                      <CheckCircle className="h-8 w-8 text-green-600 mx-auto mb-2" />
                      <p className="font-medium">1,234</p>
                      <p className="text-sm text-muted-foreground">
                        Content Approved
                      </p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardContent className="p-4 text-center">
                      <AlertTriangle className="h-8 w-8 text-yellow-600 mx-auto mb-2" />
                      <p className="font-medium">56</p>
                      <p className="text-sm text-muted-foreground">
                        Requires Review
                      </p>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardContent className="p-4 text-center">
                      <XCircle className="h-8 w-8 text-red-600 mx-auto mb-2" />
                      <p className="font-medium">12</p>
                      <p className="text-sm text-muted-foreground">
                        Content Blocked
                      </p>
                    </CardContent>
                  </Card>
                </div>

                <div className="flex gap-2">
                  <Button onClick={() => moderateJob(1)}>
                    Moderate Sample Job
                  </Button>
                  <Button variant="outline">View Flagged Content</Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* System Health Tab */}
        <TabsContent value="health" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Activity className="h-5 w-5" />
                System Health Monitoring
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-6">
                {healthMetrics.map((metric, index) => (
                  <div key={index} className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div
                          className={`w-3 h-3 rounded-full ${
                            metric.status === "healthy"
                              ? "bg-green-500"
                              : metric.status === "warning"
                              ? "bg-yellow-500"
                              : "bg-red-500"
                          }`}
                        />
                        <span className="font-medium">{metric.name}</span>
                      </div>
                      <Badge className={getStatusColor(metric.status)}>
                        {metric.status.toUpperCase()}
                      </Badge>
                    </div>

                    <div>
                      <div className="flex justify-between text-sm mb-1">
                        <span>{metric.description}</span>
                        <span>
                          {metric.value}% / {metric.threshold}%
                        </span>
                      </div>
                      <Progress
                        value={metric.value}
                        className={`h-2 ${
                          metric.value > metric.threshold
                            ? "[&>div]:bg-red-500"
                            : "[&>div]:bg-green-500"
                        }`}
                      />
                    </div>
                  </div>
                ))}

                {healthMetrics.length === 0 && (
                  <div className="text-center py-8 text-muted-foreground">
                    <Activity className="h-12 w-12 mx-auto mb-4 opacity-50" />
                    <p>
                      No health metrics available. Click refresh to load data.
                    </p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
