package org.jobai.skillbridge.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonIgnore
    private JobPost jobPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private LocalDateTime appliedAt;
    private String status; // APPLIED, REVIEWED, INTERVIEW, REJECTED, ACCEPTED

    @Column(length = 1000)
    private String coverLetter;

    private String resumeUrl;

    // Expose jobId for frontend without exposing the full jobPost object
    @JsonProperty("jobId")
    public Integer getJobId() {
        return jobPost != null ? jobPost.getId() : null;
    }

    // Expose userId for frontend without exposing the full user object
    @JsonProperty("userId")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}