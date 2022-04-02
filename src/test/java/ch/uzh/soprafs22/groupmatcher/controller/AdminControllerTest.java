package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.service.AdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    void createAdmin_successful() throws Exception{

        // expected output
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setEmail("test@test.test");
        admin.setPassword("test");

        ModelMapper mapper = new ModelMapper();
        UserDTO userDTO = mapper.map(admin, UserDTO.class);

        given(adminService.createAdmin(Mockito.any())).willReturn(admin);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(admin.getId().intValue())))
                .andExpect(jsonPath("$.email", is(admin.getEmail())));
    }

    @Test
    void loginAdmin_successful() throws Exception {
        // expected output
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setEmail("test@test.test");
        admin.setPassword("test");
        admin.setVerified(true);

        ModelMapper mapper = new ModelMapper();
        UserDTO userDTO = mapper.map(admin, UserDTO.class);

        given(adminService.checkValidLogin(Mockito.any())).willReturn(admin);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(admin.getId().intValue())))
                .andExpect(jsonPath("$.email", is(admin.getEmail())))
                .andExpect(jsonPath("$.verified", is(admin.isVerified())));
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e));
        }
    }

}