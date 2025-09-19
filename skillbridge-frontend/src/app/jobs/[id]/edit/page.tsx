"use client";

import { useState, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { useAuth, useJob, useUpdateJob } from "@/hooks";
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
  Save,
} from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";
import { Job } from "@/lib/api";

interface JobFormData {
  title: string;
  description: string;
  company: string;
  location: string;
  salary: string;
  requirements: string[];
}

interface JobEditPageProps {
  params: Promise<{ id: string }>;
}

export default function JobEditPage({ params }: JobEditPageProps) {
  const resolvedParams = use(params);
  const [formData, setFormData] = useState<JobFormData>({
    title: "",
    description: "",
    company: "",
    location: "",
    salary: "",
    requirements: [""],
  });
  const [isLoading, setIsLoading] = useState(false);
  const [newRequirement, setNewRequirement] = useState("");

  const { user, isLoading: authLoading } = useAuth();
  const { job } = useJob(parseInt(resolvedParams.id));
  const { updateJob } = useUpdateJob();
  const router = useRouter();

  // Populate form when job is loaded
  useEffect(() => {
    if (job) {
      setFormData({
        title: job.title,
        description: job.description,
        company: job.company,
        location: job.location,
        salary: job.salary ? job.salary.toString() : "",
        requirements: job.requirements.length > 0 ? job.requirements : [""],
      });
    }
  }, [job]);

  // Auth checks
  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login");
      return;
    }

    if (user && user.role !== "EMPLOYER") {
      router.push("/dashboard");
      return;
    }

    // Check if user owns this job
    if (job && user && job.employerId !== user.id) {
      toast.error("You don't have permission to edit this job");
      router.push("/employer/jobs");
      return;
    }
  }, [user, authLoading, job, router]);

  const handleInputChange = (field: keyof JobFormData, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const addRequirement = () => {
    if (newRequirement.trim()) {
      setFormData((prev) => ({
        ...prev,
        requirements: [
          ...prev.requirements.filter((req) => req.trim()),
          newRequirement.trim(),
        ],
      }));
      setNewRequirement("");
    }
  };

  const removeRequirement = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      requirements: prev.requirements.filter((_, i) => i !== index),
    }));
  };

  const updateRequirement = (index: number, value: string) => {
    setFormData((prev) => ({
      ...prev,
      requirements: prev.requirements.map((req, i) =>
        i === index ? value : req
      ),
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!job) return;

    // Validation
    if (!formData.title.trim()) {
      toast.error("Job title is required");
      return;
    }
    if (!formData.description.trim()) {
      toast.error("Job description is required");
      return;
    }
    if (!formData.company.trim()) {
      toast.error("Company name is required");
      return;
    }
    if (!formData.location.trim()) {
      toast.error("Location is required");
      return;
    }

    setIsLoading(true);

    try {
      const updatedJobData: Partial<Job> = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        company: formData.company.trim(),
        location: formData.location.trim(),
        salary: formData.salary ? parseFloat(formData.salary) : undefined,
        requirements: formData.requirements.filter((req) => req.trim()),
      };

      const result = await updateJob(job.id, updatedJobData);

      if (result.success) {
        toast.success("Job updated successfully!");
        router.push(`/jobs/${job.id}`);
      } else {
        toast.error(result.error || "Failed to update job");
      }
    } catch {
      toast.error("An error occurred while updating the job");
    } finally {
      setIsLoading(false);
    }
  };

  if (authLoading || !job) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        </div>
      </DashboardLayout>
    );
  }

  if (!user || user.role !== "EMPLOYER" || !job) {
    return null;
  }

  return (
    <DashboardLayout>
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" asChild>
            <Link href={`/jobs/${job.id}`}>
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Job
            </Link>
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-foreground">
              Edit Job Post
            </h1>
            <p className="text-muted-foreground">
              Update your job posting to attract the right candidates
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="h-5 w-5" />
                Job Information
              </CardTitle>
              <CardDescription>
                Provide clear and detailed information about the position
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="title">Job Title *</Label>
                  <Input
                    id="title"
                    value={formData.title}
                    onChange={(e) => handleInputChange("title", e.target.value)}
                    placeholder="e.g. Senior Software Engineer"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="company">Company *</Label>
                  <div className="relative">
                    <Building className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                    <Input
                      id="company"
                      value={formData.company}
                      onChange={(e) =>
                        handleInputChange("company", e.target.value)
                      }
                      className="pl-10"
                      placeholder="Company name"
                      required
                    />
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="location">Location *</Label>
                  <div className="relative">
                    <MapPin className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                    <Input
                      id="location"
                      value={formData.location}
                      onChange={(e) =>
                        handleInputChange("location", e.target.value)
                      }
                      className="pl-10"
                      placeholder="e.g. New York, NY or Remote"
                      required
                    />
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="salary">Salary (optional)</Label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                    <Input
                      id="salary"
                      type="number"
                      value={formData.salary}
                      onChange={(e) =>
                        handleInputChange("salary", e.target.value)
                      }
                      className="pl-10"
                      placeholder="Annual salary in USD"
                      min="0"
                    />
                  </div>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Job Description *</Label>
                <Textarea
                  id="description"
                  value={formData.description}
                  onChange={(e) =>
                    handleInputChange("description", e.target.value)
                  }
                  rows={8}
                  placeholder="Describe the role, responsibilities, and what makes this opportunity exciting..."
                  className="resize-none"
                  required
                />
              </div>
            </CardContent>
          </Card>

          {/* Requirements */}
          <Card>
            <CardHeader>
              <CardTitle>Requirements & Skills</CardTitle>
              <CardDescription>
                List the key requirements and skills needed for this position
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* Existing Requirements */}
              <div className="space-y-2">
                {formData.requirements.map((requirement, index) => (
                  <div key={index} className="flex gap-2">
                    <Input
                      value={requirement}
                      onChange={(e) => updateRequirement(index, e.target.value)}
                      placeholder="e.g. 3+ years of React experience"
                    />
                    <Button
                      type="button"
                      variant="outline"
                      size="icon"
                      onClick={() => removeRequirement(index)}
                      disabled={formData.requirements.length === 1}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>

              {/* Add New Requirement */}
              <div className="flex gap-2">
                <Input
                  value={newRequirement}
                  onChange={(e) => setNewRequirement(e.target.value)}
                  placeholder="Add a new requirement..."
                  onKeyPress={(e) => {
                    if (e.key === "Enter") {
                      e.preventDefault();
                      addRequirement();
                    }
                  }}
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={addRequirement}
                  disabled={!newRequirement.trim()}
                >
                  <Plus className="h-4 w-4" />
                </Button>
              </div>

              <Alert>
                <AlertDescription>
                  Add specific requirements, skills, and qualifications that
                  candidates should have. Be clear about what&apos;s required
                  vs. preferred.
                </AlertDescription>
              </Alert>
            </CardContent>
          </Card>

          {/* Submit Actions */}
          <div className="flex justify-end gap-4">
            <Button variant="outline" asChild>
              <Link href={`/jobs/${job.id}`}>Cancel</Link>
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Updating...
                </>
              ) : (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  Update Job
                </>
              )}
            </Button>
          </div>
        </form>
      </div>
    </DashboardLayout>
  );
}
