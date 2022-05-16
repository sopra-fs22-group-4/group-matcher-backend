package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.service.MatcherService;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatcherController.class)
class MatcherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatcherService matcherService;

    private Matcher testMatcher;

    private Student testStudent;

    @BeforeEach
    public void setup() {
        testMatcher = TestingUtils.createMatcher();
        testStudent = testMatcher.getStudents().get(0);
    }

    @SneakyThrows
    @Test
    void checkValidStudent(){
        UserDTO testStudentDTO = new UserDTO();
        testStudentDTO.setName("Test Student");
        testStudentDTO.setEmail(testStudent.getEmail());
        given(matcherService.findMatcherStudent(testMatcher.getId(), testStudentDTO)).willReturn(testStudent);
        mockMvc.perform(post("/matchers/{matcherId}/students", testMatcher.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(testStudentDTO)))
                .andExpect(jsonPath("$.id", is(testStudent.getId().intValue())));
    }

    @SneakyThrows
    @Test
    void submitStudentAnswers(){
        mockMvc.perform(post("/matchers/{matcherId}/students/{studentEmail}",
                        testMatcher.getId(), testStudent.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(List.of(1L, 2L))))
                .andExpect(status().isNoContent());
    }

    @SneakyThrows
    @Test
    void getMatcherOverview(){
        given(matcherService.getMatcherOverview(testMatcher.getId())).willReturn(TestingUtils.convertToOverview(testMatcher));
        mockMvc.perform(get("/matchers/{matcherId}", testMatcher.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testMatcher.getId().intValue())));
    }
}