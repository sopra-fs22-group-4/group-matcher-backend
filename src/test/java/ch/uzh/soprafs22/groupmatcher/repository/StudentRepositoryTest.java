package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.model.projections.Submission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
        testMatcher.getStudents().add(TestingUtils.createStudent(null, testMatcher));
    }

    @Test
    void findLatestSubmissionByAdminId_empty() {
        Admin testAdmin = TestingUtils.createAdmin(null, testMatcher);
        testMatcher.getCollaborators().add(testAdmin);
        Admin storedAdmin = adminRepository.save(testAdmin);
        storedAdmin.getMatchers().forEach(matcher -> matcher.getStudents().forEach(student -> assertNull(student.getSubmissionTimestamp())));
        assertTrue(studentRepository.findByMatcher_Collaborators_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(
                testAdmin.getId(), Pageable.ofSize(10)).isEmpty());
    }

    @Test
    void findLatestSubmissionByAdminId_successful() {
        AtomicInteger counter = new AtomicInteger(1);
        testMatcher.getStudents().forEach(student -> student.setSubmissionTimestamp(
                ZonedDateTime.now().minus(counter.getAndIncrement(), ChronoUnit.HOURS)));
        Admin testAdmin = TestingUtils.createAdmin(null, testMatcher);
        testMatcher.getCollaborators().add(testAdmin);
        Admin storedAdmin = adminRepository.save(testAdmin);
        List<Student> expectedStudents = storedAdmin.getMatchers().stream().map(matcher -> matcher.getStudents()
                        .stream().sorted(Comparator.comparing(Student::getSubmissionTimestamp).reversed()).toList()).toList().get(0);
        List<Submission> testSubmissions = studentRepository.findByMatcher_Collaborators_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(
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

    @Test
    void findLatestSubmissionByMatcherId_empty() {
        Matcher storedMatcher = matcherRepository.save(testMatcher);
        storedMatcher.getStudents().forEach(student -> assertNull(student.getSubmissionTimestamp()));
        assertTrue(studentRepository.findByMatcher_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(
                storedMatcher.getId(), Pageable.ofSize(10)).isEmpty());
    }

    @Test
    void findLatestSubmissionByMatcherId_successful() {

        AtomicInteger counter = new AtomicInteger(1);

        testMatcher.getStudents().forEach(student -> student.setSubmissionTimestamp(
                ZonedDateTime.now().minus(counter.getAndIncrement(), ChronoUnit.HOURS)));

        Matcher storedMatcher = matcherRepository.save(testMatcher);

        List<Student> expectedStudents = storedMatcher.getStudents()
                .stream().sorted(Comparator.comparing(Student::getSubmissionTimestamp).reversed()).toList();

        List<Submission> testSubmissions = studentRepository.findByMatcher_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(
                storedMatcher.getId(), Pageable.ofSize(10));

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
