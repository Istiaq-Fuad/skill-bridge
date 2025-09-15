"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
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
} from "lucide-react";
import { toast } from "sonner";

export default function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("overview");
  const { user, isLoading } = useAuth();
  const router = useRouter();

  // Form states
  const [showSkillDialog, setShowSkillDialog] = useState(false);

  const [skillForm, setSkillForm] = useState<Omit<Skill, "id">>({
    name: "",
    level: "BEGINNER",
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
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="overview" className="flex items-center">
              <User className="mr-2 h-4 w-4" />
              Overview
            </TabsTrigger>
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
          </TabsList>

          {/* Overview Tab */}
          <TabsContent value="overview" className="space-y-6">
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
                      value={profile.bio || ""}
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
                        <Label htmlFor="skill-level">Proficiency Level</Label>
                        <Select
                          value={skillForm.level}
                          onValueChange={(
                            value:
                              | "BEGINNER"
                              | "INTERMEDIATE"
                              | "ADVANCED"
                              | "EXPERT"
                          ) =>
                            setSkillForm((prev) => ({ ...prev, level: value }))
                          }
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="BEGINNER">Beginner</SelectItem>
                            <SelectItem value="INTERMEDIATE">
                              Intermediate
                            </SelectItem>
                            <SelectItem value="ADVANCED">Advanced</SelectItem>
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
                {profile.skills.length === 0 ? (
                  <p className="text-muted-foreground text-center py-8">
                    No skills added yet
                  </p>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {profile.skills.map((skill) => (
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
          </TabsContent>

          {/* Education Tab */}
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
                {profile.education.length === 0 ? (
                  <p className="text-muted-foreground text-center py-8">
                    No education added yet
                  </p>
                ) : (
                  <div className="space-y-4">
                    {profile.education.map((edu) => (
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
                              <p className="text-sm mt-2">{edu.description}</p>
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

          {/* Experience Tab */}
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
                {profile.experience.length === 0 ? (
                  <p className="text-muted-foreground text-center py-8">
                    No experience added yet
                  </p>
                ) : (
                  <div className="space-y-4">
                    {profile.experience.map((exp) => (
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
                              <p className="text-sm mt-2">{exp.description}</p>
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

          {/* Portfolio Tab */}
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
                {profile.portfolio.length === 0 ? (
                  <p className="text-muted-foreground text-center py-8">
                    No portfolio items added yet
                  </p>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {profile.portfolio.map((item) => (
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
        </Tabs>
      </div>
    </DashboardLayout>
  );
}
