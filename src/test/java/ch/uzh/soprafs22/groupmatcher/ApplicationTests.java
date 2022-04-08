package ch.uzh.soprafs22.groupmatcher;

import ch.uzh.soprafs22.groupmatcher.service.EmailService;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(properties = {"db-scheduler.enabled=true", "spring.mail.host=127.0.0.1",
        "spring.mail.port=3025", "spring.mail.username=test", "spring.mail.password=test"})
class ApplicationTests {

    @Autowired
    ApplicationContext context;

    @Autowired
    Scheduler scheduler;

    @Autowired
    Task<Void> checkScheduledEmailsTask;

    @MockBean
    EmailService emailService;

    @Test
    void initEmailsTaskTest() {
        assertEquals(1, context.getBeansOfType(RecurringTask.class).size());
        assertTrue(scheduler.getSchedulerState().isStarted());
        assertTrue(scheduler.getCurrentlyExecuting().isEmpty());
        scheduler.schedule(checkScheduledEmailsTask.instance("1"), Instant.now());
        verify(emailService, times(1)).sendEmailsScheduledForNow();

    }
}
