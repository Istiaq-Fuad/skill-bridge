package org.jobai.skillbridge.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "admin_profiles")
public class AdminProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private String department;
    private String accessLevel;
    private String specialPermissions;

    public AdminProfile() {
    }

    public AdminProfile(User user, String department, String accessLevel, String specialPermissions) {
        this.user = user;
        this.department = department;
        this.accessLevel = accessLevel;
        this.specialPermissions = specialPermissions;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getSpecialPermissions() {
        return specialPermissions;
    }

    public void setSpecialPermissions(String specialPermissions) {
        this.specialPermissions = specialPermissions;
    }
}