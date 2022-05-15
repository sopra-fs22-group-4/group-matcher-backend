package ch.uzh.soprafs22.groupmatcher.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.text.WordUtils;

@Getter
@AllArgsConstructor
public enum QuestionCategory {
    PREFERENCES(true), SKILLS(false);

    private final boolean similarityMatching;

    public Double calculateBalancedMatchingScore(boolean isSharedAnswer) {
        return isSharedAnswer == similarityMatching ? 1.0 : 0.0;
    }

    @JsonValue
    public String getName() {
        return WordUtils.capitalizeFully(name().toLowerCase().replace("_", " "));
    }
}
