package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Question;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AnswerRepositoryTest {

    @Autowired
    private AnswerRepository answerRepository;

    private Question testQuestion;
    private Student testStudent;
    private Answer testAnswer;

    @BeforeEach
    void setUp() {
        testQuestion = TestingUtils.createQuestion(1L,3);
        testStudent = TestingUtils.createStudent(1L,0);
        testAnswer = testQuestion.getAnswers().iterator().next();
    }

    @Test
    void findByQuestionIdAndOrdinalNum_successful() {
        testQuestion.getAnswers().forEach(answer -> assertNotNull(
                answerRepository.findByQuestionIdAndOrdinalNum(testQuestion.getId(),answer.getOrdinalNum())));
    }

    @Test
    void findByQuestionIdAndOrdinalNum_fail() {
        Integer numAnswers = testQuestion.getAnswers().size();
        assertTrue(answerRepository.findByQuestionIdAndOrdinalNum(testQuestion.getId(),numAnswers)
                .isEmpty());
    }
}