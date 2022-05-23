package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.constant.Status;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    @Value("${app.frontend-url}")
    private String frontendURL;

    private final MatcherRepository matcherRepository;

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    private MimeMessage composeMessage(String subject, String templateName, Map<String, Object> variables, String... recipients) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED, StandardCharsets.UTF_8.name());
            Context context = new Context();
            context.setVariables(variables);
            helper.setFrom("notify@groupmatcher.ch", "groupmatcher");
            helper.setBcc(recipients);
            helper.setSubject(subject);
            String html = templateEngine.process(templateName, context);
            helper.setText(html, true);
            helper.addInline("bg.png", new ClassPathResource("bg.png"));
            helper.addInline("logo.png", new ClassPathResource("logo.png"));
            helper.addInline("tagline.png", new ClassPathResource("tagline.png"));
        } catch (MessagingException | UnsupportedEncodingException exception) {
            log.info("Failed to create an email message");
        }
        return message;
    }

    public Map<String, Object> parseMatcherVariables(Matcher matcher) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("frontendURL", frontendURL);
        variables.put("id",matcher.getId());
        variables.put("courseName", matcher.getCourseName());
        variables.put("dueDate", matcher.getDueDate().format(formatter));
        return variables;
    }

    public void sendAccountVerificationEmail(Admin admin) {
        Map<String, Object> variables = Map.of(
                "name", admin.getName(),
                "verifyURL", "%s/verify/%s".formatted(frontendURL, admin.getId()));
        mailSender.send(composeMessage("Welcome to groupmatcher!", "email_verification.html", variables, admin.getEmail()));
    }

    public void sendResponseVerificationEmail(Student student) {
        Map<String, Object> variables = Map.of("name", student.getName(),
                "matcherId", student.getMatcher().getId(), "studentId", student.getId());
        mailSender.send(composeMessage("Verify Response", "email_verification.html", variables, student.getEmail()));
    }

    public void sendCollaboratorInviteEmail(Admin admin) {
//        if (Strings.isNullOrEmpty(collaborator.getPassword())) then include password reset link
//        Map<String, Object> variables = Map.of("name", admin.getName());
//        mailSender.send(composeMessage("You are invited!", "collaborator_invite.html", variables, admin.getEmail()));
    }

    public List<Matcher> sendMatchedGroupNotificationEmail() {
        return matcherRepository.findByStatus(Status.MATCHED).stream().map(matcher -> {
            log.info("Sending matching results to students of Matcher {}", matcher.getId());
            matcher.getTeams().forEach(team -> {
                Map<String, Object> variables = parseMatcherVariables(matcher);
                mailSender.send(composeMessage("Group Introduction", "matching_results.html",
                        variables, mapToRecipients(team.getStudents())));
                team.setNotified(true);
            });
            matcher.setStatus(Status.COMPLETED);
            return matcherRepository.save(matcher);
        }).toList();
    }

    public void sendReminder(Matcher matcher) {
        Map<String, Object> variables = parseMatcherVariables(matcher);
        mailSender.send(composeMessage("Reminder", "reminder.html",
                variables, mapToRecipients(matcher.getStudents())));
    }

    public String[] mapToRecipients(List<Student> students) {
        return students.stream().map(Student::getEmail).toArray(String[]::new);
    }

    @Transactional
    public List<Matcher> sendMatchingQuizInviteEmail() {
        return matcherRepository.findByPublishDateIsBeforeAndStatus(ZonedDateTime.now(), Status.DRAFT).stream().map(matcher -> {
            log.info("Sending quiz invitation emails to students of Matcher {}", matcher.getId());
            Map<String, Object> variables = parseMatcherVariables(matcher);
            variables.put("matcherURL", "%s/matchers/%s".formatted(frontendURL, matcher.getId()));
            mailSender.send(composeMessage(
                    "You've been invited to take a Matching Quiz for "+matcher.getCourseName(),
                    "invitation.html", variables, mapToRecipients(matcher.getStudents())));
            matcher.setStatus(Status.ACTIVE);
            return matcherRepository.save(matcher);
        }).toList();
    }
}