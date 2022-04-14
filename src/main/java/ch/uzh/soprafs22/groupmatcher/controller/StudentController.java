package ch.uzh.soprafs22.groupmatcher.controller;


import ch.uzh.soprafs22.groupmatcher.dto.AnswerDTO;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@RestController
public class StudentController {

    private StudentService studentService;

    @GetMapping("/question/{matcherId}/{studentEmail}")
    public Student checkValidStudent(@PathVariable Long matcherId,
                                     @PathVariable String studentEmail){
        return studentService.checkValidEmail(matcherId, studentEmail);
    }

    @PutMapping("/{studentId}/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStudentAnswer(@PathVariable Long studentId,
                                    @PathVariable Long questionId,
                                    @RequestBody List<AnswerDTO> answerDTOs){
        studentService.updateAnswer(studentId, questionId, answerDTOs);
    }
}
