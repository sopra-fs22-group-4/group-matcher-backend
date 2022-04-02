package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

public interface EmailRepository extends JpaRepository<Email, Long> {

    List<Email> findBySendAtIsBeforeAndSentFalse(ZonedDateTime sendAt);

    @Transactional
    @Modifying
    @Query("UPDATE Email email SET email.sent = true WHERE email.id = :id")
    void markEmailAsSent(@Param("id") Long id);

}