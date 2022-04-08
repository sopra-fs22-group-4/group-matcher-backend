package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.model.*;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import ch.uzh.soprafs22.groupmatcher.repository.TeamRepository;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
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
    private TeamRepository teamRepository;

    @Autowired
    private MatcherService matcherService;

    private Matcher testMatcher;

    private Student createStudent(Long studentId) {
        Student student = new Student();
        student.setId(studentId);
        student.setEmail("test-%s@email.com".formatted(studentId));
        return student;
    }

    private Question createQuestion(Long questionId, Integer numAnswers) {
        Question question = new Question();
        question.setId(questionId);
        question.setMatcher(testMatcher);
        question.setAnswers(IntStream.range(0, numAnswers).mapToObj(num -> new Answer()).toList());
        return question;
    }

    @Test
    void createMatcher_successful() {
        MatcherDTO testMatcherDTO = new MatcherDTO();
        testMatcherDTO.setName("Test Matcher");
        testMatcherDTO.setGroupSize(5);
        given(matcherRepository.save(any(Matcher.class))).willAnswer(returnsFirstArg());
        Matcher createdMatcher = matcherService.createMatcher(testMatcherDTO);
        verify(matcherRepository, times(1)).save(any());
        assertEquals(testMatcherDTO.getName(), createdMatcher.getName());
        assertEquals(testMatcherDTO.getGroupSize(), createdMatcher.getGroupSize());
    }

    @Test
    void createTeams_successful() {
        testMatcher = new Matcher();
        testMatcher.setId(1L);
        testMatcher.setGroupSize(3);
        Student student1 = createStudent(11L);
        Student student2 = createStudent(12L);
        Student student3 = createStudent(13L);
        Student student4 = createStudent(14L);
        Student student5 = createStudent(15L);
        Student student6 = createStudent(16L);
        Student student7 = createStudent(17L);
        testMatcher.setStudents(Set.of(student1, student2, student3, student4, student5));
        Question question1 = createQuestion(2L, 2);
        Question question2 = createQuestion(4L, 4);
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

        assertEquals(student1.getTeam(), student3.getTeam());
        assertEquals(student3.getTeam(), student4.getTeam());
        assertEquals(student5.getTeam(), student6.getTeam());
        assertEquals(student6.getTeam(), student7.getTeam());
        assertNotEquals(student1.getTeam(), student7.getTeam());
        assertNull(student2.getTeam());
    }
}
