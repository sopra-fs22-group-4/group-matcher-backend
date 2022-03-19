package ch.uzh.soprafs22.groupmatcher.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Matcher {

    @Id
    @GeneratedValue
    private Long id;

}
