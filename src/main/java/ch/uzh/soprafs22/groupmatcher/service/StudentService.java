package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class StudentService {

    private StudentRepository studentRepository;

    public Student checkValidEmail(Long matcherId, UserDTO userDTO){

        Student validStudent = null;
        Set<Long> studentsId = studentRepository.getStudentsByMatcherId(matcherId);
        List<Student> students = studentRepository.findByIdIn(studentsId);
        for (Student student : students) {
            if (student.getEmail().equals(userDTO.getEmail())){
                validStudent = student;
                break;
            }
        }
        if (validStudent==null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid account, please check your email address");
        }
        return validStudent;
    }
}
