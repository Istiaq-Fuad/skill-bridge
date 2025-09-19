package org.jobai.skillbridge.repo;

import org.jobai.skillbridge.model.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepo extends JpaRepository<JobPost, Integer> {
    // Legacy search method
    public List<JobPost> findByPostProfileContainingIgnoreCaseOrPostDescContainingIgnoreCase(String postProfile,
            String postDesc);

    // New search methods for updated model
    public List<JobPost> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCompanyContainingIgnoreCase(
            String title, String description, String company);

    public List<JobPost> findByEmployerId(Integer employerId);
}