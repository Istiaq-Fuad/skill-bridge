package org.jobai.skillbridge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    private LocalDateTime appliedAt;
    private LocalDateTime lastUpdated;
    private String status; // APPLIED, REVIEWED, INTERVIEW, REJECTED, ACCEPTED
    
    @Column(length = 1000)
    private String coverLetter;
    
    private String resumeUrl;
    
    @Column(length = 1000)
    private String notes;
    
    private String feedback;
    
    @ElementCollection
    private java.util.List<String> interviewStages;
    
    private LocalDateTime interviewScheduledAt;
    
    private String source; // How the candidate found the job (JOB_PORTAL, REFERRAL, etc.)
}