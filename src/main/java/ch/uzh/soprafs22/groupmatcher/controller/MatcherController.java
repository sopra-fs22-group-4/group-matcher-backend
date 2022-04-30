package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherOverview;
import ch.uzh.soprafs22.groupmatcher.model.projections.StudentOverview;
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

    @GetMapping("/{matcherId}")
    public MatcherOverview getMatcherOverview(@PathVariable Long matcherId) {
        return matcherService.getMatcherOverview(matcherId);
    }

    @GetMapping("/{matcherId}/students/{studentEmail}")
    public StudentOverview verifyStudentEmail(@PathVariable Long matcherId, @PathVariable String studentEmail) {
        return matcherService.verifyStudentEmail(matcherId, studentEmail);
    }

    @PutMapping("/{matcherId}/students/{studentEmail}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitStudentAnswers(@PathVariable Long matcherId, @PathVariable String studentEmail, @RequestBody List<Long> answerIds) {
        matcherService.submitStudentAnswers(matcherId, studentEmail, answerIds);
    }
}
