package org.jobai.skillbridge.repo;

import org.jobai.skillbridge.model.Skill;
import org.jobai.skillbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByUser(User user);
    List<Skill> findByNameContainingIgnoreCase(String name);
}