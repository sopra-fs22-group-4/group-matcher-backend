package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.constant.Status;
import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.dto.QuestionDTO;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.*;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherAdminOverview;
import ch.uzh.soprafs22.groupmatcher.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@AllArgsConstructor
@Service
public class AdminService {

    private AdminRepository adminRepository;

    private MatcherRepository matcherRepository;

    private QuestionRepository questionRepository;

    private StudentRepository studentRepository;

    private NotificationRepository notificationRepository;

    private ModelMapper modelMapper;

    private SimpMessagingTemplate websocket;

    private EmailService emailService;

    private Matcher notifyAndSave(Matcher matcher, Long adminId, String message) {
        Notification notification = new Notification();
        notification.setCreator(getAdminById(adminId));
        notification.setMatcher(matcher);
        notification.setContent(message);
        matcher.getNotifications().add(notification);
        websocket.convertAndSend("/topic/matchers/"+matcher.getId(), notification);
        return matcherRepository.save(matcher);
    }

    private Admin createCollaborator(UserDTO collaborator) {
        Admin createdAdmin = adminRepository.save(modelMapper.map(collaborator, Admin.class));
        emailService.sendCollaboratorInviteEmail(createdAdmin);
        return createdAdmin;
    }

    private Admin findOrCreateAccount(UserDTO collaborator) {
        return Optional.ofNullable(collaborator.getId()).map(this::getAdminById).or(() ->
                adminRepository.findByEmail(collaborator.getEmail())).orElse(createCollaborator(collaborator));
    }

    private Admin getAdminById(Long adminId) {
        return adminRepository.findById(adminId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for the given ID"));
    }

    public Matcher getMatcherById(Long adminId, Long matcherId) {
        Matcher storedMather = matcherRepository.findById(matcherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No matcher found for the given ID"));
        if (storedMather.getCollaborators().stream().noneMatch(admin -> admin.getId().equals(adminId)))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Matcher information is available for admins only");
        return storedMather;
    }

    private Question getQuestionById(Long adminId, Long questionId) {
        Question storedQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No question found for the given ID"));
        if (storedQuestion.getMatcher().getCollaborators().stream().noneMatch(admin -> admin.getId().equals(adminId)))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Matchers can be edited by admins only");
        return storedQuestion;
    }

    private Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No student found for the given ID"));
    }

    public Admin createAdmin(UserDTO userDTO) {
        if (adminRepository.existsByEmail(userDTO.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with the given e-mail already exists");
        Admin createdAdmin = adminRepository.save(modelMapper.map(userDTO, Admin.class));
        emailService.sendAccountVerificationEmail(createdAdmin);
        return createdAdmin;
    }

    public Admin validateLogin(UserDTO userDTO) {
        Admin storedAdmin = adminRepository.findByEmailAndPassword(userDTO.getEmail(), userDTO.getPassword())
                .orElseThrow(() -> adminRepository.findByEmail(userDTO.getEmail())
                        .map(foundEmail -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password, please try again"))
                        .orElse(new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for the given e-mail")));
        if (!storedAdmin.isVerified())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unverified account, please check your inbox");
        return storedAdmin;
    }

    public Admin verifyAccount(Long adminId) {
        Admin storedUser = getAdminById(adminId);
        storedUser.setVerified(true);
        return adminRepository.save(storedUser);
    }

    public Matcher createMatcher(Long adminId, MatcherDTO matcherDTO) {
        Admin storedAdmin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for the given ID"));
        Matcher newMatcher = modelMapper.map(matcherDTO, Matcher.class);
        newMatcher.getCollaborators().add(storedAdmin);
        matcherDTO.getCollaborators().forEach(collaboratorDTO ->
                newMatcher.getCollaborators().add(findOrCreateAccount(collaboratorDTO)));
        return matcherRepository.save(newMatcher);
    }

    public Matcher createQuestion(Long adminId, Long matcherId, QuestionDTO questionDTO) {
        Matcher storedMatcher = getMatcherById(adminId, matcherId);
        if (storedMatcher.getStatus().equals(Status.ACTIVE))
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Matchers cannot be edited after activation");
        Question newQuestion = modelMapper.map(questionDTO, Question.class);
        newQuestion.setMatcher(storedMatcher);
        newQuestion.getAnswers().forEach(answer -> answer.setQuestion(newQuestion));
        storedMatcher.getQuestions().add(newQuestion);
        return notifyAndSave(storedMatcher, adminId, " created a new question");
    }

    public Matcher updateMatcher(Long adminId, Long matcherId, MatcherDTO matcherDTO) {
        Matcher existingMatcher = getMatcherById(adminId, matcherId);
        modelMapper.map(matcherDTO, existingMatcher);
        Set.copyOf(matcherDTO.getStudents()).stream()
                .filter(studentEmail -> !studentRepository.existsByMatcherIdAndEmail(matcherId, studentEmail))
                .forEach(studentEmail -> {
                    Student newStudent = new Student();
                    newStudent.setEmail(studentEmail);
                    newStudent.setMatcher(existingMatcher);
                    existingMatcher.getStudents().add(newStudent);
                });
        matcherDTO.getCollaborators().forEach(collaboratorDTO -> {
            Admin collaborator = findOrCreateAccount(collaboratorDTO);
            if (!adminRepository.existsByMatchers_IdAndId(matcherId, collaborator.getId()))
                existingMatcher.getCollaborators().add(collaborator);
        });
        return notifyAndSave(existingMatcher, adminId, " updated matcher settings");
    }

    public void deleteMatcher(Long adminId, Long matcherId) {
        Matcher existingMatcher = getMatcherById(adminId, matcherId);
        matcherRepository.delete(existingMatcher);
    }

    public Question updateQuestion(Long adminId, Long questionId, QuestionDTO questionDTO) {
        Question existingQuestion = getQuestionById(adminId, questionId);
        modelMapper.map(questionDTO, existingQuestion);
        existingQuestion.getAnswers().forEach(answer -> {
            if (answer.getQuestion() == null)
                answer.setQuestion(existingQuestion);
        });
        notifyAndSave(existingQuestion.getMatcher(), adminId, " updated question");
        return questionRepository.save(existingQuestion);
    }

    public void deleteQuestion(Long adminId, Long questionId) {
        Question existingQuestion = getQuestionById(adminId, questionId);
        notifyAndSave(existingQuestion.getMatcher(), adminId, " deleted question");
        questionRepository.delete(existingQuestion);
    }

    public Student updateStudent(Long adminId, Long studentId, UserDTO studentDTO) {
        Student existingStudent = getStudentById(studentId);
        modelMapper.map(studentDTO, existingStudent);
        notifyAndSave(existingStudent.getMatcher(), adminId, " updated student details");
        return studentRepository.save(existingStudent);
    }

    public void deleteStudent(Long adminId, Long studentId) {
        Student existingStudent = getStudentById(studentId);
        notifyAndSave(existingStudent.getMatcher(), adminId, " deleted a student");
        studentRepository.delete(existingStudent);
    }

    public List<MatcherAdminOverview> getMatchersByAdminId(Long adminId) {
        return matcherRepository.findByCollaborators_IdOrderByCreatedAtDesc(adminId);
    }

    public List<Notification> getLatestNotificationsByAdminId(Long adminId) {
        return notificationRepository.findByMatcher_Collaborators_IdOrderByCreatedAtDesc(adminId, Pageable.ofSize(30));
    }

    public Map<String, Integer> getDailySubmissionsCountByAdminId(Long adminId) {
        ZonedDateTime aWeekAgo = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minus(7, ChronoUnit.DAYS);
        Map<String, Integer> weeklyCounts = new HashMap<>();
        IntStream.range(0, 7).forEach(numDays -> {
            ZonedDateTime start = aWeekAgo.plus(numDays, ChronoUnit.DAYS);
            weeklyCounts.put(start.format(DateTimeFormatter.ofPattern("dd/MM")),
                    studentRepository.countByMatcher_Collaborators_IdAndSubmissionTimestampBetween(
                            adminId, start, aWeekAgo.plus(numDays + 1L, ChronoUnit.DAYS)));
        });
        return weeklyCounts;
    }

    public Admin updateAdmin(Long adminId, UserDTO admin) {
        Admin existingAdmin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No admin found for the given ID"));
        modelMapper.map(admin, existingAdmin);
        return adminRepository.save(existingAdmin);
    }
}
