package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> getByMatcherIdAndEmail(Long matcherId, String email);

    boolean existsByMatcherIdAndEmail(Long matcherId, String email);

    Integer countByMatcher_Collaborators_IdAndSubmissionTimestampBetween(Long adminId, ZonedDateTime start, ZonedDateTime end);

    Integer countByMatcher_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(Long matcherId);
}