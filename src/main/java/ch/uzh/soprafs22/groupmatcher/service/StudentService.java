package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.AnswerDTO;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.AnswerRepository;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class StudentService {

    private StudentRepository studentRepository;
    private AnswerRepository answerRepository;

    public Student checkValidEmail(Long matcherId, String studentEmail){
        return studentRepository.findByMatcherIdAndEmail(matcherId,studentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Invalid email address, please check your email address"));
    }

    public void updateAnswer(Long studentId, Long questionId, List<AnswerDTO> answerDTOs){

        Student student = studentRepository.getById(studentId);
        for (AnswerDTO answerDTO:answerDTOs){
            Optional<Answer> answer = answerRepository.findByQuestionIdAndOrdinalNum(questionId, answerDTO.getOrdinalNum());

            if (answer.isPresent()){
                student.getAnswers().add(answer.get());
                answer.get().getStudents().add(student);
            }else{
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested answer is invalid.");
            }
        }
    }
}
