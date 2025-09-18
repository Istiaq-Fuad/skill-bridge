package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.EmployerProfile;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.repo.EmployerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployerProfileService {
    
    @Autowired
    private EmployerProfileRepository employerProfileRepository;
    
    public Optional<EmployerProfile> getEmployerProfileByUser(User user) {
        return employerProfileRepository.findByUser(user);
    }
    
    public EmployerProfile saveEmployerProfile(EmployerProfile employerProfile) {
        return employerProfileRepository.save(employerProfile);
    }
    
    public void deleteEmployerProfile(Long id) {
        employerProfileRepository.deleteById(id);
    }
    
    public Optional<EmployerProfile> getEmployerProfileByCompanyName(String companyName) {
        return employerProfileRepository.findByCompanyName(companyName);
    }
}