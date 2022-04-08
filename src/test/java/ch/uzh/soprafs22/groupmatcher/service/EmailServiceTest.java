package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.Email;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.repository.EmailRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {EmailService.class})
class EmailServiceTest {

    @Autowired
    EmailService emailService;

    @MockBean
    EmailRepository emailRepository;

    @MockBean
    JavaMailSender mailSender;

    @Captor
    ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @Test
    void sendEmailsScheduledForNowTest() {
        Matcher testMatcher = new Matcher();
        testMatcher.setId(1L);
        testMatcher.setStudents(Set.of(TestingUtils.createStudent(11L)));
        Email testEmail = new Email();
        testEmail.setId(2L);
        testEmail.setSendAt(ZonedDateTime.now());
        testEmail.setSubject("Test subject");
        testEmail.setContent("Test content");
        testEmail.setMatcher(testMatcher);
        testMatcher.setEmails(List.of(testEmail));
        given(emailRepository.findBySendAtIsBeforeAndSentFalse(any())).willReturn(testMatcher.getEmails());
        doAnswer(invocation -> {
            testEmail.setSent(true);
            return null;
        }).when(emailRepository).markEmailAsSent(testEmail.getId());
        emailService.sendEmailsScheduledForNow();
        verify(emailRepository, times(1)).markEmailAsSent(testEmail.getId());
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentEmail = messageCaptor.getValue();
        assertEquals(testEmail.getSubject(), sentEmail.getSubject());
        assertEquals(testEmail.getContent(), sentEmail.getText());
        assertNotNull(sentEmail.getTo());
        assertEquals(testEmail.getRecipients().length, sentEmail.getTo().length);
        assertTrue(testEmail.isSent());
    }

    @Test
    void sendAccountVerificationEmailTest() {
        emailService.sendAccountVerificationEmail("test@email.com");
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentEmail = messageCaptor.getValue();
        assertNotNull(sentEmail.getTo());
        assertEquals(List.of("test@email.com"), Arrays.stream(sentEmail.getTo()).toList());
    }

    @Test
    void sendResponseVerificationEmailTest() {
        emailService.sendResponseVerificationEmail("test@email.com");
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentEmail = messageCaptor.getValue();
        assertNotNull(sentEmail.getTo());
        assertEquals(List.of("test@email.com"), Arrays.stream(sentEmail.getTo()).toList());
    }
}
