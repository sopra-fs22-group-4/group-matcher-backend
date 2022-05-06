package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.model.projections.Submission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> getByMatcherIdAndEmail(Long matcherId, String email);

    boolean existsByMatcherIdAndEmail(Long matcherId, String email);

    List<Submission> findByMatcher_Admins_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(Long adminId, Pageable pageable);

    List<Submission> findByMatcher_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(Long matcherId, Pageable pageable);

    Integer countByMatcher_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(Long matcherId);
}