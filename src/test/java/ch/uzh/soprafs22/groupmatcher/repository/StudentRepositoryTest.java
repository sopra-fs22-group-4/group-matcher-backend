package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.TestingUtils;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Question;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class StudentRepositoryTest {

    @Autowired
    private MatcherRepository matcherRepository;

    @Autowired
    private StudentRepository studentRepository;

    private Matcher testMatcher;

    @BeforeEach
    void setup() {
        testMatcher = new Matcher();
        testMatcher.setGroupSize(3);
        testMatcher.setStudents(TestingUtils.createStudents(4));
        Question question = TestingUtils.createQuestion(null, 4);
        question.setMatcher(testMatcher);
        testMatcher.setQuestions(List.of(question));
    }

    @Transactional
    @Test
    void countMostCommonAnswer_allSameAnswer() {
        Matcher storedMatcher = matcherRepository.save(testMatcher);
        Question question = storedMatcher.getQuestions().get(0);
        Answer mostCommonAnswer = question.getAnswers().get(0);
        storedMatcher.getStudents().forEach(student -> {
            mostCommonAnswer.getStudents().add(student);
            Student storedStudent = studentRepository.findById(student.getId()).orElseThrow(EntityNotFoundException::new);
            storedStudent.getAnswers().add(mostCommonAnswer);
            studentRepository.save(student);
        });
        Set<Long> studentsIds = storedMatcher.getStudents().stream().map(Student::getId).collect(Collectors.toSet());
        assertEquals(studentsIds.size(), studentRepository.countMostCommonAnswer(question.getId(), studentsIds));
    }

    @Transactional
    @Test
    void countMostCommonAnswer_tie() {
        Matcher storedMatcher = matcherRepository.save(testMatcher);
        Question question = storedMatcher.getQuestions().get(0);
        AtomicInteger index = new AtomicInteger();
        storedMatcher.getStudents().forEach(student -> {
            Answer selectedAnswer = question.getAnswers().get(index.getAndIncrement() % 2);
            selectedAnswer.getStudents().add(student);
            Student storedStudent = studentRepository.findById(student.getId()).orElseThrow(EntityNotFoundException::new);
            storedStudent.getAnswers().add(selectedAnswer);
            studentRepository.save(storedStudent);
        });
        Set<Long> studentsIds = storedMatcher.getStudents().stream().map(Student::getId).collect(Collectors.toSet());
        assertEquals(studentsIds.size() / 2, studentRepository.countMostCommonAnswer(question.getId(), studentsIds));
    }

    @Transactional
    @Test
    void countMostCommonAnswer_allDifferentAnswers() {
        Matcher storedMatcher = matcherRepository.save(testMatcher);
        Question question = storedMatcher.getQuestions().get(0);
        AtomicInteger index = new AtomicInteger();
        storedMatcher.getStudents().forEach(student -> {
            Answer selectedAnswer = question.getAnswers().get(index.getAndIncrement());
            selectedAnswer.getStudents().add(student);
            Student storedStudent = studentRepository.findById(student.getId()).orElseThrow(EntityNotFoundException::new);
            storedStudent.getAnswers().add(selectedAnswer);
            studentRepository.save(storedStudent);
        });
        Set<Long> studentsIds = storedMatcher.getStudents().stream().map(Student::getId).collect(Collectors.toSet());
        assertEquals(1, studentRepository.countMostCommonAnswer(question.getId(), studentsIds));
    }
}
