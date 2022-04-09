package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private MatcherRepository matcherRepository;

    private UserDTO testUserDTO;
    private List<Student> testStudents = new ArrayList<>();
    private Matcher testMatcher;
    private Long matcherId;


    @BeforeEach
    public void setup() {
        testMatcher = new Matcher();
        Matcher storedMatcher = matcherRepository.save(testMatcher);
        matcherId = storedMatcher.getId();

        for(int i=10; i<20; i++){
            Student testStudent = TestingUtils.createStudent((long) i);
            testStudent.setMatcher(storedMatcher);
            studentRepository.save(testStudent);
            testStudents.add(testStudent);
        }

        testUserDTO = new UserDTO();
    }

    @Test
    void checkStudentEmail_valid() {
        testUserDTO.setEmail(testStudents.get(0).getEmail());
        Student validStudent = studentService.checkValidEmail(matcherId,testUserDTO);
        assertEquals(testUserDTO.getEmail(),validStudent.getEmail());
    }

    @Test
    void checkStudentEmail_invalid() {
        testUserDTO.setEmail("test@email.com");
        assertThrows(ResponseStatusException.class,
                ()->studentService.checkValidEmail(matcherId,testUserDTO));
    }
}