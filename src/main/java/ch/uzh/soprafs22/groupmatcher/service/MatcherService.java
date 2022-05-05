package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherOverview;
import ch.uzh.soprafs22.groupmatcher.repository.AnswerRepository;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static java.time.ZonedDateTime.now;

@Slf4j
@AllArgsConstructor
@Service
public class MatcherService {

    private MatcherRepository matcherRepository;

    private StudentRepository studentRepository;

    private AnswerRepository answerRepository;

    public Student getStudent(Long matcherId, String studentEmail) {
        return studentRepository.getByMatcherIdAndEmail(matcherId, studentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid email address"));
    }

    public MatcherOverview getMatcherOverview(Long matcherId) {
        return matcherRepository.findMatcherById(matcherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    public Student findMatcherStudent(Long matcherId, UserDTO studentDTO) {
        Student student = getStudent(matcherId, studentDTO.getEmail());
        if (!Strings.isNullOrEmpty(studentDTO.getName()))
            student.setName(studentDTO.getName());
        return studentRepository.save(student);
    }

    public Student submitStudentAnswers(Long matcherId, String studentEmail, List<Long> answerIds) {
        Student student = getStudent(matcherId, studentEmail);
        List<Answer> quizAnswers = answerRepository.findByIdInAndQuestion_Matcher_Id(answerIds, matcherId);
        if (quizAnswers.size() < student.getMatcher().getQuestions().size())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Please provide valid answers to all quiz questions");
        student.setSelectedAnswers(quizAnswers);
        student.setSubmissionTimestamp(now());
        return studentRepository.save(student);
    }

    public double[][] createAnswerMatrixAll(Long matcherId) {
        Matcher matcher = matcherRepository.findById(matcherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No matcher found for the given ID"));
        List<Long> answersIds = matcher.getQuestions().stream().flatMap(question -> question.getAnswers().stream().map(Answer::getId)).toList();

        return matcher.getStudents().stream().map(student ->
                answersIds.stream().map(answerId -> student.getSelectedAnswers()
                    .stream().filter(selectedAnswer -> selectedAnswer.getId().equals(answerId)).findFirst()
                    .map(selectedAnswer -> selectedAnswer.getQuestion().getAnswers().size()).orElse(0))
                    .mapToDouble(Integer::doubleValue).toArray()).toArray(double[][]::new);
    }
}
