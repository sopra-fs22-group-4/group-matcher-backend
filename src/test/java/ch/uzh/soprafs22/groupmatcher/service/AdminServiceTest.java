package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminService adminService;

    private Admin testAdmin;
    private UserDTO userDTO;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setEmail("test@test.test");
        testAdmin.setPassword("test");

        ModelMapper mapper = new ModelMapper();
        userDTO = mapper.map(testAdmin, UserDTO.class);

        // when -> any object is being save in the userRepository -> return the dummy testAdmin
        Mockito.when(adminRepository.save(Mockito.any())).thenReturn(testAdmin);
    }
    @Test
    void createAdmin_successful() {
        // create the testAdmin
        Admin createdAdmin = adminService.createAdmin(userDTO);

        // then
        Mockito.verify(adminRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(testAdmin.getId(), createdAdmin.getId());
        assertEquals(testAdmin.getPassword(), createdAdmin.getPassword());
        assertEquals(testAdmin.getEmail(), createdAdmin.getEmail());
    }

    @Test
    void createAdmin_alreadyExist_throwsException() {

        // when
        Mockito.when(adminRepository.findByEmail(Mockito.any())).thenReturn(testAdmin);

        // then -> attempt to create second admin with same Email -> check that an error
        // is thrown
        assertThrows(ResponseStatusException.class, () -> adminService.createAdmin(userDTO));

    }

    @Test
    void checkValidLogin_successful() {

        // when
        testAdmin.setVerified(true);
        Mockito.when(adminRepository.findByEmail(Mockito.any())).thenReturn(testAdmin);

        // then
        Admin loginAdmin = adminService.checkValidLogin(userDTO);

        assertEquals(testAdmin.getId(), loginAdmin.getId());
        assertEquals(testAdmin.getPassword(), loginAdmin.getPassword());
        assertEquals(testAdmin.getEmail(), loginAdmin.getEmail());
        assertTrue(loginAdmin.isVerified());
    }

    @Test
    void checkValidLogin_notVerified_throwsException() {

        // when
        testAdmin.setVerified(false);
        Mockito.when(adminRepository.findByEmail(Mockito.any())).thenReturn(testAdmin);

        // then
        assertThrows(ResponseStatusException.class, () -> adminService.checkValidLogin(userDTO));
    }

    @Test
    void checkValidLogin_notRegistered_throwsException() {

        // when
        Mockito.when(adminRepository.findByEmail(Mockito.any())).thenReturn(null);

        // then
        assertThrows(ResponseStatusException.class, () -> adminService.checkValidLogin(userDTO));
    }

    @Test
    void checkValidLogin_wrongPassword_throwsException() {

        // when
        testAdmin.setVerified(true);
        testAdmin.setPassword("wrongPassword");
        Mockito.when(adminRepository.findByEmail(Mockito.any())).thenReturn(testAdmin);

        // then
        assertThrows(ResponseStatusException.class, () -> adminService.checkValidLogin(userDTO));
    }
}