package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudentRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void findLatestSubmissionByMatcherId_successful() {
        int expectedNumSubmissions = 4;
        Matcher testMatcher = new Matcher();
        Admin testAdmin = TestingUtils.createAdmin(null, testMatcher);
        testMatcher.getCollaborators().add(testAdmin);
        IntStream.range(0, expectedNumSubmissions).forEach(index -> {
            Student newStudent = TestingUtils.createStudent(null, testMatcher);
            newStudent.setSubmissionTimestamp(ZonedDateTime.now().minus(index, ChronoUnit.HOURS));
            testMatcher.getStudents().add(newStudent);
        });
        adminRepository.save(testAdmin);
        ZonedDateTime start = ZonedDateTime.now().minus(expectedNumSubmissions, ChronoUnit.DAYS);
        Integer numSubmissions = studentRepository.countByMatcher_Collaborators_IdAndSubmissionTimestampBetween(
                testMatcher.getCollaborators().get(0).getId(), start, ZonedDateTime.now());
        assertEquals(expectedNumSubmissions, numSubmissions);
    }

}
