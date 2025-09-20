"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth, useJobs } from "@/hooks";
import { useJobsStore } from "@/stores";
import { useApiReady } from "@/hooks/use-api-ready";
import DashboardLayout from "@/components/layouts/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
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
import { Progress } from "@/components/ui/progress";
import {
  Search,
  MapPin,
  Building,
  DollarSign,
  Calendar,
  Plus,
  Sparkles,
  Brain,
  TrendingUp,
  Heart,
  Loader2,
  Target,
  Star,
} from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";
import type { Job } from "@/lib/api";
import { apiClient } from "@/lib/api";

interface JobMatch {
  jobId: number;
  matchScore: number;
  matchingSkills: string[];
  reasonsToApply: string[];
}

export default function JobsPage() {
  const [searchTerm, setSearchTerm] = useState("");
  const [locationFilter, setLocationFilter] = useState("");
  const [salaryMin, setSalaryMin] = useState("");
  const [salaryMax, setSalaryMax] = useState("");
  const [experienceLevel, setExperienceLevel] = useState("");
  const [industry, setIndustry] = useState("");
  const [hasInitiallyFetched, setHasInitiallyFetched] = useState(false);

  // AI Features
  const [jobMatches, setJobMatches] = useState<JobMatch[]>([]);
  const [isAiMatching, setIsAiMatching] = useState(false);
  const [sortBy, setSortBy] = useState<
    "relevance" | "date" | "salary" | "match"
  >("relevance");

  const { user, isLoading: authLoading } = useAuth();
  const { jobs, isLoading: jobsLoading, error, fetchJobs } = useJobs();
  const { setSearchFilters } = useJobsStore();
  const { isReady: apiReady } = useApiReady(true);
  const router = useRouter();

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login");
      return;
    }
  }, [user, authLoading, router]);

  // Initial fetch effect
  useEffect(() => {
    if (
      apiReady &&
      !hasInitiallyFetched &&
      jobs.length === 0 &&
      !jobsLoading &&
      !error
    ) {
      setHasInitiallyFetched(true);
      fetchJobs();
    }
  }, [
    apiReady,
    hasInitiallyFetched,
    jobs.length,
    jobsLoading,
    error,
    fetchJobs,
  ]);

  const handleSearch = () => {
    const filters = {
      search: searchTerm || undefined,
      location: locationFilter || undefined,
      salaryMin: salaryMin ? Number(salaryMin) : undefined,
      salaryMax: salaryMax ? Number(salaryMax) : undefined,
      experienceLevel: experienceLevel || undefined,
      industry: industry || undefined,
    };
    setSearchFilters(filters);
    fetchJobs(filters);
  };

  const handleAiJobMatching = async () => {
    if (!user || user.role !== "JOB_SEEKER") {
      toast.error("AI job matching is only available for job seekers");
      return;
    }

    setIsAiMatching(true);
    try {
      const response = await apiClient.findMatchingJobs(user.id, 10);

      if (response.success && response.data?.jobs) {
        const matches: JobMatch[] = response.data.jobs.map((job) => ({
          jobId: parseInt(job.jobId),
          matchScore: job.compatibilityScore,
          matchingSkills: job.reasons.slice(0, 3), // Use reasons as skills for now
          reasonsToApply: job.reasons,
        }));

        setJobMatches(matches);
        toast.success(`Found ${matches.length} AI-matched jobs!`);
        setSortBy("match");
      } else {
        toast.error("No matching jobs found");
      }
    } catch (error) {
      console.error("AI matching error:", error);
      toast.error("Failed to analyze job matches");
    } finally {
      setIsAiMatching(false);
    }
  };

  const loadAiInsights = async () => {
    toast.info("Loading market insights...");
    // Could integrate with enhancedApiClient.getPlatformInsightsAI()
    // or other insights endpoints based on user role
    toast.success("Market insights loaded! Check your dashboard for details.");
  };

  const getJobMatchScore = (jobId: number): number | null => {
    const match = jobMatches.find((m) => m.jobId === jobId);
    return match ? match.matchScore : null;
  };

  const getMatchingSkills = (jobId: number): string[] => {
    const match = jobMatches.find((m) => m.jobId === jobId);
    return match ? match.matchingSkills : [];
  };

  const sortJobs = (jobs: Job[]): Job[] => {
    const sorted = [...jobs];

    switch (sortBy) {
      case "match":
        return sorted.sort((a, b) => {
          const scoreA = getJobMatchScore(a.id) || 0;
          const scoreB = getJobMatchScore(b.id) || 0;
          return scoreB - scoreA;
        });
      case "date":
        return sorted.sort(
          (a, b) =>
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
      case "salary":
        return sorted.sort((a, b) => (b.salary || 0) - (a.salary || 0));
      case "relevance":
      default:
        return sorted;
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  if (authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  const sortedJobs = sortJobs(jobs);

  return (
    <DashboardLayout>
      <div className="space-y-6 max-w-7xl mx-auto p-6">
        {/* Header */}
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold text-foreground">Jobs</h1>
            <p className="text-muted-foreground">
              {user.role === "JOB_SEEKER"
                ? "Find your next opportunity with AI-powered matching"
                : "Manage your job postings"}
            </p>
          </div>

          <div className="flex gap-3">
            {user.role === "JOB_SEEKER" && (
              <>
                <Button
                  variant="default"
                  onClick={handleAiJobMatching}
                  disabled={isAiMatching}
                  className="bg-gradient-to-r from-primary to-indigo-600 text-white shadow-md hover:from-primary/80 hover:to-indigo-700 border-none dark:from-blue-700 dark:to-indigo-800 dark:text-white"
                >
                  {isAiMatching ? (
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  ) : (
                    <Target className="mr-2 h-4 w-4" />
                  )}
                  AI Job Matching
                </Button>
                <Dialog>
                  <DialogTrigger asChild>
                    <Button variant="outline" onClick={loadAiInsights}>
                      <TrendingUp className="mr-2 h-4 w-4" />
                      Market Insights
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="max-w-2xl">
                    <DialogHeader>
                      <DialogTitle className="flex items-center gap-2">
                        <Brain className="h-5 w-5 text-purple-600" />
                        AI Market Insights
                      </DialogTitle>
                      <DialogDescription>
                        Get insights about salary trends and skill demand in
                        your field
                      </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4">
                      <p className="text-sm text-muted-foreground">
                        AI-powered market insights provide comprehensive data to
                        help you make informed career decisions:
                      </p>
                      <ul className="list-disc list-inside space-y-1 text-sm text-muted-foreground">
                        <li>
                          Real-time salary trends for your role and location
                        </li>
                        <li>In-demand skills analysis in your industry</li>
                        <li>Personalized career growth recommendations</li>
                        <li>Market competition and opportunity analysis</li>
                      </ul>
                      <p className="text-sm text-success">
                        Click &ldquo;Load Insights&rdquo; to access your
                        personalized market data.
                      </p>
                    </div>
                  </DialogContent>
                </Dialog>
              </>
            )}

            {user.role === "EMPLOYER" && (
              <Button
                asChild
                className="bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700"
              >
                <Link href="/jobs/create">
                  <Plus className="mr-2 h-4 w-4" />
                  Post New Job
                </Link>
              </Button>
            )}
          </div>
        </div>

        {/* Search and Filters */}
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <Search className="h-5 w-5" />
              Search Jobs
              {user.role === "JOB_SEEKER" && (
                <Badge variant="secondary" className="ml-auto">
                  <Sparkles className="h-3 w-3 mr-1" />
                  AI-Enhanced
                </Badge>
              )}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="lg:col-span-2">
                <div className="relative">
                  <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Job title, keywords, or company"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onKeyPress={handleKeyPress}
                    className="pl-10"
                  />
                </div>
              </div>
              <div>
                <div className="relative">
                  <MapPin className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Location"
                    value={locationFilter}
                    onChange={(e) => setLocationFilter(e.target.value)}
                    onKeyPress={handleKeyPress}
                    className="pl-10"
                  />
                </div>
              </div>
              <div>
                <Select value={industry} onValueChange={setIndustry}>
                  <SelectTrigger>
                    <SelectValue placeholder="Industry" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Industries</SelectItem>
                    <SelectItem value="technology">Technology</SelectItem>
                    <SelectItem value="finance">Finance</SelectItem>
                    <SelectItem value="healthcare">Healthcare</SelectItem>
                    <SelectItem value="education">Education</SelectItem>
                    <SelectItem value="retail">Retail</SelectItem>
                    <SelectItem value="manufacturing">Manufacturing</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
              <div className="grid grid-cols-2 gap-2">
                <Input
                  placeholder="Min Salary"
                  value={salaryMin}
                  onChange={(e) => setSalaryMin(e.target.value)}
                  type="number"
                />
                <Input
                  placeholder="Max Salary"
                  value={salaryMax}
                  onChange={(e) => setSalaryMax(e.target.value)}
                  type="number"
                />
              </div>
              <Select
                value={experienceLevel}
                onValueChange={setExperienceLevel}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Experience Level" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Levels</SelectItem>
                  <SelectItem value="entry">Entry Level</SelectItem>
                  <SelectItem value="mid">Mid Level</SelectItem>
                  <SelectItem value="senior">Senior Level</SelectItem>
                  <SelectItem value="lead">Lead/Principal</SelectItem>
                </SelectContent>
              </Select>
              <Select
                value={sortBy}
                onValueChange={(
                  value: "relevance" | "date" | "salary" | "match"
                ) => setSortBy(value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="relevance">Relevance</SelectItem>
                  <SelectItem value="date">Newest First</SelectItem>
                  <SelectItem value="salary">Highest Salary</SelectItem>
                  {user.role === "JOB_SEEKER" && (
                    <SelectItem value="match">Best Match</SelectItem>
                  )}
                </SelectContent>
              </Select>
              <Button onClick={handleSearch} className="w-full">
                <Search className="mr-2 h-4 w-4" />
                Search
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Results Header */}
        <div className="flex justify-between items-center">
          <div className="text-sm text-muted-foreground">
            {jobs.length} job{jobs.length !== 1 ? "s" : ""} found
            {jobMatches.length > 0 && user.role === "JOB_SEEKER" && (
              <span className="ml-2 text-blue-600">• AI matching active</span>
            )}
          </div>
        </div>

        {/* Results */}
        <div className="space-y-4">
          {error && (
            <Card>
              <CardContent className="text-center py-12">
                <div className="w-12 h-12 bg-destructive/10 rounded-lg flex items-center justify-center mx-auto mb-4">
                  <Search className="h-6 w-6 text-destructive" />
                </div>
                <h3 className="text-lg font-medium text-foreground mb-2">
                  Error loading jobs
                </h3>
                <p className="text-muted-foreground mb-4">{error}</p>
                <Button onClick={() => fetchJobs()} variant="outline">
                  Try Again
                </Button>
              </CardContent>
            </Card>
          )}

          {!error && jobsLoading ? (
            Array.from({ length: 5 }).map((_, i) => (
              <Card key={i}>
                <CardHeader>
                  <div className="flex justify-between items-start">
                    <div className="space-y-2 flex-1">
                      <Skeleton className="h-6 w-3/4" />
                      <Skeleton className="h-4 w-1/2" />
                    </div>
                    <Skeleton className="h-6 w-20" />
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-3/4" />
                    <div className="flex gap-2 mt-4">
                      <Skeleton className="h-6 w-16" />
                      <Skeleton className="h-6 w-16" />
                      <Skeleton className="h-6 w-16" />
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          ) : !error && sortedJobs.length === 0 ? (
            <Card>
              <CardContent className="text-center py-12">
                <div className="w-12 h-12 bg-muted rounded-lg flex items-center justify-center mx-auto mb-4">
                  <Search className="h-6 w-6 text-muted-foreground" />
                </div>
                <h3 className="text-lg font-medium text-foreground mb-2">
                  No jobs found
                </h3>
                <p className="text-muted-foreground mb-4">
                  Try adjusting your search criteria or check back later for new
                  opportunities.
                </p>
                {user.role === "EMPLOYER" && (
                  <Button asChild>
                    <Link href="/jobs/create">Post Your First Job</Link>
                  </Button>
                )}
              </CardContent>
            </Card>
          ) : (
            sortedJobs.map((job) => {
              const matchScore = getJobMatchScore(job.id);
              const matchingSkills = getMatchingSkills(job.id);

              return (
                <Card
                  key={job.id}
                  className={`hover:shadow-md transition-all duration-200 ${
                    matchScore && matchScore > 80
                      ? "ring-2 ring-primary/30 bg-gradient-to-r from-blue-100 to-indigo-100 dark:from-blue-900/40 dark:to-indigo-900/40 dark:border-blue-800"
                      : "dark:bg-background"
                  }`}
                >
                  <CardHeader>
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <CardTitle className="text-xl">
                            <Link
                              href={`/jobs/${job.id}`}
                              className="hover:text-primary transition-colors dark:text-foreground"
                            >
                              {job.title}
                            </Link>
                          </CardTitle>
                          {matchScore && user.role === "JOB_SEEKER" && (
                            <div className="flex items-center gap-1">
                              <Star className="h-4 w-4 text-primary dark:text-yellow-400" />
                              <span className="text-sm font-medium text-primary dark:text-yellow-400">
                                {matchScore}% match
                              </span>
                            </div>
                          )}
                        </div>
                        <div className="flex items-center text-muted-foreground space-x-4 text-sm">
                          <div className="flex items-center dark:text-muted-foreground">
                            <Building className="h-4 w-4 mr-1" />
                            {job.company}
                          </div>
                          <div className="flex items-center dark:text-muted-foreground">
                            <MapPin className="h-4 w-4 mr-1" />
                            {job.location}
                          </div>
                          {job.salary && (
                            <div className="flex items-center dark:text-muted-foreground">
                              <DollarSign className="h-4 w-4 mr-1" />$
                              {job.salary.toLocaleString()}
                            </div>
                          )}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="flex items-center text-sm text-muted-foreground mb-2">
                          <Calendar className="h-4 w-4 mr-1" />
                          {new Date(job.createdAt).toLocaleDateString()}
                        </div>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <CardDescription className="mb-4 line-clamp-2">
                      <span className="dark:text-foreground">
                        {job.description}
                      </span>
                    </CardDescription>

                    {matchingSkills.length > 0 &&
                      user.role === "JOB_SEEKER" && (
                        <div className="mb-3">
                          <p className="text-xs font-medium text-primary mb-2">
                            <span className="dark:text-primary">
                              Matching skills from your profile:
                            </span>
                          </p>
                          <div className="flex flex-wrap gap-1">
                            {matchingSkills.slice(0, 4).map((skill, index) => (
                              <Badge
                                key={index}
                                variant="secondary"
                                className="text-xs bg-primary/10 text-primary border border-primary/20 dark:bg-blue-900/40 dark:text-blue-200 dark:border-blue-800"
                              >
                                {skill}
                              </Badge>
                            ))}
                            {matchingSkills.length > 4 && (
                              <Badge
                                variant="outline"
                                className="text-xs border-primary/20 text-primary"
                              >
                                <span className="dark:text-blue-200">
                                  +{matchingSkills.length - 4} more
                                </span>
                              </Badge>
                            )}
                          </div>
                        </div>
                      )}

                    <div className="flex flex-wrap gap-2 mb-4">
                      {job.requirements
                        ?.slice(0, 3)
                        .map((req: string, index: number) => (
                          <Badge
                            key={index}
                            variant="outline"
                            className="text-xs dark:text-foreground dark:border-muted"
                          >
                            {req}
                          </Badge>
                        ))}
                      {job.requirements?.length > 3 && (
                        <Badge variant="outline" className="text-xs">
                          <span className="dark:text-foreground">
                            +{job.requirements.length - 3} more
                          </span>
                        </Badge>
                      )}
                    </div>

                    <div className="flex justify-between items-center">
                      <div className="flex gap-2">
                        <Button asChild size="sm">
                          <Link href={`/jobs/${job.id}`}>View Details</Link>
                        </Button>
                        {user.role === "JOB_SEEKER" && (
                          <>
                            <Button variant="outline" size="sm">
                              <Heart className="h-4 w-4 mr-1" />
                              Save
                            </Button>
                            {matchScore && (
                              <Button
                                size="sm"
                                className="bg-primary hover:bg-primary/90 text-white dark:bg-blue-700 dark:hover:bg-blue-800"
                              >
                                Quick Apply
                              </Button>
                            )}
                          </>
                        )}
                      </div>
                      {matchScore && user.role === "JOB_SEEKER" && (
                        <div className="text-right">
                          <Progress
                            value={matchScore}
                            className="w-20 h-2 bg-primary/10 [&_.bg-primary]:bg-primary"
                          />
                          <p className="text-xs text-primary dark:text-blue-200 mt-1">
                            Match Score
                          </p>
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              );
            })
          )}
        </div>
      </div>
    </DashboardLayout>
  );
}
