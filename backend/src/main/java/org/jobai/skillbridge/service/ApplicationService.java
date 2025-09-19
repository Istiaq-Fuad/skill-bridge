package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobApplication;
import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.repo.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {
    
    @Autowired
    private JobApplicationRepository applicationRepository;
    
    /**
     * Helper method to set field value using reflection
     * @param obj The object to set the field value on
     * @param fieldName The name of the field
     * @param value The value to set
     */
    private void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            String capitalizedFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            String setterName = "set" + capitalizedFieldName;
            Method method = obj.getClass().getMethod(setterName, value.getClass());
            method.invoke(obj, value);
        } catch (Exception e) {
            System.err.println("Could not set field " + fieldName + " in " + obj.getClass().getName());
            e.printStackTrace();
        }
    }
    
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
        setFieldValue(application, "user", user);
        setFieldValue(application, "jobPost", jobPost);
        setFieldValue(application, "appliedAt", LocalDateTime.now());
        setFieldValue(application, "status", "APPLIED");
        setFieldValue(application, "coverLetter", coverLetter);
        setFieldValue(application, "resumeUrl", resumeUrl);
        
        return applicationRepository.save(application);
    }
    
    public JobApplication updateApplicationStatus(Long applicationId, String status) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        setFieldValue(application, "status", status);
        return applicationRepository.save(application);
    }
    
    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }
}