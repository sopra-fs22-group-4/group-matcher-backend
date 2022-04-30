package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.dto.QuestionDTO;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.*;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherAdminOverview;
import ch.uzh.soprafs22.groupmatcher.model.projections.Submission;
import ch.uzh.soprafs22.groupmatcher.repository.AdminRepository;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Slf4j
@AllArgsConstructor
@Service
public class AdminService {

    private AdminRepository adminRepository;

    private MatcherRepository matcherRepository;

    private StudentRepository studentRepository;

    public Admin createAdmin(UserDTO userDTO){
        if (adminRepository.existsByEmail(userDTO.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with the given e-mail already exists");
        Admin newAdmin = new ModelMapper().map(userDTO, Admin.class);
        return adminRepository.save(newAdmin);
    }

    public Admin validateLogin(UserDTO userDTO){
        Admin storedAdmin = adminRepository.findByEmailAndPassword(userDTO.getEmail(), userDTO.getPassword())
                .orElseThrow(() -> adminRepository.findByEmail(userDTO.getEmail())
                        .map(foundEmail -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password, please try again"))
                        .orElse(new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for the given e-mail")));
        if (!storedAdmin.isVerified())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unverified account, please check your inbox");
        return storedAdmin;
    }

    public Admin verifyAccount(Long adminId) {
        Admin storedUser = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for the given ID"));
        storedUser.setVerified(true);
        return adminRepository.save(storedUser);
    }

    public Matcher createMatcher(Long adminId, MatcherDTO matcherDTO) {
        Admin storedAdmin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for the given ID"));
        Matcher newMatcher = new ModelMapper().map(matcherDTO, Matcher.class);
        newMatcher.setAdmins(Set.of(storedAdmin));
        return matcherRepository.save(newMatcher);
    }

    public Matcher createQuestion(Long adminId, Long matcherId, QuestionDTO questionDTO) {
        Matcher storedMatcher = getMatcherById(adminId, matcherId);
        Question newQuestion = new ModelMapper().map(questionDTO, Question.class);
        newQuestion.setMatcher(storedMatcher);
        newQuestion.setOrdinalNum(storedMatcher.getQuestions().size()+1);
        newQuestion.setAnswers(IntStream.range(0, questionDTO.getAnswers().size()).mapToObj(index -> {
            Answer newAnswer = new Answer();
            newAnswer.setContent(questionDTO.getAnswers().get(index));
            newAnswer.setOrdinalNum(index+1);
            newAnswer.setQuestion(newQuestion);
            return newAnswer;
        }).toList());
        storedMatcher.getQuestions().add(newQuestion);
        return matcherRepository.save(storedMatcher);
    }

    public Matcher getMatcherById(Long adminId, Long matcherId) {
        Matcher storedMather = matcherRepository.findById(matcherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No matcher found for the given ID"));
        if (storedMather.getAdmins().stream().noneMatch(admin -> admin.getId().equals(adminId)))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Matcher information is available for admins only");
        return storedMather;
    }

    public Matcher addNewStudents(Long adminId, Long matcherId, List<String> studentEmails) {
        Matcher storedMatcher = getMatcherById(adminId, matcherId);
        studentEmails.stream()
                .filter(studentEmail -> !studentRepository.existsByMatcherIdAndEmail(matcherId, studentEmail))
                .forEach(studentEmail -> {
                    Student newStudent = new Student();
                    newStudent.setEmail(studentEmail);
                    newStudent.setMatcher(storedMatcher);
                    storedMatcher.getStudents().add(newStudent);
                });
        return matcherRepository.save(storedMatcher);
    }

    public List<MatcherAdminOverview> getMatchersByAdminId(Long adminId) {
        return matcherRepository.findByAdmins_IdOrderByDueDateDesc(adminId);
    }

    public List<Submission> getLatestSubmissionsByAdminId(Long adminId) {
        return studentRepository.findByMatcher_Admins_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(
                adminId, Pageable.ofSize(10));
    }

    public List<Submission> getLatestSubmissionsByMatcherId(Long adminId, Long matcherId) {
        getMatcherById(adminId, matcherId); // Verify the admin can access the matcher
        return studentRepository.findByMatcher_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(
                matcherId, Pageable.ofSize(10));
    }
}
