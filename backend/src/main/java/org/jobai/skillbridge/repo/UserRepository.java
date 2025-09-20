package org.jobai.skillbridge.repo;

import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    List<User> findByRole(UserRole role);

    List<User> findByRoleAndIsActive(UserRole role, boolean isActive);
}