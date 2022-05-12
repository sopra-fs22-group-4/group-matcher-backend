package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class EmailService {

    private MatcherRepository matcherRepository;

    private JavaMailSender mailSender;

    private SpringTemplateEngine templateEngine;

    private MimeMessage composeMessage(String subject, String templateName, Map<String, Object> variables, String... recipients) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED, StandardCharsets.UTF_8.name());
            Context context = new Context();
            context.setVariables(variables);
            helper.setFrom("notify@groupmatcher.ch");
            helper.setTo(recipients);
            helper.setSubject(subject);
            String html = templateEngine.process(templateName, context);
            helper.setText(html, true);
            helper.addInline("bg.png", new ClassPathResource("bg.png"));
            helper.addInline("logo.png", new ClassPathResource("logo.png"));
            helper.addInline("tagline.png", new ClassPathResource("tagline.png"));
        } catch (MessagingException exception) {
            log.info("Failed to create an email message");
        }
        return message;
    }

    public Map<String, Object> parseMatcherVariables(Matcher matcher) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("id",matcher.getId());
        variables.put("courseName", matcher.getCourseName());
        variables.put("date",matcher.getDueDate().format(formatter));
        return variables;
    }

    public void sendAccountVerificationEmail(Admin admin) {
        Map<String, Object> variables = Map.of("name", admin.getName());
        mailSender.send(composeMessage("Verify Account", "email_verification.html", variables, admin.getEmail()));
    }

    public void sendResponseVerificationEmail(Student student) {
        Map<String, Object> variables = Map.of("name", student.getName(),
                "matcherId", student.getMatcher().getId(), "studentId", student.getId());
        mailSender.send(composeMessage("Verify Response", "email_verification.html", variables, student.getEmail()));
    }


    public void sendGroupInfo(Matcher matcher) {
        Map<String, Object> variables = parseMatcherVariables(matcher);
        mailSender.send(composeMessage("Group Information", "matching_results.html",
                variables, mapToRecipients(matcher.getStudents())));
    }

    public void sendReminder(Matcher matcher) {
        Map<String, Object> variables = parseMatcherVariables(matcher);
        mailSender.send(composeMessage("Reminder", "reminder.html",
                variables, mapToRecipients(matcher.getStudents())));
    }

    public String[] mapToRecipients(List<Student> students) {
        return students.stream().map(Student::getEmail).toArray(String[]::new);
    }

    public List<Matcher> activateScheduledMatchers() {
        return matcherRepository.findByPublishDateIsBeforeAndActiveFalse(ZonedDateTime.now()).stream().map(matcher -> {
            Map<String, Object> variables = Map.of("courseName", matcher.getCourseName(),
                    "matcherId", matcher.getId(), "dueDate", matcher.getDueDate());
            mailSender.send(composeMessage("Link to Matching Quiz", "invitation.html",
                    variables, mapToRecipients(matcher.getStudents())));
            matcher.setActive(true);
            return matcherRepository.save(matcher);
        }).toList();
    }
}