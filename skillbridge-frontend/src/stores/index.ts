// Re-export all stores for easy access
export { useAuthStore } from "./auth-store";
export { useJobsStore } from "./jobs-store";
export { useApplicationsStore } from "./applications-store";
export { useProfileStore } from "./profile-store";

// Re-export types
export type {
  User,
  Job,
  JobApplication,
  Profile,
  Skill,
  Education,
  Experience,
  Portfolio,
} from "@/lib/api";
