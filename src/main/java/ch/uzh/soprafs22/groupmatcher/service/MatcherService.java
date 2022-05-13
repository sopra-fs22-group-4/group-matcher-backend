package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.constant.Status;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.model.Team;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherOverview;
import ch.uzh.soprafs22.groupmatcher.repository.AnswerRepository;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import ch.uzh.soprafs22.groupmatcher.service.matching.DataProcessing;
import ch.uzh.soprafs22.groupmatcher.service.matching.Prim;
import ch.uzh.soprafs22.groupmatcher.service.matching.Vertex;
import com.google.common.base.Strings;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.KMeansInitialization;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomUniformGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.MeanModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.utilities.random.RandomFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tutorial.clustering.SameSizeKMeansAlgorithm;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

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

    public List<Matcher> initMatching() {
        return matcherRepository.findByDueDateIsAfterAndStatus(ZonedDateTime.now(), Status.ACTIVE)
                .stream().map(matcher -> {
                    matcher.setStatus(Status.MATCHING);
                    Matcher updatedMatcher = matcherRepository.save(matcher);
                    return switch (updatedMatcher.getMatchingStrategy()) {
                        case MOST_SIMILAR -> runMostSimilarModel(updatedMatcher);
                        case BALANCED_SKILLS -> runBalancedSkillsModelTemp(updatedMatcher);
                    };
                }).toList();
    }

    public Matcher runMostSimilarModel(Matcher matcher) {
        double[][] answerMatrix = buildAnswersMatrixForMatcher(matcher);
        String[] studentEmails = matcher.getStudents().stream().map(Student::getEmail).toArray(String[]::new);
        DatabaseConnection databaseConnection = new ArrayAdapterDatabaseConnection(answerMatrix, studentEmails);
        Database database = new StaticArrayDatabase(databaseConnection);
        database.initialize();
        Relation<NumberVector> dataColumn = database.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        Relation<String> emailsColumn = database.getRelation(TypeUtil.STRING);
        KMeansInitialization initializer = new RandomUniformGeneratedInitialMeans(new RandomFactory(0));
        SameSizeKMeansAlgorithm<NumberVector> model = new SameSizeKMeansAlgorithm<>(
                EuclideanDistanceFunction.STATIC, matcher.getGroupSize(), -1, initializer);

        Clustering<MeanModel> matcherModel = model.run(database, dataColumn);
        matcherModel.getAllClusters().forEach(matchedTeam -> {
            Team newTeam = new Team();
            newTeam.setMatcher(matcher);
            for (DBIDIter student = matchedTeam.getIDs().iter(); student.valid(); student.advance()) {
                String studentEmail = emailsColumn.get(student);
                Student storedStudent = getStudent(matcher.getId(), studentEmail);
                newTeam.getStudents().add(storedStudent);
                storedStudent.setTeam(newTeam);
            }
            matcher.getTeams().add(newTeam);
        });
        matcher.setStatus(Status.MATCHED);
        return matcherRepository.save(matcher);
    }

    public double[][] buildAnswersMatrixForMatcher(Matcher matcher) {
        List<Long> answersIds = matcher.getQuestions().stream().flatMap(question ->
                question.getAnswers().stream().map(Answer::getId)).toList();

        return matcher.getStudents().stream().map(student ->
                answersIds.stream().map(answerId -> student.getSelectedAnswers()
                    .stream().filter(selectedAnswer -> selectedAnswer.getId().equals(answerId)).findFirst()
                    .map(selectedAnswer -> selectedAnswer.getQuestion().getAnswers().size()).orElse(0))
                    .mapToDouble(Integer::doubleValue).toArray()).toArray(double[][]::new);
    }

    public Matcher runBalancedSkillsModelTemp(Matcher matcher){

        DataProcessing dataProcessing = new DataProcessing();

        String[] studentEmails = matcher.getStudents().stream().map(Student::getEmail).toArray(String[]::new);
        double[][] totalScoreMatrix = dataProcessing.calMatchingScoreTemp(matcher.getStudents());

        List<Vertex> initGraph = dataProcessing.adjMatrixToVertexList(totalScoreMatrix,studentEmails);

        int groupSize = matcher.getGroupSize();
        Prim prim = new Prim(initGraph);

        while(!prim.getGraph().isEmpty()){
            Team newTeam = new Team();
            newTeam.setMatcher(matcher);

            prim.run(groupSize,false);

            List<Vertex> newTeamMembers = prim.getVisitedVertex();

            for (Vertex student: newTeamMembers) {
                Student storedStudent = matcher.getStudents().stream()
                        .filter(std -> Objects.equals(std.getEmail(), student.getEmail())).findFirst().orElse(null);
                newTeam.getStudents().add(storedStudent);
                if(null != storedStudent){
                    storedStudent.setTeam(newTeam);
                }
            }
            matcher.getTeams().add(newTeam);

            prim.deleteVisitedVertex();
        }

        return matcherRepository.save(matcher);
    }
}
