package ch.uzh.soprafs22.groupmatcher.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AdminStatus {
    INVITED, REGISTERED, VERIFIED;

    @JsonValue
    public String getName() {
        return name().toLowerCase();
    }
}
