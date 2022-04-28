package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.model.projections.Submission;
import ch.uzh.soprafs22.groupmatcher.service.MatcherService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@RestController
@RequestMapping("/matchers")
public class MatcherController {

    private MatcherService matcherService;

    @GetMapping("/{matcherId}/students/{studentEmail}")
    public Student verifyStudentEmail(@PathVariable Long matcherId, @PathVariable String studentEmail) {
        return matcherService.verifyStudentEmail(matcherId, studentEmail);
    }

    @GetMapping("/{matcherId}/submissions/latest")
    public List<Submission> getLatestSubmissions(@PathVariable Long matcherId) {
        return matcherService.getLatestSubmissionsByMatcherId(matcherId);
    }

    @PutMapping("/{matcherId}/students/{studentEmail}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitStudentAnswers(@PathVariable Long matcherId, @PathVariable String studentEmail, @RequestBody List<Long> answerIds) {
        matcherService.submitStudentAnswers(verifyStudentEmail(matcherId, studentEmail), answerIds);
    }

    @PostMapping("/{matcherId}/students")
    public void addStudents(@PathVariable Long matcherId, @RequestBody Set<Student> students){
        matcherService.addNewStudents(matcherId, students);
    }
}
