package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByIdInAndQuestion_Matcher_Id(List<Long> answerIds, Long matcherId);

    List<Answer> findByQuestion_Matcher_IdAndQuestion_QuestionTypeOrderByIdAsc(Long id, QuestionType questionType);
}
