package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.MatcherDTO;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Question;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.model.Team;
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

@Slf4j
@AllArgsConstructor
@Service
public class MatcherService {

    private MatcherRepository matcherRepository;

    private StudentRepository studentRepository;

    private TeamRepository teamRepository;

    public Matcher createMatcher(MatcherDTO matcherDTO) {
        Matcher newMatcher = new ModelMapper().map(matcherDTO, Matcher.class);
        return matcherRepository.save(newMatcher);
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
