package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.repository.AdminRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@AllArgsConstructor
@Service
public class AdminService {

    private AdminRepository adminRepository;

    public Admin createAdmin(UserDTO userDTO){
        if (adminRepository.existsByEmail(userDTO.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with the given Email already exists");
        Admin newAdmin = new ModelMapper().map(userDTO, Admin.class);
        return adminRepository.save(newAdmin);
    }

    public Admin checkValidLogin(UserDTO userDTO){
        Admin storedUser = adminRepository.findByEmail(userDTO.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for the given Email"));
        if (!storedUser.getPassword().equals(userDTO.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password, please try again");
        if (!storedUser.isVerified())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unverified account, please check your inbox");
        return storedUser;
    }

    public Admin verifyAccount(Long adminId) {
        Admin storedUser = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for the given ID"));
        storedUser.setVerified(true);
        return adminRepository.save(storedUser);
    }
}
