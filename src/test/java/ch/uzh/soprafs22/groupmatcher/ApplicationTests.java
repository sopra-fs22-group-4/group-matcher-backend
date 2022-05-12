package ch.uzh.soprafs22.groupmatcher;

import ch.uzh.soprafs22.groupmatcher.controller.AdminController;
import ch.uzh.soprafs22.groupmatcher.controller.MatcherController;
import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherAdminOverview;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {"db-scheduler.enabled=true"})
@Transactional
class ApplicationTests {

    @Autowired
    ApplicationContext context;

    @Autowired
    Scheduler scheduler;

    @Autowired
    Task<Void> activateScheduledMatchersTask;

    @MockBean
    JavaMailSender mailSender;

    @Autowired
    AdminController adminController;

    @Autowired
    MatcherController matcherController;

    @Test
    void initEmailsTaskTest() {
        assertEquals(2, context.getBeansOfType(RecurringTask.class).size());
        assertTrue(scheduler.getSchedulerState().isStarted());
        assertTrue(scheduler.getCurrentlyExecuting().isEmpty());
        scheduler.schedule(activateScheduledMatchersTask.instance("1"), Instant.now());
    }

    @Test
    void adminIntegrationTest() {
        UserDTO testUserDTO = new UserDTO();
        testUserDTO.setName("Test Admin");
        testUserDTO.setEmail("test-admin@email.com");
        testUserDTO.setPassword("test");
        JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
        given(mailSender.createMimeMessage()).willReturn(mailSenderImpl.createMimeMessage());
        Long createdAdminId = adminController.createAdmin(testUserDTO);
        Admin storedAdmin = adminController.verifyAccount(createdAdminId);
        assertEquals(createdAdminId, storedAdmin.getId());
        assertEquals(testUserDTO.getName(), storedAdmin.getName());
        assertEquals(testUserDTO.getEmail(), storedAdmin.getEmail());
        assertEquals(testUserDTO.getPassword(), storedAdmin.getPassword());
        MatcherDTO testMatcherDTO = new MatcherDTO();
        testMatcherDTO.setCourseName("Test Course");
        testMatcherDTO.setUniversity("Test University");
        testMatcherDTO.setDescription("Test Description");
        testMatcherDTO.setPublishDate(ZonedDateTime.now().plus(1, ChronoUnit.DAYS));
        testMatcherDTO.setPublishDate(ZonedDateTime.now().plus(3, ChronoUnit.DAYS));
        testMatcherDTO.setGroupSize(5);
        Long createdMatcherId = adminController.createMatcher(storedAdmin.getId(), testMatcherDTO);
        List<MatcherAdminOverview> matchers = adminController.getMatchers(storedAdmin.getId());
        assertEquals(1, matchers.size());
        assertEquals(createdMatcherId, matchers.get(0).getId());
    }
}
