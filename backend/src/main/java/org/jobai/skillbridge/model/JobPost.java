package org.jobai.skillbridge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jobai.skillbridge.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "job_posts")
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;
    
    @Column(name = "title")
    private String postProfile;
    
    @Column(name = "description")
    private String postDesc;
    
    @Column(name = "experience_required")
    private Integer reqExperience;
    
    @Transient
    private List<String> postTechStack;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    @JsonIgnore
    private User employer;
    
    @Column(name = "company")
    private String company;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "employment_type")
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
    
    @Column(name = "salary_min")
    private Double salaryMin;
    
    @Column(name = "salary_max")
    private Double salaryMax;
    
    @Column(name = "salary_currency")
    private String salaryCurrency;
    
    @Column(name = "posted_at")
    private LocalDateTime postedAt;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "job_status")
    private String jobStatus; // ACTIVE, INACTIVE, EXPIRED, FILLED
}
