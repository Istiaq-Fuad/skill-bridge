package org.jobai.skillbridge.repo;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.JobTechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobTechStackRepository extends JpaRepository<JobTechStack, Long> {
    List<JobTechStack> findByJobPost(JobPost jobPost);
    void deleteByJobPost(JobPost jobPost);
}