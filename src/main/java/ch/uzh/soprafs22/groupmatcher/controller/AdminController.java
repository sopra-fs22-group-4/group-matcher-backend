package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.service.AdminService;
import ch.uzh.soprafs22.groupmatcher.service.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public Admin loginAdmin(@RequestBody UserDTO admin) {
        return adminService.checkValidLogin(admin);
    }

    @PutMapping("/verify/{adminId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyAccount(@PathVariable Long adminId) {
        adminService.verifyAccount(adminId);
    }

}
