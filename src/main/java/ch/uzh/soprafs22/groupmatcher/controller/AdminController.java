package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherAdminOverview;
import ch.uzh.soprafs22.groupmatcher.model.projections.Submission;
import ch.uzh.soprafs22.groupmatcher.service.AdminService;
import ch.uzh.soprafs22.groupmatcher.service.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
public class AdminController {

    private AdminService adminService;

    private EmailService emailService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void createAdmin(@RequestBody UserDTO newAdmin) {
        adminService.createAdmin(newAdmin);
        emailService.sendAccountVerificationEmail(newAdmin.getEmail());
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
    public void createMatcher(@PathVariable Long adminId, @RequestBody MatcherDTO newMatcher) {
        adminService.createMatcher(adminId, newMatcher);
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
}
