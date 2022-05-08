package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {EmailService.class, SpringTemplateEngine.class})
class EmailServiceTest {

    @Autowired
    EmailService emailService;

    @MockBean
    MatcherRepository matcherRepository;

    @MockBean
    JavaMailSender mailSender;

    @Autowired
    SpringTemplateEngine templateEngine;

    @Captor
    ArgumentCaptor<MimeMessage> messageCaptor;

    private Matcher testMatcher;

    @BeforeEach
    public void setup() {
        JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
        testMatcher = TestingUtils.createMatcher();
        given(mailSender.createMimeMessage()).willReturn(mailSenderImpl.createMimeMessage());
        given(matcherRepository.save(any())).willAnswer(returnsFirstArg());
    }

    @SneakyThrows
    @Test
    void activatePublishedMatchersTest() {
        given(matcherRepository.findByPublishDateIsBeforeAndActiveFalse(any())).willReturn(List.of(testMatcher));
        assertFalse(testMatcher.isActive());
        List<Matcher> activatedMatchers = emailService.activateScheduledMatchers();
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentEmail = messageCaptor.getValue();
        assertEquals("Link to Matching Quiz", sentEmail.getSubject());
        assertEquals(testMatcher.getStudents().size(), sentEmail.getAllRecipients().length);
        assertEquals(1, activatedMatchers.size());
        assertEquals(testMatcher.getId(), activatedMatchers.get(0).getId());
        assertTrue(activatedMatchers.get(0).isActive());
    }

    @SneakyThrows
    @Test
    void sendAccountVerificationEmailTest() {
        Admin testAdmin = TestingUtils.createAdmin();
        emailService.sendAccountVerificationEmail(testAdmin);
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentEmail = messageCaptor.getValue();
        assertEquals(List.of(testAdmin.getEmail()), Arrays.stream(sentEmail.getAllRecipients()).map(Address::toString).toList());
    }

    @SneakyThrows
    @Test
    void sendResponseVerificationEmailTest() {
        Student testStudent = testMatcher.getStudents().get(0);
        testStudent.setName("Test Student");
        emailService.sendResponseVerificationEmail(testStudent);
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentEmail = messageCaptor.getValue();
        assertNotNull(sentEmail.getAllRecipients());
        assertEquals(List.of(testStudent.getEmail()), Arrays.stream(sentEmail.getAllRecipients()).map(Address::toString).toList());
    }
}