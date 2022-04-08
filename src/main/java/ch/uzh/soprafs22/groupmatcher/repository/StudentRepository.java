package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT student.id FROM Student student WHERE student.matcher.id = :matcherId " +
            "AND size(student.answers) = size(student.matcher.questions) AND student.team IS NULL")
    Set<Long> getStudentsWithoutTeam(@Param("matcherId") Long matcherId);

    @Query(value = "SELECT max(team_answers.frequency) FROM " +
            "(SELECT count(student_answer.answers_id) AS frequency FROM student_answers student_answer " +
            "WHERE student_answer.answers_id IN (SELECT answer.id FROM answer answer WHERE answer.question_id = :questionId) " +
            "AND student_answer.student_id IN :studentsIds GROUP BY student_answer.answers_id) AS team_answers", nativeQuery = true)
    Integer countMostCommonAnswer(@Param("questionId") Long questionId, @Param("studentsIds") Set<Long> studentsIds);

    List<Student> findByIdIn(Set<Long> studentsIds);

    boolean existsByIdInAndTeamIsNotNull(Set<Long> studentsIds);

}