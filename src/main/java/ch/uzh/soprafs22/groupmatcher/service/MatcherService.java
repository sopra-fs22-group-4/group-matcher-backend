package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.*;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherOverview;
import ch.uzh.soprafs22.groupmatcher.repository.AnswerRepository;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import ch.uzh.soprafs22.groupmatcher.repository.TeamRepository;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.ZonedDateTime.now;

@Slf4j
@AllArgsConstructor
@Service
public class MatcherService {

    private MatcherRepository matcherRepository;

    private StudentRepository studentRepository;

    private AnswerRepository answerRepository;

    private TeamRepository teamRepository;

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
        if (quizAnswers.size() != student.getMatcher().getQuestions().size())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Please provide valid answers to all quiz questions");
        student.setSelectedAnswers(quizAnswers);
        student.setSubmissionTimestamp(now());
        return studentRepository.save(student);
    }

    private Map.Entry<Set<Long>, Double> createTeamEntry(Set<Long> teamIds, List<Question> questions) {
        return Map.entry(teamIds, questions.stream().map(question ->
                question.calculateSimilarity(studentRepository.countMostCommonAnswer(question.getId(), teamIds)))
                .mapToDouble(Double::doubleValue).sum());
    }

    private void formTeam(Matcher matcher, Map.Entry<Set<Long>, Double> teamEntry) {
        if (!studentRepository.existsByIdInAndTeamIsNotNull(teamEntry.getKey())) {
            List<Student> teamMembers = studentRepository.findByIdIn(teamEntry.getKey());
            Team newTeam = new Team();
            newTeam.setMatcher(matcher);
            newTeam.setStudents(teamMembers);
            newTeam.setSimilarityScore(teamEntry.getValue());
            teamMembers.forEach(student -> student.setTeam(newTeam));
            matcher.getTeams().add(newTeam);
            teamRepository.save(newTeam);
        }
    }

    public void createTeams(Long matcherId) {
        Matcher matcher = matcherRepository.findById(matcherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Set<Long> studentsToMatch = studentRepository.getStudentsWithoutTeam(matcherId);
        Sets.combinations(studentsToMatch, matcher.getGroupSize())
                .stream().map(team -> createTeamEntry(team, matcher.getQuestions()))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(teamEntry -> formTeam(matcher, teamEntry));
    }

}
