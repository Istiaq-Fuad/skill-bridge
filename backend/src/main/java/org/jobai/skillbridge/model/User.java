package org.jobai.skillbridge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    private String firstName;
    private String lastName;
    
    @Column(length = 1000)
    private String bio;
    
    private boolean isActive = true;
}

public enum UserRole {
    ANONYMOUS,
    JOB_SEEKER,
    EMPLOYER,
    ADMIN
}