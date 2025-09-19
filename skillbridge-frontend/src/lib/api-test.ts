/**
 * Simple API client test to verify data fetching works
 */

import { apiClient } from "./api";

export async function testApiClient() {
  console.log("Testing API client...");

  try {
    // Test public endpoint (jobs list)
    console.log("Fetching jobs...");
    const jobsResponse = await apiClient.getJobs();

    if (jobsResponse.success) {
      console.log(
        "✅ Jobs fetch successful:",
        jobsResponse.data?.length,
        "jobs found"
      );
    } else {
      console.log("❌ Jobs fetch failed:", jobsResponse.error);
    }

    // Test token-based endpoint (will fail without auth)
    console.log("Testing profile endpoint (should fail without auth)...");
    const profileResponse = await apiClient.getProfile();

    if (profileResponse.success) {
      console.log("✅ Profile fetch successful (unexpected)");
    } else {
      console.log(
        "✅ Profile fetch failed as expected:",
        profileResponse.error
      );
    }

    return {
      jobsWorking: jobsResponse.success,
      authRequired: !profileResponse.success,
    };
  } catch (error) {
    console.error("❌ API client test failed:", error);
    return {
      jobsWorking: false,
      authRequired: false,
      error: error instanceof Error ? error.message : "Unknown error",
    };
  }
}

// Export for use in components or pages
export default testApiClient;
