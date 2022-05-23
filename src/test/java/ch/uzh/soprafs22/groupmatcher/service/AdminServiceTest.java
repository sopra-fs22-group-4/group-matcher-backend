package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.config.AppConfig;
import ch.uzh.soprafs22.groupmatcher.constant.MatchingStrategy;
import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
import ch.uzh.soprafs22.groupmatcher.dto.AnswerDTO;
import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.dto.QuestionDTO;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.*;
import ch.uzh.soprafs22.groupmatcher.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {AdminService.class, AppConfig.class})
class AdminServiceTest {

    @MockBean
    private AdminRepository adminRepository;

    @MockBean
    private MatcherRepository matcherRepository;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    SimpMessagingTemplate websocket;

    @MockBean
    EmailService emailService;

    @Autowired
    private AdminService adminService;

    private UserDTO testUserDTO;

    private Admin testAdmin;

    private Matcher testMatcher;

    private MatcherDTO testMatcherDTO;

    private QuestionDTO testQuestionDTO;

    @BeforeEach
    public void setup() {
        testMatcher = TestingUtils.createMatcher();
        testAdmin = testMatcher.getCollaborators().get(0);
        testUserDTO = TestingUtils.convertToDTO(testAdmin);
        testMatcherDTO = TestingUtils.convertToDTO(testMatcher);
        testQuestionDTO = TestingUtils.convertToDTO(testMatcher.getQuestions().get(0));
        given(matcherRepository.save(any(Matcher.class))).willAnswer(returnsFirstArg());
        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        given(adminRepository.save(any(Admin.class))).willAnswer(returnsFirstArg());
        given(adminRepository.findById(testAdmin.getId())).willReturn(Optional.of(testAdmin));
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
        testMatcherDTO.setCourseName("Test Course");
        testMatcherDTO.setUniversity("Test University");
        testMatcherDTO.setDescription("Test Description");
        testMatcherDTO.setPublishDate(ZonedDateTime.now().plus(1, ChronoUnit.DAYS));
        testMatcherDTO.setDueDate(ZonedDateTime.now().plus(7, ChronoUnit.DAYS));
        testMatcherDTO.setGroupSize(3);
        testMatcherDTO.setMatchingStrategy(MatchingStrategy.MOST_SIMILAR);
        Matcher createdMatcher = adminService.createMatcher(testAdmin.getId(), testMatcherDTO);
        verify(matcherRepository, times(1)).save(any());
        assertEquals(testMatcherDTO.getGroupSize(), createdMatcher.getGroupSize());
        assertEquals(testMatcherDTO.getMatchingStrategy(), createdMatcher.getMatchingStrategy());
    }

    @Test
    void addNewStudents_valid(){
        int numStudents = testMatcher.getStudents().size();
        List<String> newStudents = List.of("new-student-1@test.com", "new-student-2@test.com");
        MatcherDTO testMatcherDTO = new MatcherDTO();
        testMatcherDTO.setStudents(newStudents);
        given(studentRepository.existsByMatcherIdAndEmail(anyLong(), anyString())).willReturn(false);
        Matcher storedMatcher = adminService.updateMatcher(testAdmin.getId(), testMatcher.getId(), testMatcherDTO);
        assertEquals(numStudents + newStudents.size(), storedMatcher.getStudents().size());
    }

    @Test
    void createQuestion_success() {
        testMatcher.setPublishDate(ZonedDateTime.now().plus(1, ChronoUnit.DAYS));
        QuestionDTO testQuestionDTO = new QuestionDTO();
        testQuestionDTO.setContent("Test Question");
        testQuestionDTO.setQuestionType(QuestionType.SINGLE_CHOICE);
        AnswerDTO testAnswerDTO = new AnswerDTO();
        testAnswerDTO.setContent("Test Answer 1");
        testQuestionDTO.getAnswers().add(testAnswerDTO);
        int numQuestions = testMatcher.getQuestions().size();
        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        Matcher storedMatcher = adminService.createQuestion(testAdmin.getId(), testMatcher.getId(), testQuestionDTO);
        assertEquals(numQuestions + 1, testMatcher.getQuestions().size());
        Question createdQuestion = storedMatcher.getQuestions().get(numQuestions);
        assertEquals(testMatcher.getId(), createdQuestion.getMatcher().getId());
        assertEquals(testQuestionDTO.getContent()+"?", createdQuestion.getContent());
        assertEquals(testQuestionDTO.getQuestionType(), createdQuestion.getQuestionType());
        assertEquals(testQuestionDTO.getAnswers().stream().map(AnswerDTO::getContent).toList(),
                createdQuestion.getAnswers().stream().map(Answer::getContent).toList());


    }

    @Test
    void updateAdminWithValidInputs() {
        testUserDTO.setEmail("new@email.com");
        testUserDTO.setName("New Tester");
        testUserDTO.setPassword("test12");
        adminService.updateAdmin(testAdmin.getId(), testUserDTO);
        assertEquals(testUserDTO.getEmail(), testAdmin.getEmail());
        assertEquals(testUserDTO.getName(), testAdmin.getName());
        assertEquals(testUserDTO.getPassword(), testAdmin.getPassword());
    }

    @Test
    void updateAdminWithEmptyDTOStrings() {
        testUserDTO.setName("");
        testUserDTO.setEmail("");
        testUserDTO.setPassword("");
        String testAdminName = testAdmin.getName();
        String testAdminEmail = testAdmin.getEmail();
        String testAdminPassword = testAdmin.getPassword();
        adminService.updateAdmin(testAdmin.getId(), testUserDTO);
        assertEquals(testAdminName, testAdmin.getName());
        assertEquals(testAdminEmail, testAdmin.getEmail());
        assertEquals(testAdminPassword, testAdmin.getPassword());
    }

    @Test
    void updateMatcherWithValidInputs() {
        testMatcherDTO.setGroupSize(5);
        testMatcherDTO.setDescription("This is a new course");
        testMatcherDTO.setUniversity("ETH");
        testMatcherDTO.setCourseName("Testing Matchers");
        Matcher updatedMatcher = adminService.updateMatcher(testAdmin.getId(), testMatcher.getId(), testMatcherDTO);
        assertEquals(testMatcherDTO.getDescription(), updatedMatcher.getDescription());
        assertEquals(testMatcherDTO.getUniversity(), updatedMatcher.getUniversity());
        assertEquals(testMatcherDTO.getGroupSize(), updatedMatcher.getGroupSize());
        assertEquals(testMatcherDTO.getCourseName(), updatedMatcher.getCourseName());
    }

    @Test
    void updateNonExistingQuestionID() {
        Long adminId = testAdmin.getId();
        assertThrows(ResponseStatusException.class, () -> adminService.updateQuestion(adminId, 404L, testQuestionDTO));
    }

    @Test
    void updateQuestionAsNonAdmin() {
        Long questionId = testMatcher.getQuestions().get(0).getId();
        given(questionRepository.findById(questionId)).willReturn(Optional.of(testMatcher.getQuestions().get(0)));
        assertThrows(ResponseStatusException.class, () -> adminService.updateQuestion(404L, questionId, testQuestionDTO));
    }

    @Test
    void accessMatcherAsNonAdmin() {
        Long matcherId = testMatcher.getId();
        assertThrows(ResponseStatusException.class, () -> adminService.getMatcherById(404L, matcherId));
    }

    @Test
    void createQuestionAfterMatcherIsPublished() {
        Long adminId = testAdmin.getId();
        Long matcherId = testMatcher.getId();
        assertThrows(ResponseStatusException.class, () -> adminService.createQuestion(adminId, matcherId, testQuestionDTO));
    }

    @Test
    void loginWithNonValidatedAccount() {
        given(adminRepository.findByEmailAndPassword(testAdmin.getEmail(), testAdmin.getPassword()))
                .willReturn(Optional.of(testAdmin));
        testAdmin.setVerified(false);
        testUserDTO.setEmail(testAdmin.getEmail());
        testUserDTO.setPassword(testAdmin.getPassword());
        assertThrows(ResponseStatusException.class, () -> adminService.validateLogin(testUserDTO));
    }
}