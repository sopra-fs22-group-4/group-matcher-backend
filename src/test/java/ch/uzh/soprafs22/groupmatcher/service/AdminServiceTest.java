package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.config.AppConfig;
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

    @Autowired
    private AdminService adminService;

    private UserDTO testUserDTO;

    private Admin testAdmin;

    private Matcher testMatcher;

    private MatcherDTO testMatcherDTO;

    private QuestionDTO testQuestionDTO;

    @BeforeEach
    public void setup() {
        testAdmin = TestingUtils.createAdmin();
        testUserDTO = TestingUtils.convertToDTO(testAdmin);
        testMatcher = TestingUtils.createMatcher();
        testMatcherDTO = TestingUtils.convertToDTO(testMatcher);
        testQuestionDTO = TestingUtils.convertToDTO(testMatcher.getQuestions().get(0));
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
        testMatcherDTO.setCourseName("Test Course");
        testMatcherDTO.setUniversity("Test University");
        testMatcherDTO.setDescription("Test Description");
        testMatcherDTO.setPublishDate(ZonedDateTime.now().plus(1, ChronoUnit.DAYS));
        testMatcherDTO.setDueDate(ZonedDateTime.now().plus(7, ChronoUnit.DAYS));
        testMatcherDTO.setGroupSize(3);
        testMatcherDTO.setMatchingStrategy(MatchingStrategy.MOST_SIMILAR);
        given(adminRepository.findById(testAdmin.getId())).willReturn(Optional.of(testAdmin));
        Matcher createdMatcher = adminService.createMatcher(testAdmin.getId(), testMatcherDTO);
        verify(matcherRepository, times(1)).save(any());
        assertEquals(testMatcherDTO.getGroupSize(), createdMatcher.getGroupSize());
        assertEquals(testMatcherDTO.getMatchingStrategy(), createdMatcher.getMatchingStrategy());
    }

    @Test
    void addNewStudents_valid(){
        Matcher testMatcher = TestingUtils.createMatcher();
        int numStudents = testMatcher.getStudents().size();
        List<String> newStudents = List.of("new-student-1@test.com", "new-student-2@test.com");
        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        given(studentRepository.existsByMatcherIdAndEmail(anyLong(), anyString())).willReturn(false);
        Matcher storedMatcher = adminService.addNewStudents(testMatcher.getAdmins().get(0).getId(), testMatcher.getId(), newStudents);
        assertEquals(numStudents + newStudents.size(), storedMatcher.getStudents().size());
    }

    @Test
    void createQuestion_success() {
        Matcher testMatcher = TestingUtils.createMatcher();
        testMatcher.setPublishDate(ZonedDateTime.now().plus(1, ChronoUnit.DAYS));
        QuestionDTO testQuestionDTO = new QuestionDTO();
        testQuestionDTO.setContent("Test Question");
        testQuestionDTO.setQuestionType(QuestionType.SINGLE_CHOICE);
        testQuestionDTO.setAnswers(List.of("Test Answer 1", "Test Answer 2"));
        int numQuestions = testMatcher.getQuestions().size();
        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        Matcher storedMatcher = adminService.createQuestion(testMatcher.getAdmins().get(0).getId(), testMatcher.getId(), testQuestionDTO);
        assertEquals(numQuestions + 1, testMatcher.getQuestions().size());
        Question createdQuestion = storedMatcher.getQuestions().get(numQuestions);
        assertEquals(testMatcher.getId(), createdQuestion.getMatcher().getId());
        assertEquals(testQuestionDTO.getContent()+"?", createdQuestion.getContent());
        assertEquals(testQuestionDTO.getQuestionType(), createdQuestion.getQuestionType());
        assertEquals(testQuestionDTO.getWeight(), createdQuestion.getWeight());
        assertEquals(testQuestionDTO.getAnswers(), createdQuestion.getAnswers().stream().map(Answer::getContent).toList());


    }

    @Test
    void updateAdminWithValidInputs() {
        testAdmin.setEmail("test@email.com");
        testAdmin.setId(1L);
        testAdmin.setName("Tester");
        given(adminRepository.findById(testAdmin.getId())).willReturn(Optional.of(testAdmin));
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
        given(adminRepository.findById(testAdmin.getId())).willReturn(Optional.of(testAdmin));
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
        String testMatcherDTODescription = testMatcherDTO.getDescription();
        Integer testMatcherDTOGroupSize= testMatcherDTO.getGroupSize();
        String testMatcherDTODUniversity = testMatcherDTO.getUniversity();
        String testMatcherDTODCourseName = testMatcherDTO.getCourseName();
        testMatcher.getAdmins().add(testAdmin);
        testAdmin.getMatchers().add(testMatcher);
        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        adminService.updateMatcher(testAdmin.getId(), testMatcher.getId(), testMatcherDTO);
        assertEquals(testMatcherDTODescription, adminService.getMatcherById(testAdmin.getId(), testMatcher.getId()).getDescription());
        assertEquals(testMatcherDTODUniversity, adminService.getMatcherById(testAdmin.getId(), testMatcher.getId()).getUniversity());
        assertEquals(testMatcherDTOGroupSize, adminService.getMatcherById(testAdmin.getId(), testMatcher.getId()).getGroupSize());
        assertEquals(testMatcherDTODCourseName, adminService.getMatcherById(testAdmin.getId(), testMatcher.getId()).getCourseName());
    }

    @Test
    void updateNonExistingQuestionID() {
        Question testQuestion = new Question();
        Long adminId = testMatcher.getAdmins().get(0).getId();
        Long questionId = testQuestion.getId();
        assertThrows(ResponseStatusException.class, () -> adminService.updateQuestion(adminId, questionId, testQuestionDTO));
    }

    @Test
    void updateQuestionAsNonAdmin() {
        Admin admin = new Admin();
        Long adminId = admin.getId();
        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        Long questionId = testMatcher.getQuestions().get(0).getId();
        given(questionRepository.findById(questionId)).willReturn(Optional.of(testMatcher.getQuestions().get(0)));
        assertThrows(ResponseStatusException.class, () -> adminService.updateQuestion(adminId, questionId, testQuestionDTO));
    }

    @Test
    void accessMatcherAsNonAdmin() {
        Admin admin = new Admin();
        Long adminId = admin.getId();
        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        Long matcherId = testMatcher.getId();
        assertThrows(ResponseStatusException.class, () -> adminService.getMatcherById(adminId, matcherId));
    }

    @Test
    void createQuestionAfterMatcherIsPublished() {
        given(matcherRepository.findById(testMatcher.getId())).willReturn(Optional.of(testMatcher));
        Long adminId = testMatcher.getAdmins().get(0).getId();
        Long matcherId = testMatcher.getId();
        assertThrows(ResponseStatusException.class, () -> adminService.createQuestion(adminId, matcherId, testQuestionDTO));
    }

    @Test
    void loginWithNonValidatedAccount() {
        given(adminRepository.findByEmailAndPassword(testAdmin.getEmail(), testAdmin.getPassword())).willReturn(Optional.of(testAdmin));
        testAdmin.setVerified(false);
        testUserDTO.setEmail(testAdmin.getEmail());
        testUserDTO.setPassword(testAdmin.getPassword());
        assertThrows(ResponseStatusException.class, () -> adminService.validateLogin(testUserDTO));
    }
}