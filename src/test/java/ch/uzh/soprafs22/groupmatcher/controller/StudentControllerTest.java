package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.service.StudentService;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    private UserDTO testUserDTO;
    private Student testStudent;
    private Matcher testMatcher;

    @BeforeEach
    public void setup() {
        testMatcher = new Matcher();
        testMatcher.setId(1L);
        testStudent = TestingUtils.createStudent(null);

        testUserDTO = new UserDTO();
        testUserDTO.setEmail(testStudent.getEmail());
    }

    @SneakyThrows
    @Test
    void checkValidStudent(){
        given(studentService.checkValidEmail(any(Long.class),any(UserDTO.class))).willReturn(testStudent);
        mockMvc.perform(get("/question/{matcherId}/student",testMatcher.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(testUserDTO)))
                .andExpect(status().isOk());
    }
}