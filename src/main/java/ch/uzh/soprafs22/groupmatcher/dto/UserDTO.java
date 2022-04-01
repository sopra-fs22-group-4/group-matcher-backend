package ch.uzh.soprafs22.groupmatcher.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private String email;
    private String password;
    private String name;
}
