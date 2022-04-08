package ch.uzh.soprafs22.groupmatcher;

import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Question;
import ch.uzh.soprafs22.groupmatcher.model.Student;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestingUtils {

    public static Student createStudent(Long studentId) {
        Student student = new Student();
        student.setId(studentId);
        student.setEmail("test-%s@email.com".formatted(studentId));
        return student;
    }

    public static Answer createAnswer(Question question) {
        Answer answer = new Answer();
        answer.setQuestion(question);
        return answer;
    }

    public static Question createQuestion(Long questionId, int numAnswers) {
        Question question = new Question();
        question.setId(questionId);
        question.setQuestionType(QuestionType.SINGLE_CHOICE);
        question.setAnswers(IntStream.range(0, numAnswers).mapToObj(num -> createAnswer(question)).toList());
        return question;
    }

    public static Set<Student> createStudents(int numStudents) {
        return IntStream.range(0, numStudents).mapToObj(num -> createStudent(null)).collect(Collectors.toSet());
    }

}
