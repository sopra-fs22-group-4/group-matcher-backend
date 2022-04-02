package ch.uzh.soprafs22.groupmatcher;

import ch.uzh.soprafs22.groupmatcher.model.Email;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.EmailRepository;
import ch.uzh.soprafs22.groupmatcher.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
        Student testStudent = new Student();
        testStudent.setEmail("test@email.com");
        Matcher testMatcher = new Matcher();
        testMatcher.setId(1L);
        testMatcher.setStudents(Set.of(testStudent));
        Email testEmail = new Email();
        testEmail.setId(2L);
        testEmail.setSubject("Test subject");
        testEmail.setContent("Test content");
        testEmail.setMatcher(testMatcher);
        doReturn(List.of(testEmail)).when(emailRepository).findBySendAtIsBeforeAndSentFalse(any());
        emailService.sendEmailsScheduledForNow();
        verify(emailRepository, times(1)).markEmailAsSent(testEmail.getId());
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage actualEmail = messageCaptor.getValue();
        assertEquals(testEmail.getSubject(), actualEmail.getSubject());
        assertEquals(testEmail.getContent(), actualEmail.getText());
        assertNotNull(actualEmail.getTo());
        assertEquals(testEmail.getRecipients().size(), actualEmail.getTo().length);
    }
}
