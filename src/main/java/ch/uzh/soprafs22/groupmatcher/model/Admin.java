package ch.uzh.soprafs22.groupmatcher.model;

import ch.uzh.soprafs22.groupmatcher.constant.AdminStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Admin {
    @Id
    @GeneratedValue
    private Long id;

    private String email;

    @JsonIgnore
    private String password;

    private String name;

    @JsonIgnore
    private AdminStatus status;

    @JsonIgnore
    @ManyToMany(mappedBy = "collaborators",cascade = CascadeType.ALL)
    private Set<Matcher> matchers  = new LinkedHashSet<>();

    public boolean getHasPassword() {
        return !Strings.isNullOrEmpty(password);
    }

    public boolean isVerified() {
        return status == AdminStatus.VERIFIED;
    }
}
