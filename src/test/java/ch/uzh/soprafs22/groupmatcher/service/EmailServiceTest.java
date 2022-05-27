package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.config.AppConfig;
import ch.uzh.soprafs22.groupmatcher.constant.MatcherStatus;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.model.Team;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.TeamRepository;
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
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {EmailService.class, AppConfig.class})
class EmailServiceTest {

    @Autowired
    EmailService emailService;

    @MockBean
    MatcherRepository matcherRepository;

    @MockBean
    TeamRepository teamRepository;

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
    void sendMatchingQuizInviteEmailTest() {
        testMatcher.setStatus(MatcherStatus.DRAFT);
        given(matcherRepository.findByPublishDateIsBeforeAndStatus(any(), any())).willReturn(List.of(testMatcher));
        List<Matcher> activatedMatchers = emailService.sendMatchingQuizInviteEmail();
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentEmail = messageCaptor.getValue();
        assertEquals("You've been invited to take a Matching Quiz for "+testMatcher.getCourseName(), sentEmail.getSubject());
        assertEquals(testMatcher.getStudents().size(), sentEmail.getAllRecipients().length);
        assertEquals(1, activatedMatchers.size());
        assertEquals(testMatcher.getId(), activatedMatchers.get(0).getId());
        assertEquals(MatcherStatus.ACTIVE, activatedMatchers.get(0).getStatus());
    }

    @SneakyThrows
    @Test
    void sendAccountVerificationEmailTest() {
        Admin testAdmin = TestingUtils.createAdmin();
        emailService.sendAccountVerificationEmail(testAdmin);
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentEmail = messageCaptor.getValue();
        assertEquals(List.of(testAdmin.getEmail()), Arrays.stream(sentEmail.getAllRecipients()).map(Address::toString).toList());
        Multipart sentEmailParts = (Multipart) sentEmail.getContent();
        assertTrue(sentEmailParts.getBodyPart(0).getContent().toString().contains("Welcome to groupmatcher!"));
        assertEquals("<bg.png>", ((MimeBodyPart) sentEmailParts.getBodyPart(1)).getContentID());
        assertEquals("<logo.png>", ((MimeBodyPart) sentEmailParts.getBodyPart(2)).getContentID());
        assertEquals("Welcome to groupmatcher!", sentEmail.getSubject());
    }

    @SneakyThrows
    @Test
    void sendReminderTest() {
        Student testStudent = testMatcher.getStudents().get(0);
        Student testStudent1 = testMatcher.getStudents().get(1);
        Student testStudent2 = testMatcher.getStudents().get(2);

        given(matcherRepository.getById(1L)).willReturn(testMatcher);
        emailService.sendReminder(testMatcher);
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentEmail = messageCaptor.getValue();
        assertEquals(List.of(testStudent.getEmail(),testStudent1.getEmail(),testStudent2.getEmail()), Arrays.stream(sentEmail.getAllRecipients()).map(Address::toString).toList());
        Multipart sentEmailParts = (Multipart) sentEmail.getContent();
        assertTrue(sentEmailParts.getBodyPart(0).getContent().toString().contains("Reminder!"));
        assertEquals("<bg.png>", ((MimeBodyPart) sentEmailParts.getBodyPart(1)).getContentID());
        assertEquals("<logo.png>", ((MimeBodyPart) sentEmailParts.getBodyPart(2)).getContentID());
        assertEquals("Reminder", sentEmail.getSubject());
    }

    @SneakyThrows
    @Test()
    void sendMatchedGroupNotificationEmailTest() {
        Team testTeam = new Team();
        testTeam.setId(500L);
        testTeam.setMatcher(testMatcher);
        testTeam.getStudents().addAll(testMatcher.getStudents());
        testMatcher.getTeams().add(testTeam);
        testMatcher.setStatus(MatcherStatus.MATCHED);
        assertFalse(testMatcher.getTeams().get(0).isNotified());
        given(matcherRepository.findByStatus(MatcherStatus.MATCHED)).willReturn(List.of(testMatcher));
        List<Matcher> completedMatchers = emailService.sendMatchedGroupNotificationEmail();
        assertEquals(MatcherStatus.COMPLETED, completedMatchers.get(0).getStatus());
        assertTrue(completedMatchers.get(0).getTeams().get(0).isNotified());
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentEmail = messageCaptor.getValue();
        assertEquals(testTeam.getStudents().stream().map(Student::getEmail).toList(),
                Arrays.stream(sentEmail.getAllRecipients()).map(Address::toString).toList());
        Multipart sentEmailParts = (Multipart) sentEmail.getContent();
        assertEquals("Group Introduction", sentEmail.getSubject());
        assertTrue(sentEmailParts.getBodyPart(0).getContent().toString().contains("Here are your results!"));
        assertEquals("<bg.png>", ((MimeBodyPart) sentEmailParts.getBodyPart(1)).getContentID());
        assertEquals("<logo.png>", ((MimeBodyPart) sentEmailParts.getBodyPart(2)).getContentID());
    }

    @SneakyThrows
    @Test
    void sendResponseVerificationEmailTest() {
        Student testStudent = testMatcher.getStudents().get(0);
        testStudent.setName("Test Student");
        emailService.sendResponseVerificationEmail(testStudent);
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentEmail = messageCaptor.getValue();
        assertEquals(List.of(testStudent.getEmail()), Arrays.stream(sentEmail.getAllRecipients()).map(Address::toString).toList());
    }
}