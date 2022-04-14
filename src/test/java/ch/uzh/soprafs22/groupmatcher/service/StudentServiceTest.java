package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.dto.AnswerDTO;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.AnswerRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

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
    @MockBean
    private AnswerRepository answerRepository;

    private Matcher testMatcher;
    private Student testStudent;
    private Answer testAnswer;


    @BeforeEach
    public void setup() {
        testMatcher = new Matcher();
        testMatcher.setId(10L);
        testAnswer = new Answer();
        testAnswer.setOrdinalNum(0);
        testStudent = TestingUtils.createStudent(null,0);
        testStudent.setMatcher(testMatcher);
    }

    @Test
    void checkStudentEmail_valid() {
        Long matcherId = testMatcher.getId();
        given(studentRepository.findByMatcherIdAndEmail(any(Long.class),any(String.class)))
                .willReturn(Optional.of(testStudent));
        Student validStudent = studentService.checkValidEmail(matcherId,testStudent.getEmail());
        assertEquals(testStudent.getEmail(),validStudent.getEmail());
    }

    @Test
    void checkStudentEmail_invalid() {
        Long matcherId = testMatcher.getId();
        given(studentRepository.findByMatcherIdAndEmail(any(Long.class),any(String.class)))
                .willReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
                ()->studentService.checkValidEmail(matcherId,"test@email.com"));
    }

    @Test
    void updateAnswer_valid(){
        Long studentId  = testStudent.getId();
        Long questionId = 1L;
        List<AnswerDTO> answerDTOs = List.of(new AnswerDTO());

        given(studentRepository.getById(studentId)).willReturn(testStudent);
        given(answerRepository.findByQuestionIdAndOrdinalNum(questionId,answerDTOs.get(0).getOrdinalNum()))
                .willReturn(Optional.ofNullable(testAnswer));

        assertEquals(0, testAnswer.getStudents().size());
        assertEquals(0, testStudent.getAnswers().size());
        studentService.updateAnswer(studentId,questionId,answerDTOs);
        assertEquals(1, testAnswer.getStudents().size());
        assertEquals(1, testStudent.getAnswers().size());
        assertEquals(testStudent, testAnswer.getStudents().iterator().next());
        assertEquals(testAnswer, testStudent.getAnswers().iterator().next());
    }

    @Test
    void updateAnswer_invalid(){
        Long studentId  = testStudent.getId();
        Long questionId = 1L;
        List<AnswerDTO> answerDTOs = List.of(new AnswerDTO());

        given(studentRepository.getById(studentId)).willReturn(testStudent);
        given(answerRepository.findByQuestionIdAndOrdinalNum(questionId, answerDTOs.get(0).getOrdinalNum()))
                .willReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,()->studentService.updateAnswer(studentId,questionId,answerDTOs));
    }
}