package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByEmail(String Email);
}
