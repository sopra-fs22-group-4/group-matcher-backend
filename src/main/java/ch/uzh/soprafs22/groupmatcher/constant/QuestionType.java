package ch.uzh.soprafs22.groupmatcher.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.text.WordUtils;

public enum QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE, TEXT;

    @JsonValue
    public String getName() {
        return WordUtils.capitalizeFully(name().toLowerCase().replace("_", " "));
    }
}
