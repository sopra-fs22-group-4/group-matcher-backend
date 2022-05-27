package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.constant.MatcherStatus;
import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.model.Team;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherOverview;
import ch.uzh.soprafs22.groupmatcher.repository.AnswerRepository;
import ch.uzh.soprafs22.groupmatcher.repository.MatcherRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import ch.uzh.soprafs22.groupmatcher.repository.TeamRepository;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
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
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tutorial.clustering.SameSizeKMeansAlgorithm;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
        student.setSelectedAnswers(quizAnswers);
        student.setSubmissionTimestamp(now());
        return studentRepository.save(student);
    }

    @Transactional
    public List<Matcher> initMatching() {
        return matcherRepository.findByDueDateIsBeforeAndStatus(ZonedDateTime.now(), MatcherStatus.ACTIVE)
                .stream().map(matcher -> {
                    matcher.setStatus(MatcherStatus.MATCHING);
                    Matcher updatedMatcher = matcherRepository.save(matcher);
                    log.info("Initialising matching procedure for Matcher {}", matcher.getId());
                    return switch (updatedMatcher.getMatchingStrategy()) {
                        case MOST_SIMILAR -> runMostSimilarModel(updatedMatcher);
                        case BALANCED_SKILLS -> runBalancedSkillsModel(updatedMatcher);
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
        matcher.setStatus(MatcherStatus.MATCHED);
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

    public Matcher runBalancedSkillsModel(Matcher matcher) {
        Graph<Team, DefaultWeightedEdge> studentsGraph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(studentsGraph, matcher.getStudents().stream().map(Team::new).toList());
        Sets.combinations(studentsGraph.vertexSet(), 2).forEach(studentsPairSet -> {
            Iterator<Team> studentsPair = studentsPairSet.iterator();
            Team student1 = studentsPair.next();
            Team student2 = studentsPair.next();
            Long student2Id = student2.getStudents().get(0).getId();
            double matchingScore = student1.getStudents().get(0).getSelectedAnswers().stream().mapToDouble(selectedAnswer ->
                    selectedAnswer.calculateBalancedMatchingScore(student2Id)).sum();
            studentsGraph.setEdgeWeight(studentsGraph.addEdge(student1, student2), matchingScore);
        });
        while (!studentsGraph.edgeSet().isEmpty())
            studentsGraph.edgeSet().stream().max(Comparator.comparingDouble(studentsGraph::getEdgeWeight)).ifPresent(edge -> {
                Team newMembers = studentsGraph.getEdgeSource(edge);
                Team team = studentsGraph.getEdgeTarget(edge);
                if ((team.getStudents().size() + newMembers.getStudents().size()) > matcher.getGroupSize())
                    studentsGraph.removeEdge(edge);
                else {
                    team.getMatchingScores().add(studentsGraph.getEdgeWeight(edge));
                    Graphs.neighborListOf(studentsGraph, newMembers).forEach(newMembersNeighbor ->
                            Optional.ofNullable(studentsGraph.getEdge(team, newMembersNeighbor)).ifPresent(teamToNeighborEdge -> {
                                DefaultWeightedEdge newMembersNeighborEdge = studentsGraph.getEdge(newMembers, newMembersNeighbor);
                                double newMembersToNeighborWeight = studentsGraph.getEdgeWeight(newMembersNeighborEdge);
                                double teamToNeighborWeight = studentsGraph.getEdgeWeight(teamToNeighborEdge);
                                if (newMembersToNeighborWeight < teamToNeighborWeight)
                                    studentsGraph.setEdgeWeight(teamToNeighborEdge, newMembersToNeighborWeight);
                                studentsGraph.removeEdge(newMembersNeighborEdge);
                            }));
                    studentsGraph.removeVertex(newMembers);
                    team.getStudents().addAll(newMembers.getStudents());
                }
            });
        studentsGraph.vertexSet().forEach(team -> {
            team.setMatcher(matcher);
            team.getStudents().forEach(student -> student.setTeam(team));
            matcher.getTeams().add(team);
            teamRepository.save(team);
        });
        matcher.setStatus(MatcherStatus.MATCHED);
        return matcherRepository.save(matcher);
    }
}
