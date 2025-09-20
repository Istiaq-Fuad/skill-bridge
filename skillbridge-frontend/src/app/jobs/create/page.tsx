"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth, useCreateJob } from "@/hooks";
import DashboardLayout from "@/components/layouts/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from "@/components/ui/tabs";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  ArrowLeft,
  Building,
  MapPin,
  DollarSign,
  FileText,
  Plus,
  X,
  Loader2,
  Sparkles,
  Brain,
} from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";
import { CanCreateJobs } from "@/components/auth/RoleBasedUI";
import { apiClient } from "@/lib/api";

interface JobFormData {
  title: string;
  description: string;
  company: string;
  location: string;
  salary: string;
  requirements: string[];
  employmentType: string;
  industry: string;
  experienceLevel: string;
}

interface AiGenerationData {
  description: string;
  suggestedSkills: string[];
  salaryRange?: { min: number; max: number };
}

export default function CreateJobPage() {
  const [formData, setFormData] = useState<JobFormData>({
    title: "",
    description: "",
    company: "",
    location: "",
    salary: "",
    requirements: [""],
    employmentType: "FULL_TIME",
    industry: "",
    experienceLevel: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [requirementInput, setRequirementInput] = useState("");
  
  // AI Generation State
  const [aiGeneration, setAiGeneration] = useState<AiGenerationData | null>(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [aiDialogOpen, setAiDialogOpen] = useState(false);

  const { user, isLoading: authLoading } = useAuth();
  const { createJob } = useCreateJob();
  const router = useRouter();

  // Redirect if not authenticated or not authorized
  if (authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!user || user.role === "JOB_SEEKER") {
    router.push("/jobs");
    return null;
  }

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSelectChange = (name: string, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const addRequirement = () => {
    if (
      requirementInput.trim() &&
      !formData.requirements.includes(requirementInput.trim())
    ) {
      setFormData((prev) => ({
        ...prev,
        requirements: [
          ...prev.requirements.filter((req) => req.trim() !== ""),
          requirementInput.trim(),
        ],
      }));
      setRequirementInput("");
    }
  };

  const removeRequirement = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      requirements: prev.requirements.filter((_, i) => i !== index),
    }));
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      e.preventDefault();
      addRequirement();
    }
  };

  // AI Generation Functions
  const generateJobDescription = async () => {
    if (!formData.title || !formData.company) {
      toast.error("Please enter job title and company name first");
      return;
    }

    setIsGenerating(true);
    try {
      const response = await apiClient.generateJobDescription({
        jobTitle: formData.title,
        company: formData.company,
        industry: formData.industry,
        experienceLevel: formData.experienceLevel,
        location: formData.location,
        employmentType: formData.employmentType,
      });

      if (response.success && response.data) {
        setAiGeneration({
          description: response.data.jobDescription,
          suggestedSkills: response.data.suggestedSkills || [],
          salaryRange: response.data.salaryRange,
        });
        setAiDialogOpen(true);
        toast.success("AI job description generated successfully!");
      } else {
        toast.error(response.error || "Failed to generate job description");
      }
    } catch (error) {
      console.error("AI Generation error:", error);
      toast.error("Failed to generate job description");
    } finally {
      setIsGenerating(false);
    }
  };

  const applyAiGeneration = () => {
    if (!aiGeneration) return;

    setFormData((prev) => ({
      ...prev,
      description: aiGeneration.description,
      requirements: [
        ...prev.requirements.filter((req) => req.trim() !== ""),
        ...aiGeneration.suggestedSkills,
      ],
      salary: aiGeneration.salaryRange 
        ? `${aiGeneration.salaryRange.min}-${aiGeneration.salaryRange.max}` 
        : prev.salary,
    }));
    
    setAiDialogOpen(false);
    toast.success("AI suggestions applied to your job posting!");
  };

  const validateForm = (): boolean => {
    if (!formData.title.trim()) {
      setError("Job title is required");
      return false;
    }
    if (!formData.description.trim()) {
      setError("Job description is required");
      return false;
    }
    if (!formData.company.trim()) {
      setError("Company name is required");
      return false;
    }
    if (!formData.location.trim()) {
      setError("Location is required");
      return false;
    }
    if (formData.salary && isNaN(Number(formData.salary.split('-')[0]))) {
      setError("Salary must be a valid number or range");
      return false;
    }
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      const jobData = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        company: formData.company.trim(),
        location: formData.location.trim(),
        salary: formData.salary ? Number(formData.salary.split('-')[0]) : undefined,
        requirements: formData.requirements.filter((req) => req.trim() !== ""),
      };

      const result = await createJob(jobData);

      if (result.success) {
        toast.success("Job posted successfully!");
        router.push("/jobs");
      } else {
        setError(result.error || "Failed to create job posting");
      }
    } catch (err) {
      setError("An unexpected error occurred");
      console.error("Job creation error:", err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <CanCreateJobs fallback={<div>Access denied</div>}>
      <DashboardLayout>
        <div className="space-y-6 max-w-5xl mx-auto p-6">
          {/* Header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button variant="ghost" size="sm" asChild>
                <Link href="/jobs">
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back to Jobs
                </Link>
              </Button>
              <div>
                <h1 className="text-3xl font-bold text-foreground">
                  Create New Job
                </h1>
                <p className="text-muted-foreground">
                  Post a new job opportunity with AI-powered assistance
                </p>
              </div>
            </div>

            {/* AI Generate Button */}
            <Button
              onClick={generateJobDescription}
              disabled={isGenerating || !formData.title || !formData.company}
              className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700"
            >
              {isGenerating ? (
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              ) : (
                <Sparkles className="h-4 w-4 mr-2" />
              )}
              Generate with AI
            </Button>
          </div>

          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <Tabs defaultValue="basic" className="w-full">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="basic">Basic Information</TabsTrigger>
                <TabsTrigger value="details">Job Details</TabsTrigger>
                <TabsTrigger value="requirements">Requirements</TabsTrigger>
              </TabsList>

              <TabsContent value="basic" className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Building className="h-5 w-5" />
                      Basic Information
                    </CardTitle>
                    <CardDescription>
                      Enter the core details about your job posting
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="title">
                          Job Title <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id="title"
                          name="title"
                          value={formData.title}
                          onChange={handleInputChange}
                          placeholder="e.g., Senior Frontend Developer"
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="company">
                          Company Name <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id="company"
                          name="company"
                          value={formData.company}
                          onChange={handleInputChange}
                          placeholder="e.g., TechCorp Inc."
                          required
                        />
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="location">
                          Location <span className="text-red-500">*</span>
                        </Label>
                        <div className="relative">
                          <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                          <Input
                            id="location"
                            name="location"
                            value={formData.location}
                            onChange={handleInputChange}
                            className="pl-10"
                            placeholder="e.g., New York, NY"
                            required
                          />
                        </div>
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="industry">Industry</Label>
                        <Select
                          value={formData.industry}
                          onValueChange={(value) => handleSelectChange("industry", value)}
                        >
                          <SelectTrigger>
                            <SelectValue placeholder="Select industry" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="technology">Technology</SelectItem>
                            <SelectItem value="finance">Finance</SelectItem>
                            <SelectItem value="healthcare">Healthcare</SelectItem>
                            <SelectItem value="education">Education</SelectItem>
                            <SelectItem value="retail">Retail</SelectItem>
                            <SelectItem value="manufacturing">Manufacturing</SelectItem>
                            <SelectItem value="consulting">Consulting</SelectItem>
                            <SelectItem value="other">Other</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="experienceLevel">Experience Level</Label>
                        <Select
                          value={formData.experienceLevel}
                          onValueChange={(value) => handleSelectChange("experienceLevel", value)}
                        >
                          <SelectTrigger>
                            <SelectValue placeholder="Select level" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="entry">Entry Level (0-2 years)</SelectItem>
                            <SelectItem value="mid">Mid Level (3-5 years)</SelectItem>
                            <SelectItem value="senior">Senior Level (6+ years)</SelectItem>
                            <SelectItem value="lead">Lead/Principal</SelectItem>
                            <SelectItem value="executive">Executive</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value="details" className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <FileText className="h-5 w-5" />
                      Job Details
                    </CardTitle>
                    <CardDescription>
                      Provide detailed information about the role
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="employmentType">Employment Type</Label>
                        <Select
                          value={formData.employmentType}
                          onValueChange={(value) => handleSelectChange("employmentType", value)}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="FULL_TIME">Full-time</SelectItem>
                            <SelectItem value="PART_TIME">Part-time</SelectItem>
                            <SelectItem value="CONTRACT">Contract</SelectItem>
                            <SelectItem value="FREELANCE">Freelance</SelectItem>
                            <SelectItem value="INTERNSHIP">Internship</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="salary">Salary Range (Optional)</Label>
                        <div className="relative">
                          <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                          <Input
                            id="salary"
                            name="salary"
                            type="text"
                            value={formData.salary}
                            onChange={handleInputChange}
                            className="pl-10"
                            placeholder="e.g., 80000-120000"
                          />
                        </div>
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="description">
                        Job Description <span className="text-red-500">*</span>
                      </Label>
                      <Textarea
                        id="description"
                        name="description"
                        value={formData.description}
                        onChange={handleInputChange}
                        className="min-h-[200px]"
                        placeholder="Describe the role, responsibilities, and what makes this position exciting..."
                        required
                      />
                      <p className="text-sm text-muted-foreground">
                        💡 Tip: Use the &quot;Generate with AI&quot; button above to create a comprehensive job description
                      </p>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value="requirements" className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle>Requirements & Skills</CardTitle>
                    <CardDescription>
                      Add the skills and qualifications needed for this role
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      <Label>Add Requirement</Label>
                      <div className="flex gap-2">
                        <Input
                          value={requirementInput}
                          onChange={(e) => setRequirementInput(e.target.value)}
                          onKeyPress={handleKeyPress}
                          placeholder="e.g., 3+ years experience with React"
                        />
                        <Button type="button" onClick={addRequirement}>
                          <Plus className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>

                    {formData.requirements.filter((req) => req.trim()).length > 0 && (
                      <div className="space-y-2">
                        <Label>Current Requirements</Label>
                        <div className="flex flex-wrap gap-2">
                          {formData.requirements
                            .filter((req) => req.trim())
                            .map((requirement, index) => (
                              <Badge
                                key={index}
                                variant="secondary"
                                className="flex items-center gap-2"
                              >
                                {requirement}
                                <X
                                  className="h-3 w-3 cursor-pointer hover:text-red-500"
                                  onClick={() => removeRequirement(index)}
                                />
                              </Badge>
                            ))}
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>

            {/* Submit Button */}
            <div className="flex justify-end gap-4">
              <Button type="button" variant="outline" onClick={() => router.back()}>
                Cancel
              </Button>
              <Button type="submit" disabled={isLoading} className="px-8">
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Creating...
                  </>
                ) : (
                  "Post Job"
                )}
              </Button>
            </div>
          </form>
        </div>

        {/* AI Generation Dialog */}
        <Dialog open={aiDialogOpen} onOpenChange={setAiDialogOpen}>
          <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <Brain className="h-5 w-5 text-purple-600" />
                AI-Generated Job Description
              </DialogTitle>
              <DialogDescription>
                Review and apply the AI-generated content to your job posting
              </DialogDescription>
            </DialogHeader>

            {aiGeneration && (
              <div className="space-y-6">
                <div>
                  <Label className="text-base font-semibold">Generated Description</Label>
                  <Card className="mt-2">
                    <CardContent className="pt-4">
                      <p className="whitespace-pre-wrap text-sm">
                        {aiGeneration.description}
                      </p>
                    </CardContent>
                  </Card>
                </div>

                {aiGeneration.suggestedSkills.length > 0 && (
                  <div>
                    <Label className="text-base font-semibold">Suggested Skills</Label>
                    <div className="flex flex-wrap gap-2 mt-2">
                      {aiGeneration.suggestedSkills.map((skill, index) => (
                        <Badge key={index} variant="outline">
                          {skill}
                        </Badge>
                      ))}
                    </div>
                  </div>
                )}

                {aiGeneration.salaryRange && (
                  <div>
                    <Label className="text-base font-semibold">Suggested Salary Range</Label>
                    <p className="text-sm text-muted-foreground mt-1">
                      ${aiGeneration.salaryRange.min.toLocaleString()} - ${aiGeneration.salaryRange.max.toLocaleString()}
                    </p>
                  </div>
                )}

                <div className="flex justify-end gap-4">
                  <Button variant="outline" onClick={() => setAiDialogOpen(false)}>
                    Cancel
                  </Button>
                  <Button onClick={applyAiGeneration} className="bg-gradient-to-r from-purple-600 to-pink-600">
                    Apply to Job Posting
                  </Button>
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>
      </DashboardLayout>
    </CanCreateJobs>
  );
}