package ch.uzh.soprafs22.groupmatcher.controller;

import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherOverview;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Date;

@Controller
public class DemoController {

    @Autowired
    private MatcherRepository matcherRepository;

    @GetMapping("/invitation/{id}")
    public String invitation(@PathVariable("id") Long id,  Model model) {
        Matcher matcher = matcherRepository.getById(id);
        String course = matcher.getCourseName();
        Date date = Date.from(matcher.getDueDate().toInstant());

        model.addAttribute("course", course);
        model.addAttribute("date", date);
        return "invitation";
    }
}
