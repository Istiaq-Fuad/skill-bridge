package org.jobai.skillbridge.repo;

import org.jobai.skillbridge.model.EmployerProfile;
import org.jobai.skillbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployerProfileRepository extends JpaRepository<EmployerProfile, Long> {
    Optional<EmployerProfile> findByUser(User user);
    Optional<EmployerProfile> findByCompanyName(String companyName);
}