package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.service.MatcherService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/matchers")
public class MatcherController {

    private MatcherService matcherService;

    @GetMapping("/{matcherId}/students/{studentEmail}")
    public Student verifyStudentEmail(@PathVariable Long matcherId, @PathVariable String studentEmail) {
        return matcherService.verifyStudentEmail(matcherId, studentEmail);
    }

    @PutMapping("/{matcherId}/students/{studentEmail}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitStudentAnswers(@PathVariable Long matcherId, @PathVariable String studentEmail, @RequestBody List<Long> answerIds) {
        matcherService.submitStudentAnswers(verifyStudentEmail(matcherId, studentEmail), answerIds);
    }
}
