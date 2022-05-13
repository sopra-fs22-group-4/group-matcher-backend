package ch.uzh.soprafs22.groupmatcher.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionCategory {
    PREFERENCES(true), SKILLS(false);

    private final boolean similarityMatching;

    public Double calculateBalancedMatchingScore(boolean isSharedAnswer) {
        return isSharedAnswer == similarityMatching ? 1.0 : 0.0;
    }
}
