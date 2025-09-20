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
import {
  ArrowLeft,
  Building,
  MapPin,
  DollarSign,
  FileText,
  Plus,
  X,
} from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";
import { CanCreateJobs } from "@/components/auth/RoleBasedUI";

interface JobFormData {
  title: string;
  description: string;
  company: string;
  location: string;
  salary: string;
  requirements: string[];
}

export default function CreateJobPage() {
  const [formData, setFormData] = useState<JobFormData>({
    title: "",
    description: "",
    company: "",
    location: "",
    salary: "",
    requirements: [""],
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [requirementInput, setRequirementInput] = useState("");

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
    if (formData.salary && isNaN(Number(formData.salary))) {
      setError("Salary must be a valid number");
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
        salary: formData.salary ? Number(formData.salary) : undefined,
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
        <div className="space-y-6">
          {/* Header */}
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="sm" asChild>
              <Link href="/jobs">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Jobs
              </Link>
            </Button>
            <div>
              <h1 className="text-2xl font-bold text-foreground">
                Create New Job
              </h1>
              <p className="text-muted-foreground">
                Post a new job opportunity for talented professionals
              </p>
            </div>
          </div>

          {/* Form */}
          <Card className="max-w-4xl">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Building className="h-5 w-5" />
                Job Details
              </CardTitle>
              <CardDescription>
                Fill in the details for your job posting. All fields marked with
                * are required.
              </CardDescription>
            </CardHeader>
            <form onSubmit={handleSubmit}>
              <CardContent className="space-y-6">
                {error && (
                  <Alert variant="destructive">
                    <AlertDescription>{error}</AlertDescription>
                  </Alert>
                )}

                {/* Basic Information */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="title">Job Title *</Label>
                    <Input
                      id="title"
                      name="title"
                      type="text"
                      required
                      value={formData.title}
                      onChange={handleInputChange}
                      placeholder="e.g. Senior Software Engineer"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label
                      htmlFor="company"
                      className="flex items-center gap-2"
                    >
                      <Building className="h-4 w-4" />
                      Company *
                    </Label>
                    <Input
                      id="company"
                      name="company"
                      type="text"
                      required
                      value={formData.company}
                      onChange={handleInputChange}
                      placeholder="e.g. TechCorp Inc."
                    />
                  </div>

                  <div className="space-y-2">
                    <Label
                      htmlFor="location"
                      className="flex items-center gap-2"
                    >
                      <MapPin className="h-4 w-4" />
                      Location *
                    </Label>
                    <Input
                      id="location"
                      name="location"
                      type="text"
                      required
                      value={formData.location}
                      onChange={handleInputChange}
                      placeholder="e.g. New York, NY (Remote)"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="salary" className="flex items-center gap-2">
                      <DollarSign className="h-4 w-4" />
                      Salary (Optional)
                    </Label>
                    <Input
                      id="salary"
                      name="salary"
                      type="number"
                      value={formData.salary}
                      onChange={handleInputChange}
                      placeholder="e.g. 85000"
                    />
                  </div>
                </div>

                {/* Description */}
                <div className="space-y-2">
                  <Label
                    htmlFor="description"
                    className="flex items-center gap-2"
                  >
                    <FileText className="h-4 w-4" />
                    Job Description *
                  </Label>
                  <Textarea
                    id="description"
                    name="description"
                    required
                    value={formData.description}
                    onChange={handleInputChange}
                    placeholder="Describe the role, responsibilities, and what makes this opportunity great..."
                    className="min-h-[120px]"
                  />
                </div>

                {/* Requirements */}
                <div className="space-y-4">
                  <Label>Job Requirements</Label>
                  <div className="space-y-3">
                    <div className="flex gap-2">
                      <Input
                        value={requirementInput}
                        onChange={(e) => setRequirementInput(e.target.value)}
                        onKeyPress={handleKeyPress}
                        placeholder="e.g. 3+ years experience with React"
                        className="flex-1"
                      />
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={addRequirement}
                        disabled={!requirementInput.trim()}
                      >
                        <Plus className="h-4 w-4" />
                      </Button>
                    </div>

                    {formData.requirements.length > 0 && (
                      <div className="space-y-2">
                        <p className="text-sm text-muted-foreground">
                          Requirements:
                        </p>
                        <div className="space-y-2">
                          {formData.requirements
                            .filter((req) => req.trim() !== "")
                            .map((requirement, index) => (
                              <div
                                key={index}
                                className="flex items-center justify-between p-3 bg-muted rounded-lg"
                              >
                                <span className="text-sm">{requirement}</span>
                                <Button
                                  type="button"
                                  variant="ghost"
                                  size="sm"
                                  onClick={() => removeRequirement(index)}
                                  className="h-6 w-6 p-0"
                                >
                                  <X className="h-4 w-4" />
                                </Button>
                              </div>
                            ))}
                        </div>
                      </div>
                    )}
                  </div>
                </div>

                {/* Submit Button */}
                <div className="flex gap-4 pt-4">
                  <Button
                    type="submit"
                    disabled={isLoading}
                    className="flex-1 sm:flex-initial"
                  >
                    {isLoading ? (
                      <>
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2" />
                        Creating Job...
                      </>
                    ) : (
                      <>
                        <Plus className="h-4 w-4 mr-2" />
                        Create Job Posting
                      </>
                    )}
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    asChild
                    disabled={isLoading}
                  >
                    <Link href="/jobs">Cancel</Link>
                  </Button>
                </div>
              </CardContent>
            </form>
          </Card>
        </div>
      </DashboardLayout>
    </CanCreateJobs>
  );
}
