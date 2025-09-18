package org.jobai.skillbridge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "job_posts")
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;
    
    private String postProfile;
    private String postDesc;
    private Integer reqExperience;
    
    @ElementCollection
    private List<String> postTechStack;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    @JsonIgnore
    private User employer;
    
    private String location;
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
    private Double salaryMin;
    private Double salaryMax;
    private String salaryCurrency;
    private LocalDateTime postedAt;
    private LocalDateTime expiryDate;
    private String jobStatus; // ACTIVE, INACTIVE, EXPIRED, FILLED
}
