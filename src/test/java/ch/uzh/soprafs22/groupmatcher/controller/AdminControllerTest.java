package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private UserDTO testUserDTO;

    private Admin testAdmin;

    @BeforeEach
    public void setup() {
        testUserDTO = new UserDTO();
        testUserDTO.setEmail("test@email.com");
        testUserDTO.setPassword("test");
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setEmail(testUserDTO.getEmail());
        testAdmin.setPassword(testUserDTO.getPassword());
    }

    @Test
    void createAdmin_successful() throws Exception {
        given(adminService.createAdmin(any(UserDTO.class))).willReturn(testAdmin);
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(testUserDTO)))
                .andExpect(status().isCreated());
        verify(emailService, times(1)).sendAccountVerificationEmail(testAdmin);
    }

    @Test
    void loginAdmin_successful() throws Exception {
        testAdmin.setVerified(true);
        given(adminService.validateLogin(any(UserDTO.class))).willReturn(testAdmin);
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(testUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testAdmin.getId().intValue())))
                .andExpect(jsonPath("$.email", is(testAdmin.getEmail())))
                .andExpect(jsonPath("$.verified", is(testAdmin.isVerified())));
    }

}