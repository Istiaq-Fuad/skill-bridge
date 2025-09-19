package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.JobTechStack;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.repo.JobRepo;
import org.jobai.skillbridge.repo.JobTechStackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class JobService {
    @Autowired
    public JobRepo repo;
    
    @Autowired
    public JobTechStackRepository techStackRepo;

    public List<JobPost> getJobs() {
        List<JobPost> jobs = repo.findAll();
        // Load tech stack for each job
        for (JobPost job : jobs) {
            loadTechStackForJob(job);
        }
        return jobs;
    }

    public JobPost getJob(Long id) {
        JobPost job = repo.findById(id).orElse(null);
        if (job != null) {
            loadTechStackForJob(job);
        }
        return job;
    }

    @Transactional
    public JobPost addJob(JobPost job) {
        job.setPostedAt(LocalDateTime.now());
        JobPost savedJob = repo.save(job);
        
        // Save tech stack if present
        if (job.getPostTechStack() != null) {
            for (String tech : job.getPostTechStack()) {
                JobTechStack jobTechStack = new JobTechStack();
                jobTechStack.setJobPost(savedJob);
                jobTechStack.setTechStack(tech);
                techStackRepo.save(jobTechStack);
            }
        }
        
        // Load tech stack for the saved job
        loadTechStackForJob(savedJob);
        return savedJob;
    }

    @Transactional
    public JobPost updateJob(JobPost job) {
        // Delete existing tech stack
        JobPost existingJob = repo.findById(job.getPostId()).orElse(null);
        if (existingJob != null) {
            techStackRepo.deleteByJobPost(existingJob);
        }
        
        // Save new tech stack if present
        if (job.getPostTechStack() != null) {
            for (String tech : job.getPostTechStack()) {
                JobTechStack jobTechStack = new JobTechStack();
                jobTechStack.setJobPost(job);
                jobTechStack.setTechStack(tech);
                techStackRepo.save(jobTechStack);
            }
        }
        
        JobPost updatedJob = repo.save(job);
        // Load tech stack for the updated job
        loadTechStackForJob(updatedJob);
        return updatedJob;
    }

    @Transactional
    public void deleteJob(Long id) {
        JobPost job = repo.findById(id).orElse(null);
        if (job != null) {
            techStackRepo.deleteByJobPost(job);
            repo.deleteById(id);
        }
    }

    public List<JobPost> searchJobsByKeyword(String keyword) {
        List<JobPost> jobs = repo.findByPostProfileContainingIgnoreCaseOrPostDescContainingIgnoreCase(keyword, keyword);
        // Load tech stack for each job
        for (JobPost job : jobs) {
            loadTechStackForJob(job);
        }
        return jobs;
    }
    
    public List<JobPost> getJobsByEmployer(User employer) {
        List<JobPost> jobs = repo.findByEmployer(employer);
        // Load tech stack for each job
        for (JobPost job : jobs) {
            loadTechStackForJob(job);
        }
        return jobs;
    }
    
    public List<JobPost> getActiveJobsByEmployer(User employer) {
        List<JobPost> jobs = repo.findByEmployerAndJobStatus(employer, "ACTIVE");
        // Load tech stack for each job
        for (JobPost job : jobs) {
            loadTechStackForJob(job);
        }
        return jobs;
    }
    
    /**
     * Load tech stack for a job from the database
     * @param job The job post to load tech stack for
     */
    private void loadTechStackForJob(JobPost job) {
        List<JobTechStack> techStacks = techStackRepo.findByJobPost(job);
        List<String> techStackList = techStacks.stream()
            .map(JobTechStack::getTechStack)
            .collect(Collectors.toList());
        job.setPostTechStack(techStackList);
    }
}
