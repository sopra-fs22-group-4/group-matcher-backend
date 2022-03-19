package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@AllArgsConstructor
@Service
public class MatcherService {

    private MatcherRepository matcherRepository;

    public void createMatcher(MatcherDTO matcherDTO) {
        Matcher newMatcher = new Matcher();
        newMatcher.setName(matcherDTO.getName());
        matcherRepository.save(newMatcher);
    }
    public Matcher getMatcherByPartialName(String partialName) {
        return matcherRepository.findTopByNameContainsOrderByCreatedAtDesc(partialName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
