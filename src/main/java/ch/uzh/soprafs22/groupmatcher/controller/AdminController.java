package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.dto.QuestionDTO;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Question;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherAdminOverview;
import ch.uzh.soprafs22.groupmatcher.model.projections.Submission;
import ch.uzh.soprafs22.groupmatcher.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
public class AdminController {

    private AdminService adminService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Admin createAdmin(@RequestBody UserDTO newAdmin) {
        return adminService.createAdmin(newAdmin);
    }

    @PostMapping("/login")
    public Admin validateCredentials(@RequestBody UserDTO admin) {
        return adminService.validateLogin(admin);
    }

    @PutMapping("/admins/{adminId}/verify")
    public Admin verifyAccount(@PathVariable Long adminId) {
        return adminService.verifyAccount(adminId);
    }

    @PostMapping("/admins/{adminId}/matchers")
    @ResponseStatus(HttpStatus.CREATED)
    public Matcher createMatcher(@PathVariable Long adminId, @RequestBody MatcherDTO newMatcher) {
        return adminService.createMatcher(adminId, newMatcher);
    }

    @GetMapping("/admins/{adminId}/matchers")
    public List<MatcherAdminOverview> getMatchers(@PathVariable Long adminId) {
        return adminService.getMatchersByAdminId(adminId);
    }

    @GetMapping("/admins/{adminId}/submissions/latest")
    public List<Submission> getLatestSubmissions(@PathVariable Long adminId) {
        return adminService.getLatestSubmissionsByAdminId(adminId);
    }

    @GetMapping("/admins/{adminId}/matchers/{matcherId}")
    public Matcher getMatcher(@PathVariable Long adminId, @PathVariable Long matcherId) {
        return adminService.getMatcherById(adminId, matcherId);
    }

    @PutMapping("/admins/{adminId}/matchers/{matcherId}")
    public Matcher updateMatcher(@PathVariable Long adminId, @PathVariable Long matcherId, @RequestBody MatcherDTO updatedMatcher) {
        return adminService.updateMatcher(adminId, matcherId, updatedMatcher);
    }

    @DeleteMapping("/admins/{adminId}/matchers/{matcherId}")
    public void deleteMatcher(@PathVariable Long adminId, @PathVariable Long matcherId) {
        adminService.deleteMatcher(adminId, matcherId);
    }

    @PostMapping("/admins/{adminId}/matchers/{matcherId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public Matcher createQuestion(@PathVariable Long adminId, @PathVariable Long matcherId, @RequestBody QuestionDTO newQuestion) {
        return adminService.createQuestion(adminId, matcherId, newQuestion);
    }

    @GetMapping("/admins/{adminId}/matchers/{matcherId}/submissions/latest")
    public List<Submission> getLatestSubmissions(@PathVariable Long adminId, @PathVariable Long matcherId) {
        return adminService.getLatestSubmissionsByMatcherId(adminId, matcherId);
    }

    @PutMapping("/admins/{adminId}/questions/{questionId}")
    public Question updateQuestion(@PathVariable Long adminId, @PathVariable Long questionId, @RequestBody QuestionDTO updatedQuestion) {
        return adminService.updateQuestion(adminId, questionId, updatedQuestion);
    }

    @DeleteMapping("/admins/{adminId}/questions/{questionId}")
    public void deleteQuestion(@PathVariable Long adminId, @PathVariable Long questionId) {
        adminService.deleteQuestion(adminId, questionId);
    }

    @PutMapping("/admins/{adminId}/profile")
    public Admin updateProfile(@PathVariable long adminId, @RequestBody UserDTO admin) {
        return adminService.updateAdmin(adminId, admin);
    }
}
