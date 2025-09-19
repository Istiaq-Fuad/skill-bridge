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
  Search,
  MapPin,
  Building,
  DollarSign,
  Calendar,
  Plus,
} from "lucide-react";

import Link from "next/link";

export default function JobsPage() {
  const [searchTerm, setSearchTerm] = useState("");
  const [locationFilter, setLocationFilter] = useState("");
  const [hasInitiallyFetched, setHasInitiallyFetched] = useState(false);
  const { user, isLoading: authLoading } = useAuth();
  const { jobs, isLoading: jobsLoading, error, fetchJobs } = useJobs();
  const { setSearchFilters } = useJobsStore();
  const { isReady: apiReady } = useApiReady(true); // Require auth for jobs
  const router = useRouter();

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login");
      return;
    }
  }, [user, authLoading, router]);

  // Initial fetch effect - wait for API to be ready
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
    };
    setSearchFilters(filters);
    fetchJobs(filters);
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

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <h1 className="text-2xl font-bold text-foreground">Jobs</h1>
            <p className="text-muted-foreground">
              {user.role === "JOB_SEEKER"
                ? "Find your next opportunity"
                : "Manage your job postings"}
            </p>
          </div>

          {user.role === "EMPLOYER" && (
            <Button asChild>
              <Link href="/jobs/create">
                <Plus className="mr-2 h-4 w-4" />
                Post New Job
              </Link>
            </Button>
          )}
        </div>

        {/* Search Filters */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Search Jobs</CardTitle>
            <CardDescription>
              Find the perfect opportunity for your skills
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="flex-1">
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
              <div className="flex-1">
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
              <Button onClick={handleSearch} className="sm:px-8">
                Search
              </Button>
            </div>
          </CardContent>
        </Card>

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
            // Loading skeletons
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
          ) : !error && jobs.length === 0 ? (
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
            jobs.map((job) => (
              <Card key={job.id} className="hover:shadow-md transition-shadow">
                <CardHeader>
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <CardTitle className="text-xl mb-2">
                        <Link
                          href={`/jobs/${job.id}`}
                          className="hover:text-primary transition-colors"
                        >
                          {job.title}
                        </Link>
                      </CardTitle>
                      <div className="flex items-center text-muted-foreground space-x-4 text-sm">
                        <div className="flex items-center">
                          <Building className="h-4 w-4 mr-1" />
                          {job.company}
                        </div>
                        <div className="flex items-center">
                          <MapPin className="h-4 w-4 mr-1" />
                          {job.location}
                        </div>
                        {job.salary && (
                          <div className="flex items-center">
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
                  <CardDescription className="mb-4 line-clamp-3">
                    {job.description}
                  </CardDescription>

                  <div className="flex flex-wrap gap-2 mb-4">
                    {job.requirements?.slice(0, 3).map((req, index) => (
                      <Badge
                        key={index}
                        variant="secondary"
                        className="text-xs"
                      >
                        {req}
                      </Badge>
                    ))}
                    {job.requirements?.length > 3 && (
                      <Badge variant="outline" className="text-xs">
                        +{job.requirements.length - 3} more
                      </Badge>
                    )}
                  </div>

                  <div className="flex justify-between items-center">
                    <div className="flex gap-2">
                      <Button asChild size="sm">
                        <Link href={`/jobs/${job.id}`}>View Details</Link>
                      </Button>
                      {user.role === "JOB_SEEKER" && (
                        <Button variant="outline" size="sm">
                          Save Job
                        </Button>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>
      </div>
    </DashboardLayout>
  );
}
