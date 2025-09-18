package org.jobai.skillbridge.repo;

import org.jobai.skillbridge.model.JobApplication;
import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUser(User user);
    List<JobApplication> findByJobPost(JobPost jobPost);
    JobApplication findByUserAndJobPost(User user, JobPost jobPost);
    List<JobApplication> findByStatus(String status);
    List<JobApplication> findByJobPostAndStatus(JobPost jobPost, String status);
    List<JobApplication> findByUserAndStatus(User user, String status);
    List<JobApplication> findByJobPostEmployer(User employer);
    
    @Query("SELECT COUNT(j) FROM JobApplication j WHERE j.status = :status")
    long countByStatus(String status);
}