package ch.uzh.soprafs22.groupmatcher.controller;


import ch.uzh.soprafs22.groupmatcher.dto.AnswerDTO;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.service.StudentService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @RequestBody
    public void updateStudentAnswer(@PathVariable Long studentId,
                                    @PathVariable Long questionId,
                                    @RequestBody AnswerDTO answerDTO){
        studentService.updateAnswer(studentId, questionId, answerDTO);
    }
}
