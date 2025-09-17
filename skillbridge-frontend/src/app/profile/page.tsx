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
import { Profile, Skill, apiClient } from "@/lib/api";
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
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);

  const [skillForm, setSkillForm] = useState<Omit<Skill, "id">>({
    name: "",
    level: "BEGINNER",
  });

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
                      <Button>Save Changes</Button>
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
                  <Button size="sm">
                    <Plus className="mr-2 h-4 w-4" />
                    Add Education
                  </Button>
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
                              <Button variant="ghost" size="sm">
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                className="text-red-600"
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
                  <Button size="sm">
                    <Plus className="mr-2 h-4 w-4" />
                    Add Experience
                  </Button>
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
                              <Button variant="ghost" size="sm">
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                className="text-red-600"
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
                  <Button size="sm">
                    <Plus className="mr-2 h-4 w-4" />
                    Add Project
                  </Button>
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
                              <Button variant="ghost" size="sm">
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                className="text-red-600"
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
