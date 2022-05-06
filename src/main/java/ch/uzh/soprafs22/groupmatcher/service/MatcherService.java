package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
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
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

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

    public String[] getStudentEmails(Long matcherId) {
        return studentRepository.getAllStudentsEmailByMatcherId(matcherId);
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

    public double[][] createAnswerMatrixAll(Long matcherId) {

        double[][] studentAnswerMatrixSingle = createAnswerMatrixByQuestionType(matcherId, QuestionType.SINGLE_CHOICE);
        double[][] studentAnswerMatrixMulti = createAnswerMatrixByQuestionType(matcherId, QuestionType.MULTIPLE_CHOICE);
        double[][] studentAnswerMatrixAll = new double[studentAnswerMatrixSingle.length][];

        for (int i = 0; i<studentAnswerMatrixSingle.length; i++) {
            studentAnswerMatrixAll[i] = ArrayUtils.addAll(studentAnswerMatrixSingle[i], studentAnswerMatrixMulti[i]);
        }
        return studentAnswerMatrixAll;
    }

    public double[][] createAnswerMatrixByQuestionType(Long matcherId, QuestionType questionType) {

        List<Long> studentIdListOrdered = studentRepository.getAllStudentsIdByMatcherId(matcherId);
        int studentNumbers = studentIdListOrdered.size();

        // stdIdListOrdered Q1-A1   Q1-A2   Q1-A3   Q2-A1   Q2-A2   Q2-A3   Q2-A4
        // student1(0l)     1       0       1       0       1       0       1
        // student2(1l)     0       0       1       0       1       0       0

        List<Answer> answerList = answerRepository.findByQuestion_Matcher_IdAndQuestion_QuestionTypeOrderByIdAsc(matcherId,questionType);

        double[] temp = new double[answerList.size()];
        double[][] studentAnswerMatrix = new double[studentNumbers][answerList.size()];

        for (int i = 0; i < studentIdListOrdered.size(); i++){
            Optional<Student> student = studentRepository.findById(studentIdListOrdered.get(i));
            for (int j = 0; j < answerList.size(); j++) {
                if (student.isPresent()){
                    temp[j] = studentRepository.existsByIdAndSelectedAnswers_Id(student.get().getId()
                            ,answerList.get(j).getId()) ? 1 : 0;
                }
            }
            studentAnswerMatrix[i] = temp;
        }
        return studentAnswerMatrix;
    }
}
