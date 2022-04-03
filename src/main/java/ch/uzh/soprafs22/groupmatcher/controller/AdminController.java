package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class AdminController {

    private AdminService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Admin createAdmin(@RequestBody UserDTO newAdmin) {return userService.createAdmin(newAdmin);}

    @PostMapping("/login")
    public Admin loginAdmin(@RequestBody UserDTO admin) {return userService.checkValidLogin(admin);}

}
