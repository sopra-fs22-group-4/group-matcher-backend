package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class EmailService {

    private MatcherRepository matcherRepository;

    private JavaMailSender mailSender;

    private SimpleMailMessage composeMessage(String subject, String content, String... recipients) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setFrom("notify@groupmatcher.ch");
        message.setTo(recipients);
        message.setText(content);
        return message;
    }

    public void sendAccountVerificationEmail(Admin admin) {
        mailSender.send(composeMessage("Verify Account",
                "Hi %s!%nVerify your account: https://group-matcher.herokuapp.com/verify/%s"
                        .formatted(admin.getName(), admin.getId()), admin.getEmail()));
    }

    public void sendResponseVerificationEmail(Student student) {
        mailSender.send(composeMessage("Verify Response",
                "Hi %s!%nVerify your response: https://group-matcher.herokuapp.com/matchers/%s/verify/%s".formatted(
                        student.getName(), student.getMatcher().getId(), student.getId()), student.getEmail()));
    }

    public String[] mapToRecipients(List<Student> students) {
        return students.stream().map(Student::getEmail).toArray(String[]::new);
    }

    public List<Matcher> activatePublishedMatchers() {
        return matcherRepository.findByPublishDateIsAfterAndActiveFalse(ZonedDateTime.now()).stream().map(matcher -> {
            mailSender.send(composeMessage("Link to Matching Quiz",
                    "Hi student!%nFill out the quiz: https://group-matcher.herokuapp.com/matchers/%s".formatted(matcher.getId()),
                    mapToRecipients(matcher.getStudents())));
            matcher.setActive(true);
            return matcherRepository.save(matcher);
        }).toList();
    }
}