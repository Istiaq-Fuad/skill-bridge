package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.repo.JobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {
    @Autowired
    public JobRepo repo;

    public List<JobPost> getJobs() {
        return repo.findAll();
    }

    public JobPost getJob(Long id) {
        return repo.findById(id).orElse(null);
    }

    public JobPost addJob(JobPost job) {
        job.setPostedAt(LocalDateTime.now());
        return repo.save(job);
    }

    public JobPost updateJob(JobPost job) {
        return repo.save(job);
    }

    public void deleteJob(Long id) {
        repo.deleteById(id);
    }

    public List<JobPost> searchJobsByKeyword(String keyword) {
        return repo.findByPostProfileContainingIgnoreCaseOrPostDescContainingIgnoreCase(keyword, keyword);
    }
    
    public List<JobPost> getJobsByEmployer(User employer) {
        return repo.findByEmployer(employer);
    }
    
    public List<JobPost> getActiveJobsByEmployer(User employer) {
        return repo.findByEmployerAndJobStatus(employer, "ACTIVE");
    }
}
