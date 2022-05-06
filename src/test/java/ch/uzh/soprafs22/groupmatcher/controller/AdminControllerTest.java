package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.service.AdminService;
import ch.uzh.soprafs22.groupmatcher.service.EmailService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private EmailService emailService;

    private Admin testAdmin;

    @BeforeEach
    public void setup() {
        testAdmin = TestingUtils.createAdmin();
    }

    @Test
    void createAdmin_successful() throws Exception {
        given(adminService.createAdmin(any(UserDTO.class))).willReturn(testAdmin);
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(TestingUtils.convertToDTO(testAdmin))))
                .andExpect(status().isCreated());
    }

    @Test
    void loginAdmin_successful() throws Exception {
        testAdmin.setVerified(true);
        given(adminService.validateLogin(any(UserDTO.class))).willReturn(testAdmin);
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(TestingUtils.convertToDTO(testAdmin))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testAdmin.getId().intValue())))
                .andExpect(jsonPath("$.email", is(testAdmin.getEmail())))
                .andExpect(jsonPath("$.verified", is(testAdmin.isVerified())));
    }

    @Test
    void getMatcher_successful() throws Exception {
        Matcher testMatcher = TestingUtils.createMatcher();
        given(adminService.getMatcherById(anyLong(),anyLong())).willReturn(testMatcher);
        mockMvc.perform(get("/admins/{adminId}/matchers/{matcherId}",
                        testAdmin.getId(),testMatcher.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testMatcher.getId().intValue())))
                .andExpect(jsonPath("$.university", is(testMatcher.getUniversity())))
                .andExpect(jsonPath("$.courseName", is(testMatcher.getCourseName())));
    }

    @Test
    void updateStudentToMatcher_successful() throws Exception {
        testAdmin.setVerified(true);
        given(adminService.verifyAccount(anyLong())).willReturn(testAdmin);
        mockMvc.perform(put("/admins/{adminId}/verify", testAdmin.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testAdmin.getId().intValue())))
                .andExpect(jsonPath("$.verified", is(testAdmin.isVerified())));
    }
}