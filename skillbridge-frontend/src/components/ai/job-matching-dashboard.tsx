"use client";

import React, { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Search,
  Users,
  Briefcase,
  Star,
  RefreshCw,
  TrendingUp,
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
import { useJobsStore } from "@/stores/jobs-store";

export function JobMatchingDashboard() {
  const {
    jobMatches,
    candidateMatches,
    setJobMatches,
    setCandidateMatches,
    setLoadingMatches,
    isLoadingMatches,
  } = useAiStore();

  const { jobs } = useJobsStore();

  const [selectedJobId, setSelectedJobId] = useState<string>("");
  const [selectedUserId, setSelectedUserId] = useState<string>("");
  const [matchLimit, setMatchLimit] = useState(10);

  const findCandidateMatches = async (jobId?: string) => {
    const targetJobId = jobId || selectedJobId;
    if (!targetJobId) {
      toast.error("Please select a job first");
      return;
    }

    setLoadingMatches(true);
    try {
      const response = await apiClient.findMatchingCandidates(
        parseInt(targetJobId),
        matchLimit
      );

      if (response.success && response.data) {
        setCandidateMatches(response.data.candidates || []);
        toast.success(
          `Found ${response.data.candidates?.length || 0} candidate matches`
        );
      }
    } catch (error) {
      console.error("Error finding candidate matches:", error);
      toast.error("Failed to find candidate matches");
    } finally {
      setLoadingMatches(false);
    }
  };

  const findJobMatches = async (userId?: string) => {
    const targetUserId = userId || selectedUserId;
    if (!targetUserId) {
      toast.error("Please enter a user ID first");
      return;
    }

    setLoadingMatches(true);
    try {
      const response = await apiClient.findMatchingJobs(
        parseInt(targetUserId),
        matchLimit
      );

      if (response.success && response.data) {
        setJobMatches(response.data.jobs || []);
        toast.success(`Found ${response.data.jobs?.length || 0} job matches`);
      }
    } catch (error) {
      console.error("Error finding job matches:", error);
      toast.error("Failed to find job matches");
    } finally {
      setLoadingMatches(false);
    }
  };

  const getMatchColor = (score: number) => {
    if (score >= 90) return "text-green-600 bg-green-50";
    if (score >= 80) return "text-green-600 bg-green-50";
    if (score >= 70) return "text-yellow-600 bg-yellow-50";
    if (score >= 60) return "text-orange-600 bg-orange-50";
    return "text-red-600 bg-red-50";
  };

  const getMatchLabel = (score: number) => {
    if (score >= 90) return "Excellent Match";
    if (score >= 80) return "Great Match";
    if (score >= 70) return "Good Match";
    if (score >= 60) return "Fair Match";
    return "Poor Match";
  };

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <div className="text-center">
        <h1 className="text-3xl font-bold tracking-tight">
          AI Job Matching Dashboard
        </h1>
        <p className="text-muted-foreground mt-2">
          Find the best matches between jobs and candidates using AI analysis
        </p>
      </div>

      <Tabs defaultValue="candidates" className="w-full">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="candidates" className="flex items-center gap-2">
            <Users className="h-4 w-4" />
            Find Candidates for Jobs
          </TabsTrigger>
          <TabsTrigger value="jobs" className="flex items-center gap-2">
            <Briefcase className="h-4 w-4" />
            Find Jobs for Candidates
          </TabsTrigger>
        </TabsList>

        {/* Candidate Matching Tab */}
        <TabsContent value="candidates" className="space-y-6">
          {/* Job Selection & Controls */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Search className="h-5 w-5" />
                Find Candidates for Job
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="text-sm font-medium mb-2 block">
                    Select Job
                  </label>
                  <Select
                    value={selectedJobId}
                    onValueChange={setSelectedJobId}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Choose a job..." />
                    </SelectTrigger>
                    <SelectContent>
                      {jobs.map((job) => (
                        <SelectItem key={job.id} value={job.id.toString()}>
                          {job.title} - {job.company}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <label className="text-sm font-medium mb-2 block">
                    Match Limit
                  </label>
                  <Select
                    value={matchLimit.toString()}
                    onValueChange={(value) => setMatchLimit(parseInt(value))}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="5">Top 5</SelectItem>
                      <SelectItem value="10">Top 10</SelectItem>
                      <SelectItem value="20">Top 20</SelectItem>
                      <SelectItem value="50">Top 50</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="flex items-end">
                  <Button
                    onClick={() => findCandidateMatches()}
                    disabled={isLoadingMatches || !selectedJobId}
                    className="w-full"
                  >
                    {isLoadingMatches ? (
                      <RefreshCw className="h-4 w-4 animate-spin mr-2" />
                    ) : (
                      <Search className="h-4 w-4 mr-2" />
                    )}
                    Find Candidates
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Candidate Results */}
          {candidateMatches.length > 0 && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h2 className="text-2xl font-bold">Candidate Matches</h2>
                <Badge variant="outline">
                  {candidateMatches.length} candidates found
                </Badge>
              </div>

              <div className="grid gap-4">
                {candidateMatches.map((candidate) => (
                  <Card key={candidate.candidateId} className="overflow-hidden">
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2">
                            <div>
                              <CardTitle className="text-lg">
                                {candidate.name}
                              </CardTitle>
                              <p className="text-sm text-muted-foreground">
                                {candidate.email}
                              </p>
                            </div>
                          </div>

                          <div className="flex items-center gap-4 text-sm">
                            <Badge
                              className={getMatchColor(
                                candidate.compatibilityScore
                              )}
                            >
                              <Star className="h-3 w-3 mr-1" />
                              {candidate.compatibilityScore}% -{" "}
                              {getMatchLabel(candidate.compatibilityScore)}
                            </Badge>
                          </div>
                        </div>
                      </div>
                    </CardHeader>

                    <CardContent className="pt-0">
                      <div className="space-y-4">
                        {/* Match Breakdown */}
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                          <div>
                            <div className="flex justify-between text-sm mb-1">
                              <span>Skills Match</span>
                              <span>{candidate.skillsMatch}%</span>
                            </div>
                            <Progress
                              value={candidate.skillsMatch}
                              className="h-2"
                            />
                          </div>

                          <div>
                            <div className="flex justify-between text-sm mb-1">
                              <span>Experience Match</span>
                              <span>{candidate.experienceMatch}%</span>
                            </div>
                            <Progress
                              value={candidate.experienceMatch}
                              className="h-2"
                            />
                          </div>

                          <div>
                            <div className="flex justify-between text-sm mb-1">
                              <span>Overall Compatibility</span>
                              <span>{candidate.compatibilityScore}%</span>
                            </div>
                            <Progress
                              value={candidate.compatibilityScore}
                              className="h-2"
                            />
                          </div>
                        </div>

                        {/* Match Reasons */}
                        {candidate.reasons && candidate.reasons.length > 0 && (
                          <div>
                            <h4 className="text-sm font-medium mb-2">
                              Why this is a good match:
                            </h4>
                            <ul className="text-sm space-y-1">
                              {candidate.reasons.map((reason, index) => (
                                <li
                                  key={index}
                                  className="flex items-start gap-2"
                                >
                                  <TrendingUp className="h-3 w-3 text-green-600 mt-0.5 flex-shrink-0" />
                                  {reason}
                                </li>
                              ))}
                            </ul>
                          </div>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          )}
        </TabsContent>

        {/* Job Matching Tab */}
        <TabsContent value="jobs" className="space-y-6">
          {/* User Selection & Controls */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Search className="h-5 w-5" />
                Find Jobs for Candidate
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="text-sm font-medium mb-2 block">
                    User ID
                  </label>
                  <Input
                    type="number"
                    placeholder="Enter candidate user ID"
                    value={selectedUserId}
                    onChange={(e) => setSelectedUserId(e.target.value)}
                  />
                </div>

                <div>
                  <label className="text-sm font-medium mb-2 block">
                    Match Limit
                  </label>
                  <Select
                    value={matchLimit.toString()}
                    onValueChange={(value) => setMatchLimit(parseInt(value))}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="5">Top 5</SelectItem>
                      <SelectItem value="10">Top 10</SelectItem>
                      <SelectItem value="20">Top 20</SelectItem>
                      <SelectItem value="50">Top 50</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="flex items-end">
                  <Button
                    onClick={() => findJobMatches()}
                    disabled={isLoadingMatches || !selectedUserId}
                    className="w-full"
                  >
                    {isLoadingMatches ? (
                      <RefreshCw className="h-4 w-4 animate-spin mr-2" />
                    ) : (
                      <Search className="h-4 w-4 mr-2" />
                    )}
                    Find Jobs
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Job Results */}
          {jobMatches.length > 0 && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h2 className="text-2xl font-bold">Job Matches</h2>
                <Badge variant="outline">{jobMatches.length} jobs found</Badge>
              </div>

              <div className="grid gap-4">
                {jobMatches.map((job) => (
                  <Card key={job.jobId} className="overflow-hidden">
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2">
                            <div>
                              <CardTitle className="text-lg">
                                {job.jobTitle}
                              </CardTitle>
                              <p className="text-sm text-muted-foreground">
                                {job.company}
                              </p>
                            </div>
                          </div>

                          <div className="flex items-center gap-4 text-sm">
                            <Badge
                              className={getMatchColor(job.compatibilityScore)}
                            >
                              <Star className="h-3 w-3 mr-1" />
                              {job.compatibilityScore}% -{" "}
                              {getMatchLabel(job.compatibilityScore)}
                            </Badge>
                          </div>
                        </div>
                      </div>
                    </CardHeader>

                    <CardContent className="pt-0">
                      <div className="space-y-4">
                        {/* Match Breakdown */}
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                          <div>
                            <div className="flex justify-between text-sm mb-1">
                              <span>Skills</span>
                              <span>{job.skillsMatch}%</span>
                            </div>
                            <Progress value={job.skillsMatch} className="h-2" />
                          </div>

                          <div>
                            <div className="flex justify-between text-sm mb-1">
                              <span>Experience</span>
                              <span>{job.experienceMatch}%</span>
                            </div>
                            <Progress
                              value={job.experienceMatch}
                              className="h-2"
                            />
                          </div>

                          <div>
                            <div className="flex justify-between text-sm mb-1">
                              <span>Location</span>
                              <span>{job.locationMatch}%</span>
                            </div>
                            <Progress
                              value={job.locationMatch}
                              className="h-2"
                            />
                          </div>

                          <div>
                            <div className="flex justify-between text-sm mb-1">
                              <span>Salary</span>
                              <span>{job.salaryMatch}%</span>
                            </div>
                            <Progress value={job.salaryMatch} className="h-2" />
                          </div>
                        </div>

                        {/* Match Reasons */}
                        {job.reasons && job.reasons.length > 0 && (
                          <div>
                            <h4 className="text-sm font-medium mb-2">
                              Why this is a good match:
                            </h4>
                            <ul className="text-sm space-y-1">
                              {job.reasons.map((reason, index) => (
                                <li
                                  key={index}
                                  className="flex items-start gap-2"
                                >
                                  <TrendingUp className="h-3 w-3 text-green-600 mt-0.5 flex-shrink-0" />
                                  {reason}
                                </li>
                              ))}
                            </ul>
                          </div>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
