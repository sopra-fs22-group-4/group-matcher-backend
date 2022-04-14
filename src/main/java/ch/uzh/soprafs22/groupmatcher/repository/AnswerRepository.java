package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<Answer> findByQuestionIdAndOrdinalNum(Long questionId, Integer ordinalNum);

    Optional<Answer> findByIdAndQuestion_Matcher_Id(Long questionId, Long matcherId);
}
