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

@Slf4j
@Configuration
public class TaskScheduler {

    @Bean
    Task<Void> sendMatchingQuizInviteEmailTask(EmailService emailService) {
        return Tasks
                .recurring("send-invite", fixedDelay(Duration.ofMinutes(3)))
                .execute((taskInstance, executionContext) -> emailService.sendMatchingQuizInviteEmail()
                        .forEach(matcher -> log.info("Successfully activated Matcher {}", matcher.getId())));
    }

    @Bean
    Task<Void> initMatchingTask(MatcherService matcherService) {
        return Tasks
                .recurring("init-matching", fixedDelay(Duration.ofMinutes(3)))
                .execute((taskInstance, executionContext) -> matcherService.initMatching().forEach(matcher ->
                        log.info("Completed matching procedure for Matcher {}", matcher.getId())));
    }

    @Bean
    Task<Void> sendMatchedGroupNotificationEmailTask(EmailService emailService) {
        return Tasks
                .recurring("notify-groups", fixedDelay(Duration.ofMinutes(3)))
                .execute((taskInstance, executionContext) -> emailService.sendMatchedGroupNotificationEmail()
                        .forEach(matcher -> log.info("Successfully completed matching for Matcher {}", matcher.getId())));
    }
}
