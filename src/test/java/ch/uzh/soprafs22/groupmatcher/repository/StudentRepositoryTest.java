package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.*;
import ch.uzh.soprafs22.groupmatcher.model.projections.Submission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StudentRepositoryTest {

    @Autowired
    private MatcherRepository matcherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    private Matcher testMatcher;

    @BeforeEach
    void setup() {
        testMatcher = new Matcher();
        testMatcher.setGroupSize(3);
        testMatcher.setCourseName("Test Course");
        Set<Student> testStudents = TestingUtils.createStudents(4);
        testStudents.forEach(student -> student.setMatcher(testMatcher));
        testMatcher.setStudents(testStudents);
        Question question = TestingUtils.createQuestion(null, 4);
        question.setMatcher(testMatcher);
        testMatcher.setQuestions(List.of(question));
    }

    @Transactional
    @Test
    void countMostCommonAnswer_allSameAnswer() {
        Matcher storedMatcher = matcherRepository.save(testMatcher);
        Question question = storedMatcher.getQuestions().get(0);
        Answer mostCommonAnswer = question.getAnswers().get(0);
        storedMatcher.getStudents().forEach(student -> {
            mostCommonAnswer.getStudents().add(student);
            Student storedStudent = studentRepository.findById(student.getId()).orElseThrow(EntityNotFoundException::new);
            storedStudent.getAnswers().add(mostCommonAnswer);
            studentRepository.save(student);
        });
        Set<Long> studentsIds = storedMatcher.getStudents().stream().map(Student::getId).collect(Collectors.toSet());
        assertEquals(studentsIds.size(), studentRepository.countMostCommonAnswer(question.getId(), studentsIds));
    }

    @Transactional
    @Test
    void countMostCommonAnswer_tie() {
        Matcher storedMatcher = matcherRepository.save(testMatcher);
        Question question = storedMatcher.getQuestions().get(0);
        AtomicInteger index = new AtomicInteger();
        storedMatcher.getStudents().forEach(student -> {
            Answer selectedAnswer = question.getAnswers().get(index.getAndIncrement() % 2);
            selectedAnswer.getStudents().add(student);
            Student storedStudent = studentRepository.findById(student.getId()).orElseThrow(EntityNotFoundException::new);
            storedStudent.getAnswers().add(selectedAnswer);
            studentRepository.save(storedStudent);
        });
        Set<Long> studentsIds = storedMatcher.getStudents().stream().map(Student::getId).collect(Collectors.toSet());
        assertEquals(studentsIds.size() / 2, studentRepository.countMostCommonAnswer(question.getId(), studentsIds));
    }

    @Transactional
    @Test
    void countMostCommonAnswer_allDifferentAnswers() {
        Matcher storedMatcher = matcherRepository.save(testMatcher);
        Question question = storedMatcher.getQuestions().get(0);
        AtomicInteger index = new AtomicInteger();
        storedMatcher.getStudents().forEach(student -> {
            Answer selectedAnswer = question.getAnswers().get(index.getAndIncrement());
            selectedAnswer.getStudents().add(student);
            Student storedStudent = studentRepository.findById(student.getId()).orElseThrow(EntityNotFoundException::new);
            storedStudent.getAnswers().add(selectedAnswer);
            studentRepository.save(storedStudent);
        });
        Set<Long> studentsIds = storedMatcher.getStudents().stream().map(Student::getId).collect(Collectors.toSet());
        assertEquals(1, studentRepository.countMostCommonAnswer(question.getId(), studentsIds));
    }

    @Test
    void findByMatcherIdAndStudentsEmail_successful() {
        Matcher storedMatcher = matcherRepository.save(testMatcher);
        storedMatcher.getStudents().forEach(
                student -> assertTrue(studentRepository.findByMatcherIdAndEmail(
                        storedMatcher.getId(),student.getEmail()).isPresent()));
    }

    @Test
    void findByMatcherIdAndStudentsEmail_failed() {
        Matcher storedMatcher = matcherRepository.save(testMatcher);
        Optional<Student> searchedStudent = studentRepository.findByMatcherIdAndEmail(
                storedMatcher.getId(), "test@email.com");
        assertTrue(searchedStudent.isEmpty());
    }

    @Test
    void findLatestSubmissionByAdminId_empty() {
        Admin testAdmin = new Admin();
        testAdmin.setId(101L);
        testAdmin.setEmail("test@email.com");
        testAdmin.setPassword("test");
        testAdmin.setMatchers(Set.of(testMatcher));
        testMatcher.setAdmins(Set.of(testAdmin));
        Admin storedAdmin = adminRepository.save(testAdmin);
        storedAdmin.getMatchers().forEach(matcher -> matcher.getStudents().forEach(student -> assertNull(student.getSubmissionTimestamp())));
        assertTrue(studentRepository.findByMatcher_Admins_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(
                testAdmin.getId(), Pageable.ofSize(10)).isEmpty());
    }

    @Test
    void findLatestSubmissionByAdminId_successful() {
        AtomicInteger counter = new AtomicInteger(1);
        testMatcher.getStudents().forEach(student -> student.setSubmissionTimestamp(
                ZonedDateTime.now().minus(counter.getAndIncrement(), ChronoUnit.HOURS)));
        Admin testAdmin = new Admin();
        testAdmin.setEmail("test@email.com");
        testAdmin.setPassword("test");
        testAdmin.setMatchers(Set.of(testMatcher));
        testMatcher.setAdmins(Set.of(testAdmin));
        Admin storedAdmin = adminRepository.save(testAdmin);
        List<Student> expectedStudents = storedAdmin.getMatchers().stream().map(matcher -> matcher.getStudents()
                        .stream().sorted(Comparator.comparing(Student::getSubmissionTimestamp).reversed()).toList()).toList().get(0);
        List<Submission> testSubmissions = studentRepository.findByMatcher_Admins_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(
                storedAdmin.getId(), Pageable.ofSize(10));
        assertEquals(expectedStudents.size(), testSubmissions.size());
        IntStream.range(0, expectedStudents.size()).forEach(index -> {
            Student expectedStudent = expectedStudents.get(index);
            Submission testSubmission = testSubmissions.get(index);
            assertEquals(expectedStudent.getEmail(), testSubmission.getEmail());
            assertEquals(expectedStudent.getMatcher().getCourseName(), testSubmission.getCourseName());
            assertEquals(expectedStudent.getSubmissionTimestamp().truncatedTo(ChronoUnit.MINUTES),
                    testSubmission.getSubmissionTimestamp().truncatedTo(ChronoUnit.MINUTES));
        });
    }

}
