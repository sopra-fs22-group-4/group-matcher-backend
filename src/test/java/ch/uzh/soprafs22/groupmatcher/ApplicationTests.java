package ch.uzh.soprafs22.groupmatcher;

import ch.uzh.soprafs22.groupmatcher.constant.MatchingStrategy;
import ch.uzh.soprafs22.groupmatcher.controller.AdminController;
import ch.uzh.soprafs22.groupmatcher.controller.MatcherController;
import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherAdminOverview;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
class ApplicationTests {

    @MockBean
    JavaMailSender mailSender;

    @Autowired
    AdminController adminController;

    @Autowired
    MatcherController matcherController;

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
        testMatcherDTO.setMatchingStrategy(MatchingStrategy.MOST_SIMILAR);
        testMatcherDTO.setPublishDate(ZonedDateTime.now().plus(1, ChronoUnit.DAYS));
        testMatcherDTO.setPublishDate(ZonedDateTime.now().plus(3, ChronoUnit.DAYS));
        testMatcherDTO.setGroupSize(5);
        Matcher createdMatcher = adminController.createMatcher(storedAdmin.getId(), testMatcherDTO);
        List<MatcherAdminOverview> matchers = adminController.getMatchers(storedAdmin.getId());
        assertEquals(1, matchers.size());
        assertEquals(createdMatcher.getId(), matchers.get(0).getId());
    }
}
