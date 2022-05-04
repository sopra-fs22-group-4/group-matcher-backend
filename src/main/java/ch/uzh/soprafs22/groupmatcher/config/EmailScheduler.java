package ch.uzh.soprafs22.groupmatcher.config;

import ch.uzh.soprafs22.groupmatcher.service.EmailService;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static com.github.kagkarlsson.scheduler.task.schedule.Schedules.fixedDelay;
import static java.time.ZonedDateTime.now;

@Slf4j
@Configuration
public class EmailScheduler {

    @Bean
    Task<Void> checkScheduledEmailsTask(EmailService emailService) {
        return Tasks
                .recurring("check-scheduled-emails", fixedDelay(Duration.ofMinutes(1)))
                .execute((taskInstance, executionContext) -> {
                    log.info("Checking if there are emails scheduled for {}", now());
                    emailService.activatePublishedMatchers();
                });
    }
}
