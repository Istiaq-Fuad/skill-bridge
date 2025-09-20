"use client";

import React, { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Loader2, Wand2, Copy, Download } from "lucide-react";
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
export function JobDescriptionGenerator() {
  const [formData, setFormData] = useState({
    jobTitle: "",
    company: "",
    industry: "",
    experienceLevel: "",
    location: "",
    employmentType: "FULL_TIME",
    additionalRequirements: "",
  });

  const { setCurrentGeneration, updateJobGeneration, setJobGeneration } =
    useAiStore();

  const [generatedDescription, setGeneratedDescription] = useState("");
  const [suggestedSkills, setSuggestedSkills] = useState<string[]>([]);
  const [suggestedSalary, setSuggestedSalary] = useState<{
    min: number;
    max: number;
  } | null>(null);
  const [isGenerating, setIsGenerating] = useState(false);

  const handleInputChange = (field: string, value: string) => {
    setFormData((prev: typeof formData) => ({ ...prev, [field]: value }));
  };

  const generateJobDescription = async () => {
    if (
      !formData.jobTitle ||
      !formData.company ||
      !formData.experienceLevel ||
      !formData.location
    ) {
      toast.error("Please fill in all required fields");
      return;
    }

    setIsGenerating(true);
    const generationId = `gen_${Date.now()}`;

    try {
      // Create initial generation record
      const generation = {
        id: generationId,
        title: formData.jobTitle,
        industry: formData.industry || "",
        experienceLevel: formData.experienceLevel,
        location: formData.location,
        status: "generating" as const,
      };

      setJobGeneration(generation);
      setCurrentGeneration(generation);

      // Generate job description
      const descResponse = await apiClient.generateJobDescription(formData);

      if (descResponse.success && descResponse.data) {
        setGeneratedDescription(descResponse.data.jobDescription);
        updateJobGeneration(generationId, {
          generatedDescription: descResponse.data.jobDescription,
        });
      }

      // Get skill suggestions
      const skillsResponse = await apiClient.suggestSkills({
        jobTitle: formData.jobTitle,
        industry: formData.industry,
        experienceLevel: formData.experienceLevel,
      });

      if (skillsResponse.success && skillsResponse.data) {
        setSuggestedSkills(skillsResponse.data.skills);
        updateJobGeneration(generationId, {
          suggestedSkills: skillsResponse.data.skills,
        });
      }

      // Get salary suggestions
      const salaryResponse = await apiClient.suggestSalary({
        jobTitle: formData.jobTitle,
        location: formData.location,
        experienceLevel: formData.experienceLevel,
        industry: formData.industry,
      });

      if (salaryResponse.success && salaryResponse.data) {
        const salaryRange = {
          min: salaryResponse.data.minSalary,
          max: salaryResponse.data.maxSalary,
        };
        setSuggestedSalary(salaryRange);
        updateJobGeneration(generationId, {
          salaryRange,
          status: "completed",
        });
      }

      toast.success("Job description generated successfully!");
    } catch (error) {
      console.error("Error generating job description:", error);
      updateJobGeneration(generationId, {
        status: "error",
        error:
          error instanceof Error
            ? error.message
            : "Failed to generate job description",
      });
      toast.error("Failed to generate job description");
    } finally {
      setIsGenerating(false);
    }
  };

  const optimizeDescription = async () => {
    if (!generatedDescription) {
      toast.error("Generate a job description first");
      return;
    }

    setIsGenerating(true);

    try {
      const response = await apiClient.optimizeJobDescription({
        jobDescription: generatedDescription,
        targetAudience: "experienced_professionals",
        optimizationGoals: ["clarity", "engagement", "seo"],
      });

      if (response.success && response.data) {
        setGeneratedDescription(response.data.optimizedJobDescription);
        toast.success("Job description optimized!");
      }
    } catch (error) {
      console.error("Error optimizing description:", error);
      toast.error("Failed to optimize job description");
    } finally {
      setIsGenerating(false);
    }
  };

  const copyToClipboard = async () => {
    if (!generatedDescription) return;

    try {
      await navigator.clipboard.writeText(generatedDescription);
      toast.success("Job description copied to clipboard!");
    } catch {
      toast.error("Failed to copy to clipboard");
    }
  };

  const downloadDescription = () => {
    if (!generatedDescription) return;

    const blob = new Blob([generatedDescription], { type: "text/plain" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `job-description-${formData.jobTitle.replace(
      /\s+/g,
      "-"
    )}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <div className="text-center">
        <h1 className="text-3xl font-bold tracking-tight">
          AI Job Description Generator
        </h1>
        <p className="text-muted-foreground mt-2">
          Create compelling job descriptions with AI-powered insights
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Input Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Wand2 className="h-5 w-5" />
              Job Details
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="jobTitle">Job Title *</Label>
                <Input
                  id="jobTitle"
                  value={formData.jobTitle}
                  onChange={(e) =>
                    handleInputChange("jobTitle", e.target.value)
                  }
                  placeholder="e.g., Senior Frontend Developer"
                />
              </div>
              <div>
                <Label htmlFor="company">Company *</Label>
                <Input
                  id="company"
                  value={formData.company}
                  onChange={(e) => handleInputChange("company", e.target.value)}
                  placeholder="e.g., TechCorp Inc."
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="industry">Industry</Label>
                <Input
                  id="industry"
                  value={formData.industry}
                  onChange={(e) =>
                    handleInputChange("industry", e.target.value)
                  }
                  placeholder="e.g., Technology, Healthcare"
                />
              </div>
              <div>
                <Label htmlFor="location">Location *</Label>
                <Input
                  id="location"
                  value={formData.location}
                  onChange={(e) =>
                    handleInputChange("location", e.target.value)
                  }
                  placeholder="e.g., New York, NY / Remote"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="experienceLevel">Experience Level *</Label>
                <Select
                  value={formData.experienceLevel}
                  onValueChange={(value) =>
                    handleInputChange("experienceLevel", value)
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select experience level" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="entry">
                      Entry Level (0-2 years)
                    </SelectItem>
                    <SelectItem value="mid">Mid Level (3-5 years)</SelectItem>
                    <SelectItem value="senior">
                      Senior Level (6-10 years)
                    </SelectItem>
                    <SelectItem value="lead">
                      Lead/Principal (10+ years)
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="employmentType">Employment Type</Label>
                <Select
                  value={formData.employmentType}
                  onValueChange={(value) =>
                    handleInputChange("employmentType", value)
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="FULL_TIME">Full Time</SelectItem>
                    <SelectItem value="PART_TIME">Part Time</SelectItem>
                    <SelectItem value="CONTRACT">Contract</SelectItem>
                    <SelectItem value="INTERNSHIP">Internship</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div>
              <Label htmlFor="additionalRequirements">
                Additional Requirements
              </Label>
              <Textarea
                id="additionalRequirements"
                value={formData.additionalRequirements}
                onChange={(e) =>
                  handleInputChange("additionalRequirements", e.target.value)
                }
                placeholder="Any specific requirements, certifications, or preferences..."
                rows={3}
              />
            </div>

            <div className="flex gap-2 pt-4">
              <Button
                onClick={generateJobDescription}
                disabled={isGenerating}
                className="flex-1"
              >
                {isGenerating ? (
                  <Loader2 className="h-4 w-4 animate-spin mr-2" />
                ) : (
                  <Wand2 className="h-4 w-4 mr-2" />
                )}
                Generate Description
              </Button>
              <Button
                variant="outline"
                onClick={optimizeDescription}
                disabled={isGenerating || !generatedDescription}
              >
                Optimize
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Generated Content */}
        <Card>
          <CardHeader>
            <CardTitle>Generated Job Description</CardTitle>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={copyToClipboard}
                disabled={!generatedDescription}
              >
                <Copy className="h-4 w-4 mr-2" />
                Copy
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={downloadDescription}
                disabled={!generatedDescription}
              >
                <Download className="h-4 w-4 mr-2" />
                Download
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {generatedDescription ? (
              <div className="space-y-4">
                <div className="bg-muted/50 p-4 rounded-lg">
                  <pre className="whitespace-pre-wrap text-sm font-mono">
                    {generatedDescription}
                  </pre>
                </div>

                {suggestedSkills.length > 0 && (
                  <div>
                    <h4 className="font-medium mb-2">Suggested Skills:</h4>
                    <div className="flex flex-wrap gap-2">
                      {suggestedSkills.map((skill, index) => (
                        <Badge key={index} variant="secondary">
                          {skill}
                        </Badge>
                      ))}
                    </div>
                  </div>
                )}

                {suggestedSalary && (
                  <div>
                    <h4 className="font-medium mb-2">
                      Suggested Salary Range:
                    </h4>
                    <Badge variant="outline">
                      ${suggestedSalary.min.toLocaleString()} - $
                      {suggestedSalary.max.toLocaleString()}
                    </Badge>
                  </div>
                )}
              </div>
            ) : (
              <div className="text-center py-12 text-muted-foreground">
                <Wand2 className="h-12 w-12 mx-auto mb-4 opacity-50" />
                <p>
                  Fill in the job details and click &quot;Generate
                  Description&quot; to get started
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
