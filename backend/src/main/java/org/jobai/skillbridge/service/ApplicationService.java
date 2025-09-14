package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobApplication;
import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.repo.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {
    
    @Autowired
    private JobApplicationRepository applicationRepository;
    
    public List<JobApplication> getUserApplications(User user) {
        return applicationRepository.findByUser(user);
    }
    
    public List<JobApplication> getJobApplications(JobPost jobPost) {
        return applicationRepository.findByJobPost(jobPost);
    }
    
    public JobApplication applyToJob(User user, JobPost jobPost, String coverLetter, String resumeUrl) {
        JobApplication existingApplication = applicationRepository.findByUserAndJobPost(user, jobPost);
        if (existingApplication != null) {
            throw new RuntimeException("User has already applied to this job");
        }
        
        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setJobPost(jobPost);
        application.setAppliedAt(LocalDateTime.now());
        application.setStatus("APPLIED");
        application.setCoverLetter(coverLetter);
        application.setResumeUrl(resumeUrl);
        
        return applicationRepository.save(application);
    }
    
    public JobApplication updateApplicationStatus(Long applicationId, String status) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setStatus(status);
        return applicationRepository.save(application);
    }
    
    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }
}