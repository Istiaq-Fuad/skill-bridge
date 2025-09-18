package org.jobai.skillbridge.repo;

import org.jobai.skillbridge.model.AdminProfile;
import org.jobai.skillbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {
    Optional<AdminProfile> findByUser(User user);
    Optional<AdminProfile> findByAdminLevel(String adminLevel);
}