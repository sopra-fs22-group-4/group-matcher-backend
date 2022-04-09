package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@SpringBootTest
class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @MockBean
    private StudentRepository studentRepository;

    private final List<Student> testStudents = new ArrayList<>();
    private Matcher testMatcher;


    @BeforeEach
    public void setup() {
        testMatcher = new Matcher();
        testMatcher.setId(10L);
        for(int i=0; i<3; i++){
            Student testStudent = TestingUtils.createStudent((long) i+10,i);
            testStudent.setMatcher(testMatcher);
            testStudents.add(testStudent);
        }
    }

    @Test
    void checkStudentEmail_valid() {
        Long matcherId = testMatcher.getId();
        Student expStudent = testStudents.get(0);
        given(studentRepository.findByMatcher_IdAndEmail(any(Long.class),any(String.class)))
                .willReturn(Optional.of(expStudent));
        Student validStudent = studentService.checkValidEmail(matcherId,expStudent.getEmail());
        assertEquals(expStudent.getEmail(),validStudent.getEmail());
    }

    @Test
    void checkStudentEmail_invalid() {
        Long matcherId = testMatcher.getId();
        given(studentRepository.findByMatcher_IdAndEmail(any(Long.class),any(String.class)))
                .willReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
                ()->studentService.checkValidEmail(matcherId,"test@email.com"));
    }
}