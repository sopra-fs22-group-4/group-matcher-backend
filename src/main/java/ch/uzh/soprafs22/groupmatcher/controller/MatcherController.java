package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
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

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public void createMatcher(@RequestBody MatcherDTO newMatcher) {
        matcherService.createMatcher(newMatcher);
    }

    @GetMapping("/{matcherId}/students/{studentEmail}")
    public Student checkValidStudent(@PathVariable Long matcherId, @PathVariable String studentEmail) {
        return matcherService.checkValidEmail(matcherId, studentEmail);
    }

    @PutMapping("/{matcherId}/students/{studentEmail}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitStudentAnswers(@PathVariable Long matcherId, @PathVariable String studentEmail, @RequestBody List<Long> answerIds) {
        matcherService.submitStudentAnswers(checkValidStudent(matcherId, studentEmail), answerIds);
    }
}
