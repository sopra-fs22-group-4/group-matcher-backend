package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.service.MatcherService;
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

@WebMvcTest(MatcherController.class)
class MatcherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatcherService matcherService;

    private Student testStudent;
    private Matcher testMatcher;

    @BeforeEach
    public void setup() {
        testMatcher = new Matcher();
        testMatcher.setId(1L);
        testStudent = TestingUtils.createStudent(null, null);
    }

    @SneakyThrows
    @Test
    void checkValidStudent(){
        given(matcherService.verifyStudentEmail(any(Long.class),any(String.class))).willReturn(TestingUtils.convertToOverview(testStudent));
        mockMvc.perform(get("/matchers/{matcherId}/students/{studentEmail}}",
                        testMatcher.getId(), testStudent.getEmail())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}