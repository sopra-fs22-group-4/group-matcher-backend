package ch.uzh.soprafs22.groupmatcher;

import ch.uzh.soprafs22.groupmatcher.constant.QuestionCategory;
import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.*;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherOverview;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TestingUtils {

    private static final SpelAwareProxyProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();

    public static Admin createAdmin(Long adminId, Matcher matcher) {
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setName("Test Admin");
        admin.setEmail("test@email.com");
        admin.setPassword("test");
        admin.getMatchers().add(matcher);
        return admin;
    }

    public static Admin createAdmin() {
        return createAdmin(1L, null);
    }

    public static Student createStudent(Long studentId, Matcher matcher) {
        Student student = new Student();
        student.setId(studentId);
        student.setEmail("test-%s@email.com".formatted(studentId));
        student.setMatcher(matcher);
        return student;
    }

    public static Answer createAnswer(Long answerId, Question question) {
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setOrdinalNum(question.getAnswers().size()+1);
        answer.setQuestion(question);
        return answer;
    }

    public static Question createQuestion(Long questionId, Matcher matcher) {
        Question question = new Question();
        question.setId(questionId);
        question.setOrdinalNum(matcher.getQuestions().size()+1);
        question.setQuestionCategory(QuestionCategory.KNOWLEDGE);
        question.setQuestionType(QuestionType.SINGLE_CHOICE);
        question.setContent("Test Question");
        question.setMatcher(matcher);
        return question;
    }

    public static Matcher createMatcher(){
        Matcher matcher = new Matcher();
        matcher.setId(1L);
        matcher.setCourseName("Test Course");
        matcher.setUniversity("Test University");
        matcher.setDescription("Test Description");
        matcher.setPublishDate(ZonedDateTime.now().minus(2, ChronoUnit.MINUTES));
        matcher.setDueDate(ZonedDateTime.now().plus(7, ChronoUnit.DAYS));
        matcher.setGroupSize(3);
        matcher.getAdmins().add(createAdmin(2L, matcher));
        Question question1 = createQuestion(10L,  matcher);
        question1.getAnswers().add(createAnswer(11L, question1));
        question1.getAnswers().add(createAnswer(12L, question1));
        matcher.getQuestions().add(question1);
        Question question2 = createQuestion(20L,  matcher);
        question2.getAnswers().add(createAnswer(21L, question2));
        question2.getAnswers().add(createAnswer(22L, question2));
        question2.getAnswers().add(createAnswer(23L, question2));
        question2.getAnswers().add(createAnswer(24L, question2));
        matcher.getQuestions().add(question2);
        matcher.getStudents().add(createStudent(101L, matcher));
        matcher.getStudents().add(createStudent(102L, matcher));
        matcher.getStudents().add(createStudent(103L, matcher));
        return matcher;
    }

    public static MatcherOverview convertToOverview(Matcher matcher) {
        return projectionFactory.createProjection(MatcherOverview.class, matcher);
    }

    public static UserDTO convertToDTO(Admin admin) {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(admin.getName());
        userDTO.setEmail(admin.getEmail());
        userDTO.setPassword(admin.getPassword());
        return userDTO;
    }

    public static UserDTO convertToDTO(Student student) {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Test Student");
        userDTO.setEmail(student.getEmail());
        return userDTO;
    }
}
