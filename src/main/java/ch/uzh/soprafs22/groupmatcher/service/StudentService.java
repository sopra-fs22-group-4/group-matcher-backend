package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.model.Student;
import ch.uzh.soprafs22.groupmatcher.repository.StudentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@AllArgsConstructor
@Service
public class StudentService {

    private StudentRepository studentRepository;

    public Student checkValidEmail(Long matcherId, String studentEmail){
        return studentRepository.findByMatcher_IdAndEmail(matcherId,studentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Invalid email address, please check your email address"));
    }
}
