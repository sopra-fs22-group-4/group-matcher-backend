package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.model.QuestionRes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionsResRepository extends JpaRepository<QuestionRes, Long> {
}