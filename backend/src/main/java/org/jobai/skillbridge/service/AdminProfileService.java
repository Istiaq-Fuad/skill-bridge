package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.AdminProfile;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.repo.AdminProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminProfileService {
    
    @Autowired
    private AdminProfileRepository adminProfileRepository;
    
    public Optional<AdminProfile> getAdminProfileByUser(User user) {
        return adminProfileRepository.findByUser(user);
    }
    
    public AdminProfile saveAdminProfile(AdminProfile adminProfile) {
        return adminProfileRepository.save(adminProfile);
    }
    
    public void deleteAdminProfile(Long id) {
        adminProfileRepository.deleteById(id);
    }
    
    public Optional<AdminProfile> getAdminProfileByLevel(String adminLevel) {
        return adminProfileRepository.findByAdminLevel(adminLevel);
    }
}