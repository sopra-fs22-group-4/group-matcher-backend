package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Question;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.model.projections.StudentOverview;
import ch.uzh.soprafs22.groupmatcher.repository.AnswerRepository;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import ch.uzh.soprafs22.groupmatcher.repository.TeamRepository;
import org.apache.commons.math3.util.CombinatoricsUtils;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        testMatcher = new Matcher();
        testMatcher.setId(1L);
        testMatcher.setGroupSize(3);
        Question testQuestion = TestingUtils.createQuestion(2L, 1);
        testMatcher.setQuestions(List.of(testQuestion));
        testStudent = TestingUtils.createStudent(3L, 3);
        testStudent.setMatcher(testMatcher);
        testMatcher.getStudents().add(testStudent);
        given(matcherRepository.save(any(Matcher.class))).willAnswer(returnsFirstArg());
        given(studentRepository.save(any(Student.class))).willAnswer(returnsFirstArg());
    }

    @Test
    void createTeams_successful() {
        Student student1 = TestingUtils.createStudent(11L,0);
        Student student2 = TestingUtils.createStudent(12L,1);
        Student student3 = TestingUtils.createStudent(13L,2);
        Student student4 = TestingUtils.createStudent(14L,3);
        Student student5 = TestingUtils.createStudent(15L,4);
        Student student6 = TestingUtils.createStudent(16L,5);
        Student student7 = TestingUtils.createStudent(17L,6);
        testMatcher.setStudents(Set.of(student1, student2, student3, student4, student5, student6, student7));
        Question question1 = TestingUtils.createQuestion(2L, 2);
        Question question2 = TestingUtils.createQuestion(4L, 4);
        question1.setMatcher(testMatcher);
        question2.setMatcher(testMatcher);
        testMatcher.setQuestions(List.of(question1, question2));

        int numTeams = testMatcher.getStudents().size() / testMatcher.getGroupSize();
        int numTeamCombinations = (int) CombinatoricsUtils.binomialCoefficient(testMatcher.getStudents().size(), testMatcher.getGroupSize());
        int numCallsToCountMostCommonAnswer = numTeamCombinations * testMatcher.getQuestions().size();

        given(studentRepository.countMostCommonAnswer(anyLong(), anySet())).willReturn(1);

        Set<Long> maxScoreTeam = Set.of(student1.getId(), student3.getId(), student4.getId());
        given(studentRepository.countMostCommonAnswer(question1.getId(), maxScoreTeam)).willReturn(testMatcher.getGroupSize());
        given(studentRepository.countMostCommonAnswer(question2.getId(), maxScoreTeam)).willReturn(testMatcher.getGroupSize());

        Set<Long> maxScoreTeamWithAssignedMember = Set.of(student2.getId(), student4.getId(), student5.getId());
        given(studentRepository.countMostCommonAnswer(question1.getId(), maxScoreTeamWithAssignedMember)).willReturn(testMatcher.getGroupSize());
        given(studentRepository.countMostCommonAnswer(question2.getId(), maxScoreTeamWithAssignedMember)).willReturn(testMatcher.getGroupSize());

        Set<Long> highScoreTeam = Set.of(student5.getId(), student6.getId(), student7.getId());
        given(studentRepository.countMostCommonAnswer(question1.getId(), highScoreTeam)).willReturn(testMatcher.getGroupSize()-1);
        given(studentRepository.countMostCommonAnswer(question2.getId(), highScoreTeam)).willReturn(testMatcher.getGroupSize());

        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        given(studentRepository.getStudentsWithoutTeam(testMatcher.getId())).willReturn(testMatcher.getStudents()
                .stream().filter(student -> student.getTeam() == null).map(Student::getId).collect(Collectors.toSet()));
        given(studentRepository.existsByIdInAndTeamIsNotNull(anySet())).willAnswer(invocation -> {
            Set<Long> studentsIds = invocation.getArgument(0);
            return testMatcher.getStudents().stream().anyMatch(student -> studentsIds.contains(student.getId()) && student.getTeam() != null);
        });
        given(studentRepository.findByIdIn(anySet())).willAnswer(invocation -> {
            Set<Long> studentsIds = invocation.getArgument(0);
            return testMatcher.getStudents().stream().filter(student -> studentsIds.contains(student.getId())).toList();
        });

        matcherService.createTeams(testMatcher.getId());
        verify(matcherRepository, times(1)).findById(any());
        verify(studentRepository, times(1)).getStudentsWithoutTeam(any());
        verify(studentRepository, times(numCallsToCountMostCommonAnswer)).countMostCommonAnswer(any(), any());
        verify(studentRepository, times(numTeamCombinations)).existsByIdInAndTeamIsNotNull(any());
        verify(studentRepository, times(numTeams)).findByIdIn(any());
        verify(teamRepository, times(numTeams)).save(any());

        assertEquals(numTeams, testMatcher.getTeams().size());
        assertNotNull(student1.getTeam());
        assertNotNull(student3.getTeam());
        assertNotNull(student4.getTeam());
        assertNotNull(student5.getTeam());
        assertNotNull(student6.getTeam());
        assertNotNull(student7.getTeam());
        assertNull(student2.getTeam());

        assertEquals(testMatcher, student1.getTeam().getMatcher());
        assertEquals(testMatcher, student7.getTeam().getMatcher());

        assertEquals(testMatcher.getGroupSize(), student1.getTeam().getStudents().size());
        assertTrue(student1.getTeam().getStudents().contains(student3));
        assertTrue(student1.getTeam().getStudents().contains(student4));

        assertEquals(testMatcher.getGroupSize(), student5.getTeam().getStudents().size());
        assertTrue(student5.getTeam().getStudents().contains(student6));
        assertTrue(student5.getTeam().getStudents().contains(student7));

        Double maxSimilarityScore = testMatcher.getQuestions().stream().mapToDouble(question -> question.getAnswers().size()).sum();
        assertEquals(maxSimilarityScore, student1.getTeam().getSimilarityScore());
        assertTrue(student7.getTeam().getSimilarityScore() < maxSimilarityScore);
    }

    @Test
    void checkStudentEmail_valid() {
        StudentOverview studentOverview = TestingUtils.convertToOverview(testStudent);
        given(studentRepository.findByMatcherIdAndEmail(testMatcher.getId(), testStudent.getEmail())).willReturn(Optional.of(studentOverview));
        StudentOverview storedStudentOverview = matcherService.verifyStudentEmail(testMatcher.getId(), testStudent.getEmail());
        assertEquals(studentOverview, storedStudentOverview);
    }

    @Test
    void checkStudentEmail_invalid() {
        Long matcherId = testMatcher.getId();
        given(studentRepository.findByMatcherIdAndEmail(matcherId, testStudent.getEmail())).willReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> matcherService.verifyStudentEmail(matcherId, "not-a-student@email.com"));
    }

    @Test
    void submitStudentAnswers_valid(){
        Answer testAnswer = testMatcher.getQuestions().get(0).getAnswers().get(0);
        testAnswer.setId(4L);
        given(studentRepository.getByMatcherIdAndEmail(testMatcher.getId(), testStudent.getEmail())).willReturn(Optional.of(testStudent));
        given(answerRepository.findByIdAndQuestion_Matcher_Id(testAnswer.getId(), testMatcher.getId())).willReturn(Optional.of(testAnswer));
        assertTrue(testStudent.getAnswers().isEmpty());
        Student storedStudent = matcherService.submitStudentAnswers(testMatcher.getId(), testStudent.getEmail(), List.of(testAnswer.getId()));
        assertEquals(List.of(testAnswer), storedStudent.getAnswers());
    }

    @Test
    void submitStudentAnswers_invalid(){
        Answer testAnswer = testMatcher.getQuestions().get(0).getAnswers().get(0);
        testAnswer.setId(4L);
        List<Long> answerIds = List.of(testAnswer.getId());
        Long matcherId = testMatcher.getId();
        String studentEmail = testStudent.getEmail();
        given(answerRepository.findByIdAndQuestion_Matcher_Id(testAnswer.getId(), testMatcher.getId())).willReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,() -> matcherService.submitStudentAnswers(matcherId, studentEmail, answerIds));
    }
}