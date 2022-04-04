package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.repository.EmailRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Slf4j
@AllArgsConstructor
@Service
public class EmailService {

    private EmailRepository emailRepository;

    private JavaMailSender mailSender;

    private SimpleMailMessage composeMessage(String subject, String content, String... recipients) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setFrom("notify@groupmatcher.ch");
        message.setTo(recipients);
        message.setText(content);
        return message;
    }

    public void sendAccountVerificationEmail(String email) {
        mailSender.send(composeMessage("Verify Account", "Click here to verify your account", email));
    }

    public void sendResponseVerificationEmail(String email) {
        mailSender.send(composeMessage("Verify Response", "Click here to verify your response", email));
    }

    public void sendEmailsScheduledForNow() {
        emailRepository.findBySendAtIsBeforeAndSentFalse(ZonedDateTime.now()).forEach(email -> {
            mailSender.send(composeMessage(email.getSubject(), email.getContent(), email.getRecipients()));
            emailRepository.markEmailAsSent(email.getId());
        });
    }
}