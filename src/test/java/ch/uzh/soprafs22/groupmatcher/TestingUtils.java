package ch.uzh.soprafs22.groupmatcher;

import ch.uzh.soprafs22.groupmatcher.constant.QuestionCategory;
import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Question;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.model.projections.StudentOverview;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestingUtils {

    private static final SpelAwareProxyProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();

    public static Admin createAdmin(Long adminId) {
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setName("Test Admin");
        admin.setEmail("test@email.com");
        admin.setPassword("test");
        return admin;
    }

    public static Student createStudent(Long studentId, Integer emailNum) {
        Student student = new Student();
        student.setId(studentId);
        student.setEmail("test-%s@email.com".formatted(emailNum));
        return student;
    }

    public static Answer createAnswer(Question question, Integer ordinalNum) {
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setOrdinalNum(ordinalNum);
        return answer;
    }

    public static Question createQuestion(Long questionId, int numAnswers) {
        Question question = new Question();
        question.setId(questionId);
        question.setQuestionCategory(QuestionCategory.KNOWLEDGE);
        question.setQuestionType(QuestionType.SINGLE_CHOICE);
        question.setAnswers(IntStream.range(0, numAnswers).mapToObj(num -> createAnswer(question, num)).toList());
        return question;
    }

    public static Set<Student> createStudents(int numStudents) {
        return IntStream.range(0, numStudents).mapToObj(num -> createStudent(null, num)).collect(Collectors.toSet());
    }

    public static StudentOverview convertToOverview(Student student) {
        return projectionFactory.createProjection(StudentOverview.class, student);
    }

}
