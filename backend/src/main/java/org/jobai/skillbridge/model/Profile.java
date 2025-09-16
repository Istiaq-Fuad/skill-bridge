package org.jobai.skillbridge.model;

import java.util.List;

public class Profile {
    private Long id;
    private Long userId;
    private String bio;
    private List<Skill> skills;
    private List<Education> education;
    private List<Experience> experience;
    private List<Portfolio> portfolio;

    public Profile() {
    }

    public Profile(Long id, Long userId, String bio, List<Skill> skills, List<Education> education,
            List<Experience> experience, List<Portfolio> portfolio) {
        this.id = id;
        this.userId = userId;
        this.bio = bio;
        this.skills = skills;
        this.education = education;
        this.experience = experience;
        this.portfolio = portfolio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Education> getEducation() {
        return education;
    }

    public void setEducation(List<Education> education) {
        this.education = education;
    }

    public List<Experience> getExperience() {
        return experience;
    }

    public void setExperience(List<Experience> experience) {
        this.experience = experience;
    }

    public List<Portfolio> getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(List<Portfolio> portfolio) {
        this.portfolio = portfolio;
    }
}