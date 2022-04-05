package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {AdminService.class})
class AdminServiceTest {

    @MockBean
    private AdminRepository adminRepository;

    @Autowired
    private AdminService adminService;

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
        given(adminRepository.save(any(Admin.class))).willAnswer(returnsFirstArg());
    }

    @Test
    void createAdmin_successful() {
        Admin createdAdmin = adminService.createAdmin(testUserDTO);
        verify(adminRepository, times(1)).save(any());
        assertEquals(testUserDTO.getPassword(), createdAdmin.getPassword());
        assertEquals(testUserDTO.getEmail(), createdAdmin.getEmail());
    }

    @Test
    void createAdmin_alreadyExist_throwsException() {
        given(adminRepository.existsByEmail(testUserDTO.getEmail())).willReturn(true);
        assertThrows(ResponseStatusException.class, () -> adminService.createAdmin(testUserDTO));
    }

    @Test
    void checkValidLogin_successful() {
        testAdmin.setVerified(true);
        given(adminRepository.findByEmail(testUserDTO.getEmail())).willReturn(Optional.of(testAdmin));
        Admin returnedAdmin = adminService.checkValidLogin(testUserDTO);
        assertEquals(testAdmin.getId(), returnedAdmin.getId());
        assertEquals(testAdmin.getPassword(), returnedAdmin.getPassword());
        assertEquals(testAdmin.getEmail(), returnedAdmin.getEmail());
        assertTrue(returnedAdmin.isVerified());
    }

    @Test
    void checkValidLogin_notVerified_throwsException() {
        assertFalse(testAdmin.isVerified());
        given(adminRepository.findByEmail(testUserDTO.getEmail())).willReturn(Optional.of(testAdmin));
        assertThrows(ResponseStatusException.class, () -> adminService.checkValidLogin(testUserDTO));
    }

    @Test
    void checkValidLogin_notRegistered_throwsException() {
        given(adminRepository.findByEmail(testUserDTO.getEmail())).willReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> adminService.checkValidLogin(testUserDTO));
    }

    @Test
    void checkValidLogin_wrongPassword_throwsException() {
        testAdmin.setVerified(true);
        testAdmin.setPassword("wrongPassword");
        given(adminRepository.findByEmail(testUserDTO.getEmail())).willReturn(Optional.of(testAdmin));
        assertThrows(ResponseStatusException.class, () -> adminService.checkValidLogin(testUserDTO));
    }

    @Test
    void verifyAccount_successful() {
        assertFalse(testAdmin.isVerified());
        given(adminRepository.findById(testAdmin.getId())).willReturn(Optional.of(testAdmin));
        Admin returnedAdmin = adminService.verifyAccount(testAdmin.getId());
        assertTrue(returnedAdmin.isVerified());
    }

    @Test
    void verifyAccount_notRegistered_throwsException() {
        given(adminRepository.findById(testAdmin.getId())).willReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> adminService.verifyAccount(1L));
    }
}