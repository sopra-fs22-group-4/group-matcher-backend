package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.constant.MatchingStrategy;
import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.dto.QuestionDTO;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.*;
import ch.uzh.soprafs22.groupmatcher.repository.AdminRepository;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.QuestionRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {AdminService.class})
class AdminServiceTest {

    @MockBean
    private AdminRepository adminRepository;

    @MockBean
    private MatcherRepository matcherRepository;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private StudentRepository studentRepository;

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
        given(matcherRepository.save(any(Matcher.class))).willAnswer(returnsFirstArg());
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
        given(adminRepository.findByEmailAndPassword(testUserDTO.getEmail(), testUserDTO.getPassword())).willReturn(Optional.of(testAdmin));
        Admin returnedAdmin = adminService.validateLogin(testUserDTO);
        assertEquals(testAdmin.getId(), returnedAdmin.getId());
        assertEquals(testAdmin.getPassword(), returnedAdmin.getPassword());
        assertEquals(testAdmin.getEmail(), returnedAdmin.getEmail());
        assertTrue(returnedAdmin.isVerified());
    }

    @Test
    void checkValidLogin_notVerified_throwsException() {
        assertFalse(testAdmin.isVerified());
        given(adminRepository.findByEmail(testUserDTO.getEmail())).willReturn(Optional.of(testAdmin));
        assertThrows(ResponseStatusException.class, () -> adminService.validateLogin(testUserDTO));
    }

    @Test
    void checkValidLogin_notRegistered_throwsException() {
        given(adminRepository.findByEmail(testUserDTO.getEmail())).willReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> adminService.validateLogin(testUserDTO));
    }

    @Test
    void checkValidLogin_wrongPassword_throwsException() {
        testAdmin.setVerified(true);
        testAdmin.setPassword("wrongPassword");
        given(adminRepository.findByEmail(testUserDTO.getEmail())).willReturn(Optional.of(testAdmin));
        assertThrows(ResponseStatusException.class, () -> adminService.validateLogin(testUserDTO));
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

    @Test
    void createMatcher_successful() {
        MatcherDTO testMatcherDTO = new MatcherDTO();
        testMatcherDTO.setGroupSize(5);
        given(adminRepository.findById(testAdmin.getId())).willReturn(Optional.of(testAdmin));
        Matcher createdMatcher = adminService.createMatcher(testAdmin.getId(), testMatcherDTO);
        verify(matcherRepository, times(1)).save(any());
        assertEquals(testMatcherDTO.getGroupSize(), createdMatcher.getGroupSize());
        assertEquals(MatchingStrategy.MOST_SIMILAR, createdMatcher.getMatchingStrategy());
    }

    @Test
    void addNewStudents_valid(){
        Matcher testMatcher = new Matcher();
        testMatcher.setId(1L);
        testMatcher.getAdmins().add(testAdmin);
        Student testStudent = TestingUtils.createStudent(3L, 3);
        testStudent.setMatcher(testMatcher);
        testMatcher.getStudents().add(testStudent);
        List<String> testStudents = List.of("new-student-1@test.com", "new-student-2@test.com");
        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        given(studentRepository.existsByMatcherIdAndEmail(anyLong(), anyString())).willReturn(false);
        assertEquals(1, testMatcher.getStudents().size());
        Matcher storedMatcher = adminService.addNewStudents(testAdmin.getId(), testMatcher.getId(), testStudents);
        assertEquals(3, storedMatcher.getStudents().size());
    }

    @Test
    void createQuestion_success() {
        QuestionDTO testQuestionDTO = new QuestionDTO();
        testQuestionDTO.setContent("Test Question");
        testQuestionDTO.setQuestionType(QuestionType.SINGLE_CHOICE);
        testQuestionDTO.setAnswers(List.of("Test Answer"));
        Matcher testMatcher = new Matcher();
        testMatcher.setId(1L);
        testMatcher.setPublishDate(ZonedDateTime.now().plus(1, ChronoUnit.DAYS));
        testMatcher.getAdmins().add(testAdmin);
        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        assertTrue(testMatcher.getQuestions().isEmpty());
        Matcher storedMatcher = adminService.createQuestion(testAdmin.getId(), testMatcher.getId(), testQuestionDTO);
        assertEquals(1, testMatcher.getQuestions().size());
        assertEquals(testMatcher.getId(), storedMatcher.getQuestions().get(0).getMatcher().getId());
        assertEquals(testQuestionDTO.getContent()+"?", storedMatcher.getQuestions().get(0).getContent());
        assertEquals(testQuestionDTO.getQuestionType(), storedMatcher.getQuestions().get(0).getQuestionType());
        assertEquals(testQuestionDTO.getWeight(), storedMatcher.getQuestions().get(0).getWeight());
        assertEquals(testQuestionDTO.getAnswers(), storedMatcher.getQuestions().get(0).getAnswers()
                .stream().map(Answer::getContent).toList());


    }
}