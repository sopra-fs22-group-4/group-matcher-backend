package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Question;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.AnswerRepository;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import ch.uzh.soprafs22.groupmatcher.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {MatcherService.class})
class MatcherServiceTest {

    @MockBean
    private MatcherRepository matcherRepository;

    @MockBean
    private StudentRepository studentRepository;
    @MockBean
    private AnswerRepository answerRepository;

    @MockBean
    private TeamRepository teamRepository;

    @Autowired
    private MatcherService matcherService;

    private Matcher testMatcher;
    private Student testStudent;

    @BeforeEach
    public void setup() {
        testMatcher = TestingUtils.createMatcher();
        testStudent = testMatcher.getStudents().get(0);
        given(matcherRepository.save(any(Matcher.class))).willAnswer(returnsFirstArg());
        given(studentRepository.save(any(Student.class))).willAnswer(returnsFirstArg());
    }

    @Test
    void findStudent_valid() {
        UserDTO testStudentDTO = TestingUtils.convertToDTO(testStudent);
        given(studentRepository.getByMatcherIdAndEmail(testMatcher.getId(), testStudent.getEmail())).willReturn(Optional.of(testStudent));
        Student storedStudent = matcherService.findMatcherStudent(testMatcher.getId(), testStudentDTO);
        assertEquals(testStudentDTO.getName(), storedStudent.getName());
        assertEquals(testStudentDTO.getEmail(), storedStudent.getEmail());
    }

    @Test
    void findStudent_invalid() {
        UserDTO testStudentDTO = TestingUtils.convertToDTO(testStudent);
        Long matcherId = testMatcher.getId();
        given(studentRepository.findByMatcherIdAndEmail(matcherId, testStudent.getEmail())).willReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> matcherService.findMatcherStudent(matcherId, testStudentDTO));
    }

    @Test
    void submitStudentAnswers_valid(){
        assertEquals(2, testMatcher.getQuestions().size());
        Answer selectedAnswer1 = testMatcher.getQuestions().get(0).getAnswers().get(0);
        Answer selectedAnswer2 = testMatcher.getQuestions().get(1).getAnswers().get(0);
        List<Answer> selectedAnswers = List.of(selectedAnswer1, selectedAnswer2);
        List<Long> selectedAnswersIds = selectedAnswers.stream().map(Answer::getId).toList();
        given(studentRepository.getByMatcherIdAndEmail(testMatcher.getId(), testStudent.getEmail())).willReturn(Optional.of(testStudent));
        given(answerRepository.findByIdInAndQuestion_Matcher_Id(selectedAnswersIds, testMatcher.getId())).willReturn(selectedAnswers);
        assertTrue(testStudent.getSelectedAnswers().isEmpty());
        Student storedStudent = matcherService.submitStudentAnswers(testMatcher.getId(), testStudent.getEmail(), selectedAnswersIds);
        assertEquals(selectedAnswers, storedStudent.getSelectedAnswers());
    }

    @Test
    void submitStudentAnswers_invalid(){
        List<Long> answerIds = List.of(404L);
        Long matcherId = testMatcher.getId();
        String studentEmail = testStudent.getEmail();
        given(answerRepository.findByIdInAndQuestion_Matcher_Id(answerIds, testMatcher.getId())).willReturn(List.of());
        assertThrows(ResponseStatusException.class,() -> matcherService.submitStudentAnswers(matcherId, studentEmail, answerIds));
    }

    @Test
    void queryAnswerMatrix_successful() {
        assertEquals(2, testMatcher.getQuestions().size());
        assertEquals(2, testMatcher.getQuestions().get(0).getAnswers().size());
        assertEquals(4, testMatcher.getQuestions().get(1).getAnswers().size());
        assertEquals(3, testMatcher.getStudents().size());

        Question question1 = testMatcher.getQuestions().get(0);
        Question question2 = testMatcher.getQuestions().get(1);
        List<Answer> selectedAnswers1 = List.of(question1.getAnswers().get(0), question2.getAnswers().get(2), question2.getAnswers().get(3));
        List<Answer> selectedAnswers2 = List.of(question1.getAnswers().get(1), question2.getAnswers().get(0), question2.getAnswers().get(3));
        List<Answer> selectedAnswers3 = List.of(question1.getAnswers().get(1), question2.getAnswers().get(2));
        testMatcher.getStudents().get(0).setSelectedAnswers(selectedAnswers1);
        testMatcher.getStudents().get(1).setSelectedAnswers(selectedAnswers2);
        testMatcher.getStudents().get(2).setSelectedAnswers(selectedAnswers3);

        /*                     Expected matrix representation:
                         Q1-A1   Q1-A2   Q2-A1   Q2-A2   Q2-A3   Q2-A4
            Student 1     2.0     0.0     0.0     0.0     4.0     4.0
            Student 2     0.0     2.0     4.0     0.0     0.0     4.0
            Student 3     0.0     2.0     0.0     0.0     4.0     0.0
         */

        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        double[][] returnedMatrix = matcherService.createAnswerMatrixAll(testMatcher.getId());
        double[][] expectedMatrix = {{2.0, 0.0, 0.0, 0.0, 4.0, 4.0}, {0.0, 2.0, 4.0, 0.0, 0.0, 4.0}, {0.0, 2.0, 0.0, 0.0, 4.0, 0.0}};
        assertArrayEquals(expectedMatrix, returnedMatrix);
    }
}