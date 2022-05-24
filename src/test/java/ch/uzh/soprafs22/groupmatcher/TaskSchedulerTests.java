package ch.uzh.soprafs22.groupmatcher;

import ch.uzh.soprafs22.groupmatcher.service.EmailService;
import ch.uzh.soprafs22.groupmatcher.service.MatcherService;
import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {"db-scheduler.enabled=true"})
@Transactional
class TaskSchedulerTests {

    @Autowired
    ApplicationContext context;

    @Autowired
    Scheduler scheduler;

    @Autowired
    Task<Void> sendMatchingQuizInviteEmailTask;

    @Autowired
    Task<Void> initMatchingTask;

    @Autowired
    Task<Void> sendMatchedGroupNotificationEmailTask;

    @MockBean
    MatcherService matcherService;

    @MockBean
    EmailService emailService;

    @Test
    void initTaskSchedulerTest() {
        assertEquals(3, context.getBeansOfType(RecurringTask.class).size());
        assertTrue(scheduler.getSchedulerState().isStarted());
        assertTrue(scheduler.getCurrentlyExecuting().isEmpty());
    }

    @SneakyThrows
    @Test
    void sendMatchingQuizInviteEmailTaskTest() {
        TaskInstance<Void> taskInstance = sendMatchingQuizInviteEmailTask.instance("1");
        scheduler.start();
        scheduler.schedule(taskInstance, Instant.now().plus(3, ChronoUnit.SECONDS));
        scheduler.triggerCheckForDueExecutions();
        Optional<ScheduledExecution<Object>> scheduledExecution = scheduler.getScheduledExecution(taskInstance);
        assertTrue(scheduledExecution.isPresent());
        assertEquals(taskInstance, scheduledExecution.get().getTaskInstance());
    }

    @Test
    void initMatchingTaskTest() {
        TaskInstance<Void> taskInstance = initMatchingTask.instance("1");
        scheduler.start();
        scheduler.schedule(taskInstance, Instant.now().plus(3, ChronoUnit.SECONDS));
        scheduler.triggerCheckForDueExecutions();
        Optional<ScheduledExecution<Object>> scheduledExecution = scheduler.getScheduledExecution(taskInstance);
        assertTrue(scheduledExecution.isPresent());
        assertEquals(taskInstance, scheduledExecution.get().getTaskInstance());
    }

    @Test
    void sendMatchedGroupNotificationEmailTaskTest() {
        TaskInstance<Void> taskInstance = sendMatchedGroupNotificationEmailTask.instance("1");
        scheduler.start();
        scheduler.schedule(taskInstance, Instant.now().plus(3, ChronoUnit.SECONDS));
        scheduler.triggerCheckForDueExecutions();
        Optional<ScheduledExecution<Object>> scheduledExecution = scheduler.getScheduledExecution(taskInstance);
        assertTrue(scheduledExecution.isPresent());
        assertEquals(taskInstance, scheduledExecution.get().getTaskInstance());
    }
}
