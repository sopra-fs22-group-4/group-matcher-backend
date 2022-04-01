package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.service.MatcherService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{partialName}")
    public Matcher getMatcherByPartialName(@PathVariable String partialName) {
        return matcherService.getMatcherByPartialName(partialName);
    }
}
