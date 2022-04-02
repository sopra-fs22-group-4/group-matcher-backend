package ch.uzh.soprafs22.groupmatcher.service;

import ch.uzh.soprafs22.groupmatcher.dto.UserDTO;
import ch.uzh.soprafs22.groupmatcher.model.Admin;
import ch.uzh.soprafs22.groupmatcher.repository.AdminRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@AllArgsConstructor
@Service
public class AdminService {

    private AdminRepository adminRepository;
    private ModelMapper mapper;

    public Admin createAdmin(UserDTO userDTO){

        mapper = new ModelMapper();
        Admin newAdmin = mapper.map(userDTO, Admin.class);

        checkIfAdminExists(newAdmin);

        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newAdmin = adminRepository.save(newAdmin);
        adminRepository.flush();

        log.debug("Created Information for admin: {}", newAdmin);

        return newAdmin;
    }

    public Admin checkValidLogin(UserDTO userDTO){

        String BaseErrorMessage;

        mapper = new ModelMapper();
        Admin admin = mapper.map(userDTO, Admin.class);

        String email = admin.getEmail();
        String password = admin.getPassword();

        Admin adminByEmail = adminRepository.findByEmail(email);

        if (adminByEmail == null) {
            BaseErrorMessage = "The %s provided is not registered. Please register first!";
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    String.format(BaseErrorMessage, email));
        }else{
            if (!adminByEmail.isVerified()){
                BaseErrorMessage = "The %s provided is not verified. Please check your verification email first!";
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        String.format(BaseErrorMessage, email));
            }else if(!adminByEmail.getPassword().equals(password)){
                BaseErrorMessage = "The password is not correct. Please check it again!";
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        BaseErrorMessage);
            }
        }

        log.debug("Login Information: {}", admin);

        return adminByEmail;

    }

    private void checkIfAdminExists(Admin newAdmin) {

        String email = newAdmin.getEmail();
        Admin adminByEmail = adminRepository.findByEmail(email);

        String baseErrorMessage = "The admin %s is already registered.";
        if (adminByEmail != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format(baseErrorMessage, email));
        }
    }
}
