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

    public void sendEmailsScheduledForNow() {
        emailRepository.findBySendAtIsBeforeAndSentFalse(ZonedDateTime.now()).forEach(email -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(email.getSubject());
            message.setFrom("notify@groupmatcher.ch");
            message.setTo(email.getRecipients().toArray(new String[0]));
            message.setText(email.getContent());
            mailSender.send(message);
            emailRepository.markEmailAsSent(email.getId());
            log.info("Sent email regarding matcher {} to {} recipients", email.getMatcher().getId(), email.getRecipients().size());
        });
    }
}