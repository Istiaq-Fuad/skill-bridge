package org.jobai.skillbridge.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_tech_stack")
public class JobTechStack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private JobPost job;

    private String technology;
    private String category; // Frontend, Backend, Database, etc.
    private String proficiencyLevel; // Required, Preferred, Nice-to-have

    public JobTechStack() {
    }

    public JobTechStack(JobPost job, String technology, String category, String proficiencyLevel) {
        this.job = job;
        this.technology = technology;
        this.category = category;
        this.proficiencyLevel = proficiencyLevel;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobPost getJob() {
        return job;
    }

    public void setJob(JobPost job) {
        this.job = job;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }
}