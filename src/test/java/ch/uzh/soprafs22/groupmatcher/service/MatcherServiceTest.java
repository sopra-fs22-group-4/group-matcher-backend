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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {MatcherService.class})
class MatcherServiceTest {

    @MockBean
    private MatcherRepository matcherRepository;

    @MockBean
    private StudentRepository studentRepository;
    @MockBean
    private AnswerRepository answerRepository;

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
        given(studentRepository.getByMatcherIdAndEmail(matcherId, testStudent.getEmail())).willReturn(Optional.empty());
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
    void buildAnswersMatrixForMatcher_successful() {
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

        double[][] returnedMatrix = matcherService.buildAnswersMatrixForMatcher(testMatcher);
        double[][] expectedMatrix = {{2.0, 0.0, 0.0, 0.0, 4.0, 4.0}, {0.0, 2.0, 4.0, 0.0, 0.0, 4.0}, {0.0, 2.0, 0.0, 0.0, 4.0, 0.0}};
        assertArrayEquals(expectedMatrix, returnedMatrix);
    }

    @Test
    void initMatchingTest() {
        Question question1 = testMatcher.getQuestions().get(0);
        Question question2 = testMatcher.getQuestions().get(1);
        List<Answer> selectedAnswers1 = List.of(question1.getAnswers().get(0), question2.getAnswers().get(2), question2.getAnswers().get(3));
        List<Answer> selectedAnswers2 = List.of(question1.getAnswers().get(1), question2.getAnswers().get(0), question2.getAnswers().get(3));
        List<Answer> selectedAnswers3 = List.of(question1.getAnswers().get(1), question2.getAnswers().get(2));
        List<Answer> selectedAnswers4 = List.of(question1.getAnswers().get(1), question2.getAnswers().get(0), question2.getAnswers().get(1));
        List<Answer> selectedAnswers5 = List.of(question1.getAnswers().get(0), question2.getAnswers().get(0), question2.getAnswers().get(2));
        List<Answer> selectedAnswers6 = List.of(question1.getAnswers().get(1), question2.getAnswers().get(1));
        List<Answer> selectedAnswers7 = List.of(question1.getAnswers().get(0), question2.getAnswers().get(1), question2.getAnswers().get(3));
        testMatcher.getStudents().get(0).setSelectedAnswers(selectedAnswers1);
        testMatcher.getStudents().get(1).setSelectedAnswers(selectedAnswers2);
        testMatcher.getStudents().get(2).setSelectedAnswers(selectedAnswers3);
        Student student4 = TestingUtils.createStudent(104L, testMatcher);
        student4.setSelectedAnswers(selectedAnswers4);
        testMatcher.getStudents().add(student4);
        Student student5 = TestingUtils.createStudent(105L, testMatcher);
        student5.setSelectedAnswers(selectedAnswers5);
        testMatcher.getStudents().add(student5);
        Student student6 = TestingUtils.createStudent(106L, testMatcher);
        student6.setSelectedAnswers(selectedAnswers6);
        testMatcher.getStudents().add(student6);
        Student student7 = TestingUtils.createStudent(107L, testMatcher);
        student7.setSelectedAnswers(selectedAnswers7);
        testMatcher.getStudents().add(student7);
        given(matcherRepository.findByDueDateIsAfterAndTeams_Empty(any())).willReturn(List.of(testMatcher));
        given(studentRepository.getByMatcherIdAndEmail(anyLong(), anyString())).willAnswer(invocation ->
                testMatcher.getStudents().stream().filter(student -> student.getEmail().equals(invocation.getArgument(1))).findFirst());
        List<Matcher> returnedMatchers = matcherService.initMatching();
        assertEquals(1, returnedMatchers.size());
        Matcher returnedMatcher = returnedMatchers.get(0);
        assertEquals(testMatcher.getId(), returnedMatcher.getId());
        assertEquals(3, returnedMatcher.getTeams().size());
        assertEquals(2, returnedMatcher.getTeams().get(0).getStudents().size());
        assertEquals(3, returnedMatcher.getTeams().get(1).getStudents().size());
        assertEquals(2, returnedMatcher.getTeams().get(2).getStudents().size());
        Set<Long> expectedTeamStudents1_3 = Set.of(testMatcher.getStudents().get(0).getId(), testMatcher.getStudents().get(2).getId());
        Set<Long> expectedTeamStudents2_7 = Set.of(testMatcher.getStudents().get(1).getId(), testMatcher.getStudents().get(6).getId());
        Set<Long> expectedTeamStudents4_5_6 = Set.of(testMatcher.getStudents().get(3).getId(),
                testMatcher.getStudents().get(4).getId(), testMatcher.getStudents().get(5).getId());
        assertEquals(expectedTeamStudents1_3, returnedMatcher.getStudents().get(0).getTeam().getStudents()
                .stream().map(Student::getId).collect(Collectors.toSet()));
        assertEquals(expectedTeamStudents2_7, returnedMatcher.getStudents().get(1).getTeam().getStudents()
                .stream().map(Student::getId).collect(Collectors.toSet()));
        assertEquals(expectedTeamStudents4_5_6, returnedMatcher.getStudents().get(3).getTeam().getStudents()
                .stream().map(Student::getId).collect(Collectors.toSet()));
    }
}