package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.repo.JobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {
    @Autowired
    public JobRepo repo;

    public List<JobPost> getJobs() {
        return repo.findAll();
    }

    public JobPost getJob(int id) {
        return repo.findById(id).orElse(null);
    }

    public JobPost saveJob(JobPost job) {
        return repo.save(job);
    }

    public void addJob(JobPost job) {
        repo.save(job);
    }

    public void updateJob(JobPost job) {
        repo.save(job);
    }

    public void deleteJob(int id) {
        repo.deleteById(id);
    }

    public List<JobPost> searchJobsByKeyword(String keyword) {
        // Try new fields first, fallback to legacy fields
        List<JobPost> results = repo
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCompanyContainingIgnoreCase(keyword,
                        keyword, keyword);
        if (results.isEmpty()) {
            results = repo.findByPostProfileContainingIgnoreCaseOrPostDescContainingIgnoreCase(keyword, keyword);
        }
        return results;
    }
}
