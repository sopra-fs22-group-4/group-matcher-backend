package ch.uzh.soprafs22.groupmatcher.model;

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

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    @Column(nullable = false)
    private boolean verified = false;

    @ManyToMany(mappedBy = "admins",cascade = CascadeType.ALL)
    private Set<Matcher> matchers  = new LinkedHashSet<>();
}
