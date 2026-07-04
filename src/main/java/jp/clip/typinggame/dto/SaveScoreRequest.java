package jp.clip.typinggame.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SaveScoreRequest(
        @NotBlank
        String time,

        @NotNull
        @Min(0)
        Integer score,

        @NotNull
        @Min(0)
        @Max(2)
        Integer mode,

        @NotBlank
        @Pattern(regexp = "normal|timeAttack")
        String gameRule,

        @Min(30)
        @Max(90)
        Integer timeLimitSeconds,

        @Min(0)
        Integer wpm,

        @Min(0)
        @Max(100)
        Integer accuracy,

        @Min(0)
        Integer missCount,

        @Min(0)
        Integer correctCharacterCount) {
}
