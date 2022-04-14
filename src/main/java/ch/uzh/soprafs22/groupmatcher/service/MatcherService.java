package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.model.*;
import ch.uzh.soprafs22.groupmatcher.repository.AnswerRepository;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import ch.uzh.soprafs22.groupmatcher.repository.TeamRepository;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class MatcherService {

    private MatcherRepository matcherRepository;

    private StudentRepository studentRepository;

    private AnswerRepository answerRepository;

    private TeamRepository teamRepository;

    public Matcher createMatcher(MatcherDTO matcherDTO) {
        Matcher newMatcher = new ModelMapper().map(matcherDTO, Matcher.class);
        return matcherRepository.save(newMatcher);
    }

    public Student checkValidEmail(Long matcherId, String studentEmail) {
        return studentRepository.findByMatcherIdAndEmail(matcherId, studentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid email address"));
    }

    public Student submitStudentAnswers(Student student, List<Long> answerIds) {
        Set<Answer> quizAnswers = answerIds.stream().map(answerId -> // Verify the answer belongs to the student's matcher
                answerRepository.findByIdAndQuestion_Matcher_Id(answerId, student.getMatcher().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid answer ID"))).collect(Collectors.toSet());
        student.setAnswers(quizAnswers);
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
