package ch.uzh.soprafs22.groupmatcher;

import ch.uzh.soprafs22.groupmatcher.controller.MatcherController;
import ch.uzh.soprafs22.groupmatcher.rest.dto.MatcherDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ApplicationTests {

    @Autowired
    MatcherController matcherController;

    @Test
    void contextLoads() {
        MatcherDTO newMatcher = new MatcherDTO();
        newMatcher.setName("test");
        matcherController.createMatcher(newMatcher);
        assertEquals("test", matcherController.getMatcherByPartialName("test").getName());
    }

}
