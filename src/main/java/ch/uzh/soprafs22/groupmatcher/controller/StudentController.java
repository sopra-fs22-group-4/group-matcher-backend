package ch.uzh.soprafs22.groupmatcher.controller;


import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class StudentController {

    private StudentService studentService;

    @GetMapping("/question/{matcherId}/student")
    public Student checkValidStudent(@PathVariable Long matcherId,
                                     @RequestBody UserDTO student){
        return studentService.checkValidEmail(matcherId, student);
    }
}
