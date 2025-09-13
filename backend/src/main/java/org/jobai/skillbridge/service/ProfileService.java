package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {
    
    @Autowired
    private EducationRepository educationRepository;
    
    @Autowired
    private ExperienceRepository experienceRepository;
    
    @Autowired
    private SkillRepository skillRepository;
    
    @Autowired
    private PortfolioRepository portfolioRepository;
    
    // Education methods
    public List<Education> getUserEducations(User user) {
        return educationRepository.findByUser(user);
    }
    
    public Education saveEducation(Education education) {
        return educationRepository.save(education);
    }
    
    public void deleteEducation(Long id) {
        educationRepository.deleteById(id);
    }
    
    // Experience methods
    public List<Experience> getUserExperiences(User user) {
        return experienceRepository.findByUser(user);
    }
    
    public Experience saveExperience(Experience experience) {
        return experienceRepository.save(experience);
    }
    
    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }
    
    // Skill methods
    public List<Skill> getUserSkills(User user) {
        return skillRepository.findByUser(user);
    }
    
    public Skill saveSkill(Skill skill) {
        return skillRepository.save(skill);
    }
    
    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }
    
    // Portfolio methods
    public List<Portfolio> getUserPortfolios(User user) {
        return portfolioRepository.findByUser(user);
    }
    
    public Portfolio savePortfolio(Portfolio portfolio) {
        return portfolioRepository.save(portfolio);
    }
    
    public void deletePortfolio(Long id) {
        portfolioRepository.deleteById(id);
    }
}