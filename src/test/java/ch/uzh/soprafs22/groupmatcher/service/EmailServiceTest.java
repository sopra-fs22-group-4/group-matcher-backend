package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {EmailService.class})
class EmailServiceTest {

    @Autowired
    EmailService emailService;

    @MockBean
    MatcherRepository matcherRepository;

    @MockBean
    JavaMailSender mailSender;

    @Captor
    ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @Test
    void activatePublishedMatchersTest() {
        Matcher testMatcher = new Matcher();
        testMatcher.setId(1L);
        testMatcher.setPublishDate(ZonedDateTime.now().minus(2, ChronoUnit.MINUTES));
        testMatcher.setDueDate(ZonedDateTime.now().minus(2, ChronoUnit.DAYS));
        testMatcher.setStudents(Set.of(TestingUtils.createStudent(11L,0)));
        given(matcherRepository.save(any())).willAnswer(returnsFirstArg());
        given(matcherRepository.findByPublishDateIsAfterAndActiveFalse(any())).willReturn(List.of(testMatcher));
        assertFalse(testMatcher.isActive());
        List<Matcher> activatedMatchers = emailService.activatePublishedMatchers();
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentEmail = messageCaptor.getValue();
        assertEquals("Link to Matching Quiz", sentEmail.getSubject());
        assertNotNull(sentEmail.getTo());
        assertEquals(testMatcher.getStudents().size(), sentEmail.getTo().length);
        assertEquals(1, activatedMatchers.size());
        assertEquals(testMatcher.getId(), activatedMatchers.get(0).getId());
        assertTrue(activatedMatchers.get(0).isActive());
    }

    @Test
    void sendAccountVerificationEmailTest() {
        Admin testAdmin = TestingUtils.createAdmin(1L);
        emailService.sendAccountVerificationEmail(testAdmin);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentEmail = messageCaptor.getValue();
        assertNotNull(sentEmail.getTo());
        assertEquals(List.of(testAdmin.getEmail()), Arrays.stream(sentEmail.getTo()).toList());
    }

    @Test
    void sendResponseVerificationEmailTest() {
        Matcher testMatcher = new Matcher();
        testMatcher.setId(1L);
        Student testStudent = TestingUtils.createStudent(2L, 2);
        testStudent.setMatcher(testMatcher);
        emailService.sendResponseVerificationEmail(testStudent);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentEmail = messageCaptor.getValue();
        assertNotNull(sentEmail.getTo());
        assertEquals(List.of(testStudent.getEmail()), Arrays.stream(sentEmail.getTo()).toList());
    }
}
