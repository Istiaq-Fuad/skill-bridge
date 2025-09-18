package org.jobai.skillbridge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employer_profiles")
public class EmployerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    
    private String companyName;
    private String companyWebsite;
    private String companyDescription;
    private String industry;
    private String companySize;
    private String headquarters;
    private String foundedYear;
    private String companyLogoUrl;
    
    @Column(length = 1000)
    private String missionStatement;
}