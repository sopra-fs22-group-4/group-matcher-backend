package ch.uzh.soprafs22.groupmatcher.config;

import ch.uzh.soprafs22.groupmatcher.service.EmailService;
import ch.uzh.soprafs22.groupmatcher.service.MatcherService;
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
public class TaskScheduler {

    @Bean
    Task<Void> sendMatchingQuizInviteEmailTask(EmailService emailService) {
        return Tasks
                .recurring("send-invite", fixedDelay(Duration.ofMinutes(1)))
                .execute((taskInstance, executionContext) -> {
                    log.info("Checking if there are matchers scheduled to be published before {}", now());
                    emailService.sendMatchingQuizInviteEmail();
                });
    }

    @Bean
    Task<Void> initMatchingTask(MatcherService matcherService) {
        return Tasks
                .recurring("init-matching", fixedDelay(Duration.ofMinutes(1)))
                .execute((taskInstance, executionContext) -> {
                    log.info("Checking if there are matchers due before {}", now());
                    matcherService.initMatching();
                });
    }

    @Bean
    Task<Void> sendMatchedGroupNotificationEmailTask(EmailService emailService) {
        return Tasks
                .recurring("notify-groups", fixedDelay(Duration.ofMinutes(1)))
                .execute((taskInstance, executionContext) -> {
                    log.info("Checking if any of the active matching procedures are finished...");
                    emailService.sendMatchedGroupNotificationEmail();
                });
    }
}
