"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/hooks";
import { useUpdateEmployerProfile } from "@/hooks/api/use-employer";
import DashboardLayout from "@/components/layouts/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Checkbox } from "@/components/ui/checkbox";
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
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Profile,
  Skill,
  Education,
  Experience,
  Portfolio,
  apiClient,
} from "@/lib/api";
import {
  User,
  BookOpen,
  Briefcase,
  FolderOpen,
  Plus,
  Edit,
  Trash2,
  Building2,
  Globe,
  MapPin,
  Phone,
  Save,
} from "lucide-react";
import { toast } from "sonner";

export default function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("overview");
  const { user, isLoading, updateUser } = useAuth();
  const { updateProfile: updateEmployerProfile } = useUpdateEmployerProfile();
  const router = useRouter();

  // Form states
  const [showSkillDialog, setShowSkillDialog] = useState(false);
  const [showEducationDialog, setShowEducationDialog] = useState(false);
  const [showExperienceDialog, setShowExperienceDialog] = useState(false);
  const [showPortfolioDialog, setShowPortfolioDialog] = useState(false);
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);
  const [isUpdatingBio, setIsUpdatingBio] = useState(false);

  const [skillForm, setSkillForm] = useState<Omit<Skill, "id">>({
    name: "",
    level: "BEGINNER",
  });

  const [educationForm, setEducationForm] = useState({
    institution: "",
    degree: "",
    fieldOfStudy: "",
    startDate: "",
    endDate: "",
    description: "",
  });

  const [experienceForm, setExperienceForm] = useState({
    company: "",
    position: "",
    startDate: "",
    endDate: "",
    description: "",
    current: false,
  });

  const [portfolioForm, setPortfolioForm] = useState({
    title: "",
    description: "",
    url: "",
    technologies: [] as string[],
    imageUrl: "",
  });

  const [editingItem, setEditingItem] = useState<{
    type: "education" | "experience" | "portfolio";
    id: number;
  } | null>(null);

  // Employer profile form states
  const [employerForm, setEmployerForm] = useState({
    companyName: "",
    companyDescription: "",
    companyWebsite: "",
    companyLocation: "",
    contactPhone: "",
  });

  const fetchProfile = useCallback(async () => {
    if (!user) return;

    try {
      setLoading(true);
      const response = await apiClient.getUserProfile(user.id);

      if (response.success && response.data) {
        setProfile(response.data);
      } else {
        // Profile doesn't exist, create empty one
        setProfile({
          id: 0,
          userId: user.id,
          bio: "",
          skills: [],
          education: [],
          experience: [],
          portfolio: [],
        });
      }
    } catch {
      toast.error("Error fetching profile");
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    if (!isLoading && !user) {
      router.push("/login");
      return;
    }
    if (user) {
      fetchProfile();

      // Initialize employer form if user is an employer
      if (user.role === "EMPLOYER") {
        setEmployerForm({
          companyName: user.companyName || "",
          companyDescription: user.companyDescription || "",
          companyWebsite: user.companyWebsite || "",
          companyLocation: user.companyLocation || "",
          contactPhone: user.contactPhone || "",
        });
      }
    }
  }, [user, isLoading, router, fetchProfile]);

  const handleAddSkill = async () => {
    if (!user || !skillForm.name.trim()) return;

    try {
      const response = await apiClient.addSkill(user.id, skillForm);
      if (response.success && response.data) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                skills: [...prev.skills, response.data!],
              }
            : null
        );
        setSkillForm({ name: "", level: "BEGINNER" });
        setShowSkillDialog(false);
        toast.success("Skill added successfully");
      } else {
        toast.error("Failed to add skill");
      }
    } catch {
      toast.error("Error adding skill");
    }
  };

  const handleRemoveSkill = async (skillId: number) => {
    if (!user) return;

    try {
      const response = await apiClient.removeSkill(user.id, skillId);
      if (response.success) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                skills: prev.skills.filter((skill) => skill.id !== skillId),
              }
            : null
        );
        toast.success("Skill removed");
      } else {
        toast.error("Failed to remove skill");
      }
    } catch {
      toast.error("Error removing skill");
    }
  };

  const handleUpdateBio = async () => {
    if (!user || !profile) return;

    try {
      setIsUpdatingBio(true);
      const response = await apiClient.updateUserProfile(user.id, {
        bio: profile.bio,
      });

      if (response.success) {
        toast.success("Bio updated successfully");
      } else {
        toast.error("Failed to update bio");
      }
    } catch {
      toast.error("Error updating bio");
    } finally {
      setIsUpdatingBio(false);
    }
  };

  const handleAddEducation = async () => {
    if (!user) return;

    try {
      const response = await apiClient.addEducation(user.id, educationForm);
      if (response.success && response.data) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                education: [...prev.education, response.data!],
              }
            : null
        );
        setEducationForm({
          institution: "",
          degree: "",
          fieldOfStudy: "",
          startDate: "",
          endDate: "",
          description: "",
        });
        setShowEducationDialog(false);
        toast.success("Education added successfully");
      } else {
        toast.error("Failed to add education");
      }
    } catch {
      toast.error("Error adding education");
    }
  };

  const handleUpdateEducation = async () => {
    if (!user || !editingItem || editingItem.type !== "education") return;

    try {
      const response = await apiClient.updateEducation(
        user.id,
        editingItem.id,
        educationForm
      );
      if (response.success && response.data) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                education: prev.education.map((edu) =>
                  edu.id === editingItem.id ? response.data! : edu
                ),
              }
            : null
        );
        setEditingItem(null);
        setShowEducationDialog(false);
        toast.success("Education updated successfully");
      } else {
        toast.error("Failed to update education");
      }
    } catch {
      toast.error("Error updating education");
    }
  };

  const handleDeleteEducation = async (eduId: number) => {
    if (!user) return;

    try {
      const response = await apiClient.deleteEducation(user.id, eduId);
      if (response.success) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                education: prev.education.filter((edu) => edu.id !== eduId),
              }
            : null
        );
        toast.success("Education deleted");
      } else {
        toast.error("Failed to delete education");
      }
    } catch {
      toast.error("Error deleting education");
    }
  };

  const handleAddExperience = async () => {
    if (!user) return;

    try {
      const response = await apiClient.addExperience(user.id, experienceForm);
      if (response.success && response.data) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                experience: [...prev.experience, response.data!],
              }
            : null
        );
        setExperienceForm({
          company: "",
          position: "",
          startDate: "",
          endDate: "",
          description: "",
          current: false,
        });
        setShowExperienceDialog(false);
        toast.success("Experience added successfully");
      } else {
        toast.error("Failed to add experience");
      }
    } catch {
      toast.error("Error adding experience");
    }
  };

  const handleUpdateExperience = async () => {
    if (!user || !editingItem || editingItem.type !== "experience") return;

    try {
      const response = await apiClient.updateExperience(
        user.id,
        editingItem.id,
        experienceForm
      );
      if (response.success && response.data) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                experience: prev.experience.map((exp) =>
                  exp.id === editingItem.id ? response.data! : exp
                ),
              }
            : null
        );
        setEditingItem(null);
        setShowExperienceDialog(false);
        toast.success("Experience updated successfully");
      } else {
        toast.error("Failed to update experience");
      }
    } catch {
      toast.error("Error updating experience");
    }
  };

  const handleDeleteExperience = async (expId: number) => {
    if (!user) return;

    try {
      const response = await apiClient.deleteExperience(user.id, expId);
      if (response.success) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                experience: prev.experience.filter((exp) => exp.id !== expId),
              }
            : null
        );
        toast.success("Experience deleted");
      } else {
        toast.error("Failed to delete experience");
      }
    } catch {
      toast.error("Error deleting experience");
    }
  };

  const handleAddPortfolio = async () => {
    if (!user) return;

    try {
      const response = await apiClient.addPortfolio(user.id, portfolioForm);
      if (response.success && response.data) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                portfolio: [...prev.portfolio, response.data!],
              }
            : null
        );
        setPortfolioForm({
          title: "",
          description: "",
          url: "",
          technologies: [],
          imageUrl: "",
        });
        setShowPortfolioDialog(false);
        toast.success("Portfolio item added successfully");
      } else {
        toast.error("Failed to add portfolio item");
      }
    } catch {
      toast.error("Error adding portfolio item");
    }
  };

  const handleUpdatePortfolio = async () => {
    if (!user || !editingItem || editingItem.type !== "portfolio") return;

    try {
      const response = await apiClient.updatePortfolio(
        user.id,
        editingItem.id,
        portfolioForm
      );
      if (response.success && response.data) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                portfolio: prev.portfolio.map((item) =>
                  item.id === editingItem.id ? response.data! : item
                ),
              }
            : null
        );
        setEditingItem(null);
        setShowPortfolioDialog(false);
        toast.success("Portfolio item updated successfully");
      } else {
        toast.error("Failed to update portfolio item");
      }
    } catch {
      toast.error("Error updating portfolio item");
    }
  };

  const handleDeletePortfolio = async (portfolioId: number) => {
    if (!user) return;

    try {
      const response = await apiClient.deletePortfolio(user.id, portfolioId);
      if (response.success) {
        setProfile((prev) =>
          prev
            ? {
                ...prev,
                portfolio: prev.portfolio.filter(
                  (item) => item.id !== portfolioId
                ),
              }
            : null
        );
        toast.success("Portfolio item deleted");
      } else {
        toast.error("Failed to delete portfolio item");
      }
    } catch {
      toast.error("Error deleting portfolio item");
    }
  };

  const handleUpdateEmployerProfile = async () => {
    if (!user || user.role !== "EMPLOYER") return;

    try {
      setIsUpdatingProfile(true);

      // Filter out empty values
      const updateData = Object.entries(employerForm).reduce(
        (acc, [key, value]) => {
          if (value.trim() !== "") {
            acc[key as keyof typeof employerForm] = value;
          }
          return acc;
        },
        {} as Partial<typeof employerForm>
      );

      const result = await updateEmployerProfile(updateData);

      if (result.success && result.user) {
        // Update the user context with new data
        updateUser(result.user);
        toast.success("Profile updated successfully");
      } else {
        toast.error(result.error || "Failed to update profile");
      }
    } catch {
      toast.error("Error updating profile");
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  // Helper functions for editing
  const startEditingEducation = (education: Education) => {
    setEducationForm({
      institution: education.institution || "",
      degree: education.degree || "",
      fieldOfStudy: education.fieldOfStudy || "",
      startDate: education.startDate || "",
      endDate: education.endDate || "",
      description: education.description || "",
    });
    setEditingItem({ type: "education", id: education.id });
    setShowEducationDialog(true);
  };

  const startEditingExperience = (experience: Experience) => {
    setExperienceForm({
      company: experience.company || "",
      position: experience.position || "",
      startDate: experience.startDate || "",
      endDate: experience.endDate || "",
      description: experience.description || "",
      current: experience.current || false,
    });
    setEditingItem({ type: "experience", id: experience.id });
    setShowExperienceDialog(true);
  };

  const startEditingPortfolio = (portfolio: Portfolio) => {
    setPortfolioForm({
      title: portfolio.title || "",
      description: portfolio.description || "",
      url: portfolio.url || "",
      technologies: portfolio.technologies || [],
      imageUrl: portfolio.imageUrl || "",
    });
    setEditingItem({ type: "portfolio", id: portfolio.id });
    setShowPortfolioDialog(true);
  };

  if (isLoading || loading) {
    return (
      <DashboardLayout>
        <div className="min-h-screen flex items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        </div>
      </DashboardLayout>
    );
  }

  if (!user || !profile) {
    return null;
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Profile Header */}
        <Card>
          <CardHeader>
            <div className="flex items-center space-x-4">
              <div className="w-16 h-16 bg-primary text-primary-foreground rounded-full flex items-center justify-center text-2xl font-bold">
                {user.firstName?.[0] || user.username?.[0] || "U"}
              </div>
              <div>
                <h1 className="text-2xl font-bold">
                  {user.firstName && user.lastName
                    ? `${user.firstName} ${user.lastName}`
                    : user.username}
                </h1>
                <p className="text-muted-foreground">{user.email}</p>
                <Badge variant="secondary">
                  {user.role === "JOB_SEEKER" ? "Job Seeker" : "Employer"}
                </Badge>
              </div>
            </div>
          </CardHeader>
        </Card>

        {/* Profile Tabs */}
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList
            className={`grid w-full ${
              user.role === "EMPLOYER" ? "grid-cols-2" : "grid-cols-4"
            }`}
          >
            <TabsTrigger value="overview" className="flex items-center">
              <User className="mr-2 h-4 w-4" />
              Overview
            </TabsTrigger>
            {user.role === "EMPLOYER" ? (
              <TabsTrigger value="company" className="flex items-center">
                <Building2 className="mr-2 h-4 w-4" />
                Company Profile
              </TabsTrigger>
            ) : (
              <>
                <TabsTrigger value="education" className="flex items-center">
                  <BookOpen className="mr-2 h-4 w-4" />
                  Education
                </TabsTrigger>
                <TabsTrigger value="experience" className="flex items-center">
                  <Briefcase className="mr-2 h-4 w-4" />
                  Experience
                </TabsTrigger>
                <TabsTrigger value="portfolio" className="flex items-center">
                  <FolderOpen className="mr-2 h-4 w-4" />
                  Portfolio
                </TabsTrigger>
              </>
            )}
          </TabsList>

          {/* Overview Tab */}
          <TabsContent value="overview" className="space-y-6">
            {user.role === "EMPLOYER" ? (
              /* Employer Overview */
              <Card>
                <CardHeader>
                  <CardTitle>Account Information</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <Label htmlFor="firstName">First Name</Label>
                        <Input
                          id="firstName"
                          value={user.firstName || ""}
                          disabled
                          className="bg-muted"
                        />
                      </div>
                      <div>
                        <Label htmlFor="lastName">Last Name</Label>
                        <Input
                          id="lastName"
                          value={user.lastName || ""}
                          disabled
                          className="bg-muted"
                        />
                      </div>
                    </div>
                    <div>
                      <Label htmlFor="email">Email Address</Label>
                      <Input
                        id="email"
                        value={user.email}
                        disabled
                        className="bg-muted"
                      />
                    </div>
                    <div className="text-sm text-muted-foreground">
                      Contact support to update your account information
                    </div>
                  </div>
                </CardContent>
              </Card>
            ) : (
              /* Job Seeker Overview */
              <>
                <Card>
                  <CardHeader>
                    <CardTitle>About</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      <div>
                        <Label htmlFor="bio">Bio</Label>
                        <Textarea
                          id="bio"
                          placeholder="Tell us about yourself..."
                          value={profile?.bio || ""}
                          onChange={(e) =>
                            setProfile((prev) =>
                              prev ? { ...prev, bio: e.target.value } : null
                            )
                          }
                          className="mt-1"
                        />
                      </div>
                      <Button
                        onClick={handleUpdateBio}
                        disabled={isUpdatingBio}
                        className="flex items-center gap-2"
                      >
                        <Save className="h-4 w-4" />
                        {isUpdatingBio ? "Saving..." : "Save Changes"}
                      </Button>
                    </div>
                  </CardContent>
                </Card>

                {/* Skills Section */}
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between">
                    <CardTitle>Skills</CardTitle>
                    <Dialog
                      open={showSkillDialog}
                      onOpenChange={setShowSkillDialog}
                    >
                      <DialogTrigger asChild>
                        <Button size="sm">
                          <Plus className="mr-2 h-4 w-4" />
                          Add Skill
                        </Button>
                      </DialogTrigger>
                      <DialogContent>
                        <DialogHeader>
                          <DialogTitle>Add New Skill</DialogTitle>
                          <DialogDescription>
                            Add a skill to your profile
                          </DialogDescription>
                        </DialogHeader>
                        <div className="space-y-4">
                          <div>
                            <Label htmlFor="skill-name">Skill Name</Label>
                            <Input
                              id="skill-name"
                              value={skillForm.name}
                              onChange={(e) =>
                                setSkillForm((prev) => ({
                                  ...prev,
                                  name: e.target.value,
                                }))
                              }
                              placeholder="e.g., JavaScript, Python, Design"
                            />
                          </div>
                          <div>
                            <Label htmlFor="skill-level">
                              Proficiency Level
                            </Label>
                            <Select
                              value={skillForm.level}
                              onValueChange={(
                                value:
                                  | "BEGINNER"
                                  | "INTERMEDIATE"
                                  | "ADVANCED"
                                  | "EXPERT"
                              ) =>
                                setSkillForm((prev) => ({
                                  ...prev,
                                  level: value,
                                }))
                              }
                            >
                              <SelectTrigger>
                                <SelectValue />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="BEGINNER">
                                  Beginner
                                </SelectItem>
                                <SelectItem value="INTERMEDIATE">
                                  Intermediate
                                </SelectItem>
                                <SelectItem value="ADVANCED">
                                  Advanced
                                </SelectItem>
                                <SelectItem value="EXPERT">Expert</SelectItem>
                              </SelectContent>
                            </Select>
                          </div>
                        </div>
                        <DialogFooter>
                          <Button
                            variant="outline"
                            onClick={() => setShowSkillDialog(false)}
                          >
                            Cancel
                          </Button>
                          <Button onClick={handleAddSkill}>Add Skill</Button>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>
                  </CardHeader>
                  <CardContent>
                    {profile?.skills.length === 0 ? (
                      <p className="text-muted-foreground text-center py-8">
                        No skills added yet
                      </p>
                    ) : (
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {profile?.skills.map((skill) => (
                          <div
                            key={skill.id}
                            className="flex items-center justify-between p-3 border rounded-lg"
                          >
                            <div>
                              <div className="font-medium">{skill.name}</div>
                              <Badge variant="outline" className="text-xs">
                                {skill.level.toLowerCase()}
                              </Badge>
                            </div>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleRemoveSkill(skill.id)}
                              className="text-red-600 hover:text-red-700"
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        ))}
                      </div>
                    )}
                  </CardContent>
                </Card>
              </>
            )}
          </TabsContent>

          {/* Education Tab - Only for Job Seekers */}
          {user.role !== "EMPLOYER" && (
            <TabsContent value="education">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle>Education</CardTitle>
                  <Dialog
                    open={showEducationDialog}
                    onOpenChange={setShowEducationDialog}
                  >
                    <DialogTrigger asChild>
                      <Button
                        size="sm"
                        onClick={() => {
                          setEditingItem(null);
                          setEducationForm({
                            institution: "",
                            degree: "",
                            fieldOfStudy: "",
                            startDate: "",
                            endDate: "",
                            description: "",
                          });
                        }}
                      >
                        <Plus className="mr-2 h-4 w-4" />
                        Add Education
                      </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[525px]">
                      <DialogHeader>
                        <DialogTitle>
                          {editingItem ? "Edit Education" : "Add Education"}
                        </DialogTitle>
                        <DialogDescription>
                          Add your educational background to showcase your
                          qualifications.
                        </DialogDescription>
                      </DialogHeader>
                      <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <Label htmlFor="institution">Institution</Label>
                            <Input
                              id="institution"
                              value={educationForm.institution}
                              onChange={(e) =>
                                setEducationForm((prev) => ({
                                  ...prev,
                                  institution: e.target.value,
                                }))
                              }
                              placeholder="University name"
                            />
                          </div>
                          <div>
                            <Label htmlFor="degree">Degree</Label>
                            <Input
                              id="degree"
                              value={educationForm.degree}
                              onChange={(e) =>
                                setEducationForm((prev) => ({
                                  ...prev,
                                  degree: e.target.value,
                                }))
                              }
                              placeholder="Bachelor's, Master's, etc."
                            />
                          </div>
                        </div>
                        <div>
                          <Label htmlFor="fieldOfStudy">Field of Study</Label>
                          <Input
                            id="fieldOfStudy"
                            value={educationForm.fieldOfStudy}
                            onChange={(e) =>
                              setEducationForm((prev) => ({
                                ...prev,
                                fieldOfStudy: e.target.value,
                              }))
                            }
                            placeholder="Computer Science, Business, etc."
                          />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <Label htmlFor="startDate">Start Date</Label>
                            <Input
                              id="startDate"
                              type="date"
                              value={educationForm.startDate}
                              onChange={(e) =>
                                setEducationForm((prev) => ({
                                  ...prev,
                                  startDate: e.target.value,
                                }))
                              }
                            />
                          </div>
                          <div>
                            <Label htmlFor="endDate">End Date</Label>
                            <Input
                              id="endDate"
                              type="date"
                              value={educationForm.endDate}
                              onChange={(e) =>
                                setEducationForm((prev) => ({
                                  ...prev,
                                  endDate: e.target.value,
                                }))
                              }
                            />
                          </div>
                        </div>
                        <div>
                          <Label htmlFor="description">Description</Label>
                          <Textarea
                            id="description"
                            value={educationForm.description}
                            onChange={(e) =>
                              setEducationForm((prev) => ({
                                ...prev,
                                description: e.target.value,
                              }))
                            }
                            placeholder="Relevant coursework, achievements, etc."
                          />
                        </div>
                      </div>
                      <DialogFooter>
                        <Button
                          type="submit"
                          onClick={
                            editingItem
                              ? handleUpdateEducation
                              : handleAddEducation
                          }
                        >
                          {editingItem ? "Update Education" : "Add Education"}
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                </CardHeader>
                <CardContent>
                  {profile?.education.length === 0 ? (
                    <p className="text-muted-foreground text-center py-8">
                      No education added yet
                    </p>
                  ) : (
                    <div className="space-y-4">
                      {profile?.education.map((edu) => (
                        <div key={edu.id} className="border rounded-lg p-4">
                          <div className="flex justify-between items-start">
                            <div className="flex-1">
                              <h3 className="font-semibold">{edu.degree}</h3>
                              <p className="text-muted-foreground">
                                {edu.institution}
                              </p>
                              <p className="text-sm text-muted-foreground">
                                {edu.fieldOfStudy}
                              </p>
                              <p className="text-sm text-muted-foreground">
                                {edu.startDate} - {edu.endDate || "Present"}
                              </p>
                              {edu.description && (
                                <p className="text-sm mt-2">
                                  {edu.description}
                                </p>
                              )}
                            </div>
                            <div className="flex gap-2">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => startEditingEducation(edu)}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                className="text-red-600"
                                onClick={() => handleDeleteEducation(edu.id)}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          )}

          {/* Experience Tab - Only for Job Seekers */}
          {user.role !== "EMPLOYER" && (
            <TabsContent value="experience">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle>Work Experience</CardTitle>
                  <Dialog
                    open={showExperienceDialog}
                    onOpenChange={setShowExperienceDialog}
                  >
                    <DialogTrigger asChild>
                      <Button
                        size="sm"
                        onClick={() => {
                          setEditingItem(null);
                          setExperienceForm({
                            company: "",
                            position: "",
                            startDate: "",
                            endDate: "",
                            description: "",
                            current: false,
                          });
                        }}
                      >
                        <Plus className="mr-2 h-4 w-4" />
                        Add Experience
                      </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[525px]">
                      <DialogHeader>
                        <DialogTitle>
                          {editingItem ? "Edit Experience" : "Add Experience"}
                        </DialogTitle>
                        <DialogDescription>
                          Add your work experience to showcase your professional
                          background.
                        </DialogDescription>
                      </DialogHeader>
                      <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <Label htmlFor="company">Company</Label>
                            <Input
                              id="company"
                              value={experienceForm.company}
                              onChange={(e) =>
                                setExperienceForm((prev) => ({
                                  ...prev,
                                  company: e.target.value,
                                }))
                              }
                              placeholder="Company name"
                            />
                          </div>
                          <div>
                            <Label htmlFor="position">Position</Label>
                            <Input
                              id="position"
                              value={experienceForm.position}
                              onChange={(e) =>
                                setExperienceForm((prev) => ({
                                  ...prev,
                                  position: e.target.value,
                                }))
                              }
                              placeholder="Job title"
                            />
                          </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <Label htmlFor="startDate">Start Date</Label>
                            <Input
                              id="startDate"
                              type="date"
                              value={experienceForm.startDate}
                              onChange={(e) =>
                                setExperienceForm((prev) => ({
                                  ...prev,
                                  startDate: e.target.value,
                                }))
                              }
                            />
                          </div>
                          <div>
                            <Label htmlFor="endDate">End Date</Label>
                            <Input
                              id="endDate"
                              type="date"
                              value={experienceForm.endDate}
                              onChange={(e) =>
                                setExperienceForm((prev) => ({
                                  ...prev,
                                  endDate: e.target.value,
                                }))
                              }
                              disabled={experienceForm.current}
                            />
                          </div>
                        </div>
                        <div className="flex items-center space-x-2">
                          <Checkbox
                            id="current"
                            checked={experienceForm.current}
                            onCheckedChange={(checked) =>
                              setExperienceForm((prev) => ({
                                ...prev,
                                current: checked === true,
                                endDate: checked ? "" : prev.endDate,
                              }))
                            }
                          />
                          <Label htmlFor="current">I currently work here</Label>
                        </div>
                        <div>
                          <Label htmlFor="description">Description</Label>
                          <Textarea
                            id="description"
                            value={experienceForm.description}
                            onChange={(e) =>
                              setExperienceForm((prev) => ({
                                ...prev,
                                description: e.target.value,
                              }))
                            }
                            placeholder="Describe your responsibilities and achievements..."
                          />
                        </div>
                      </div>
                      <DialogFooter>
                        <Button
                          type="submit"
                          onClick={
                            editingItem
                              ? handleUpdateExperience
                              : handleAddExperience
                          }
                        >
                          {editingItem ? "Update Experience" : "Add Experience"}
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                </CardHeader>
                <CardContent>
                  {profile?.experience.length === 0 ? (
                    <p className="text-muted-foreground text-center py-8">
                      No experience added yet
                    </p>
                  ) : (
                    <div className="space-y-4">
                      {profile?.experience.map((exp) => (
                        <div key={exp.id} className="border rounded-lg p-4">
                          <div className="flex justify-between items-start">
                            <div className="flex-1">
                              <h3 className="font-semibold">{exp.position}</h3>
                              <p className="text-muted-foreground">
                                {exp.company}
                              </p>
                              <p className="text-sm text-muted-foreground">
                                {exp.startDate} -{" "}
                                {exp.current ? "Present" : exp.endDate}
                              </p>
                              {exp.description && (
                                <p className="text-sm mt-2">
                                  {exp.description}
                                </p>
                              )}
                            </div>
                            <div className="flex gap-2">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => startEditingExperience(exp)}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                className="text-red-600"
                                onClick={() => handleDeleteExperience(exp.id)}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          )}

          {/* Portfolio Tab - Only for Job Seekers */}
          {user.role !== "EMPLOYER" && (
            <TabsContent value="portfolio">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle>Portfolio</CardTitle>
                  <Dialog
                    open={showPortfolioDialog}
                    onOpenChange={setShowPortfolioDialog}
                  >
                    <DialogTrigger asChild>
                      <Button
                        size="sm"
                        onClick={() => {
                          setEditingItem(null);
                          setPortfolioForm({
                            title: "",
                            description: "",
                            url: "",
                            technologies: [],
                            imageUrl: "",
                          });
                        }}
                      >
                        <Plus className="mr-2 h-4 w-4" />
                        Add Project
                      </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[525px]">
                      <DialogHeader>
                        <DialogTitle>
                          {editingItem
                            ? "Edit Portfolio Item"
                            : "Add Portfolio Item"}
                        </DialogTitle>
                        <DialogDescription>
                          Showcase your projects and work to potential
                          employers.
                        </DialogDescription>
                      </DialogHeader>
                      <div className="grid gap-4 py-4">
                        <div>
                          <Label htmlFor="title">Project Title</Label>
                          <Input
                            id="title"
                            value={portfolioForm.title}
                            onChange={(e) =>
                              setPortfolioForm((prev) => ({
                                ...prev,
                                title: e.target.value,
                              }))
                            }
                            placeholder="Project name"
                          />
                        </div>
                        <div>
                          <Label htmlFor="description">Description</Label>
                          <Textarea
                            id="description"
                            value={portfolioForm.description}
                            onChange={(e) =>
                              setPortfolioForm((prev) => ({
                                ...prev,
                                description: e.target.value,
                              }))
                            }
                            placeholder="Describe your project..."
                          />
                        </div>
                        <div>
                          <Label htmlFor="url">Project URL</Label>
                          <Input
                            id="url"
                            value={portfolioForm.url}
                            onChange={(e) =>
                              setPortfolioForm((prev) => ({
                                ...prev,
                                url: e.target.value,
                              }))
                            }
                            placeholder="https://your-project.com"
                          />
                        </div>
                        <div>
                          <Label htmlFor="technologies">Technologies</Label>
                          <Input
                            id="technologies"
                            value={portfolioForm.technologies.join(", ")}
                            onChange={(e) =>
                              setPortfolioForm((prev) => ({
                                ...prev,
                                technologies: e.target.value
                                  .split(",")
                                  .map((tech) => tech.trim())
                                  .filter((tech) => tech),
                              }))
                            }
                            placeholder="React, Node.js, MongoDB (comma-separated)"
                          />
                        </div>
                        <div>
                          <Label htmlFor="imageUrl">Image URL</Label>
                          <Input
                            id="imageUrl"
                            value={portfolioForm.imageUrl}
                            onChange={(e) =>
                              setPortfolioForm((prev) => ({
                                ...prev,
                                imageUrl: e.target.value,
                              }))
                            }
                            placeholder="https://image-url.com/screenshot.png"
                          />
                        </div>
                      </div>
                      <DialogFooter>
                        <Button
                          type="submit"
                          onClick={
                            editingItem
                              ? handleUpdatePortfolio
                              : handleAddPortfolio
                          }
                        >
                          {editingItem
                            ? "Update Portfolio Item"
                            : "Add Portfolio Item"}
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                </CardHeader>
                <CardContent>
                  {profile?.portfolio.length === 0 ? (
                    <p className="text-muted-foreground text-center py-8">
                      No portfolio items added yet
                    </p>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {profile?.portfolio.map((item) => (
                        <div key={item.id} className="border rounded-lg p-4">
                          <div className="flex justify-between items-start mb-2">
                            <h3 className="font-semibold">{item.title}</h3>
                            <div className="flex gap-2">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => startEditingPortfolio(item)}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                className="text-red-600"
                                onClick={() => handleDeletePortfolio(item.id)}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </div>
                          </div>
                          <p className="text-sm text-muted-foreground mb-3">
                            {item.description}
                          </p>
                          {item.url && (
                            <a
                              href={item.url}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="text-primary text-sm hover:underline"
                            >
                              View Project
                            </a>
                          )}
                          {item.technologies.length > 0 && (
                            <div className="flex flex-wrap gap-1 mt-3">
                              {item.technologies.map((tech, index) => (
                                <Badge
                                  key={index}
                                  variant="outline"
                                  className="text-xs"
                                >
                                  {tech}
                                </Badge>
                              ))}
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          )}

          {/* Company Profile Tab - Only for Employers */}
          {user.role === "EMPLOYER" && (
            <TabsContent value="company" className="space-y-6">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Building2 className="h-5 w-5" />
                    Company Information
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div>
                      <Label htmlFor="companyName">Company Name *</Label>
                      <Input
                        id="companyName"
                        value={employerForm.companyName}
                        onChange={(e) =>
                          setEmployerForm((prev) => ({
                            ...prev,
                            companyName: e.target.value,
                          }))
                        }
                        placeholder="Enter your company name"
                        className="mt-1"
                      />
                    </div>

                    <div>
                      <Label htmlFor="companyDescription">
                        Company Description
                      </Label>
                      <Textarea
                        id="companyDescription"
                        value={employerForm.companyDescription}
                        onChange={(e) =>
                          setEmployerForm((prev) => ({
                            ...prev,
                            companyDescription: e.target.value,
                          }))
                        }
                        placeholder="Describe your company, mission, and values..."
                        className="mt-1 min-h-[100px]"
                      />
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <Label
                          htmlFor="companyWebsite"
                          className="flex items-center gap-2"
                        >
                          <Globe className="h-4 w-4" />
                          Company Website
                        </Label>
                        <Input
                          id="companyWebsite"
                          type="url"
                          value={employerForm.companyWebsite}
                          onChange={(e) =>
                            setEmployerForm((prev) => ({
                              ...prev,
                              companyWebsite: e.target.value,
                            }))
                          }
                          placeholder="https://www.company.com"
                          className="mt-1"
                        />
                      </div>

                      <div>
                        <Label
                          htmlFor="contactPhone"
                          className="flex items-center gap-2"
                        >
                          <Phone className="h-4 w-4" />
                          Contact Phone
                        </Label>
                        <Input
                          id="contactPhone"
                          type="tel"
                          value={employerForm.contactPhone}
                          onChange={(e) =>
                            setEmployerForm((prev) => ({
                              ...prev,
                              contactPhone: e.target.value,
                            }))
                          }
                          placeholder="+1 (555) 123-4567"
                          className="mt-1"
                        />
                      </div>
                    </div>

                    <div>
                      <Label
                        htmlFor="companyLocation"
                        className="flex items-center gap-2"
                      >
                        <MapPin className="h-4 w-4" />
                        Company Location
                      </Label>
                      <Input
                        id="companyLocation"
                        value={employerForm.companyLocation}
                        onChange={(e) =>
                          setEmployerForm((prev) => ({
                            ...prev,
                            companyLocation: e.target.value,
                          }))
                        }
                        placeholder="City, State, Country"
                        className="mt-1"
                      />
                    </div>

                    <div className="flex justify-end pt-4">
                      <Button
                        onClick={handleUpdateEmployerProfile}
                        disabled={isUpdatingProfile}
                        className="flex items-center gap-2"
                      >
                        {isUpdatingProfile ? (
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                        ) : (
                          <Save className="h-4 w-4" />
                        )}
                        {isUpdatingProfile ? "Saving..." : "Save Changes"}
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Company Statistics */}
              <Card>
                <CardHeader>
                  <CardTitle>Company Statistics</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="text-center p-4 border rounded-lg">
                      <div className="text-2xl font-bold text-primary">
                        {user.companyName ? "✓" : "–"}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        Company Name
                      </div>
                    </div>
                    <div className="text-center p-4 border rounded-lg">
                      <div className="text-2xl font-bold text-primary">
                        {user.companyWebsite ? "✓" : "–"}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        Website
                      </div>
                    </div>
                    <div className="text-center p-4 border rounded-lg">
                      <div className="text-2xl font-bold text-primary">
                        {user.companyLocation ? "✓" : "–"}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        Location
                      </div>
                    </div>
                  </div>
                  <div className="text-sm text-muted-foreground text-center mt-4">
                    Complete your profile to attract more candidates
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          )}
        </Tabs>
      </div>
    </DashboardLayout>
  );
}
