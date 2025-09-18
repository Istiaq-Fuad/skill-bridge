package org.jobai.skillbridge.repo;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepo extends JpaRepository<JobPost, Long> {
    public List<JobPost> findByPostProfileContainingIgnoreCaseOrPostDescContainingIgnoreCase(String postProfile,
            String postDesc);
    public List<JobPost> findByEmployer(User employer);
    public List<JobPost> findByEmployerAndJobStatus(User employer, String jobStatus);
}