package jp.clip.typinggame.dto;

public record ScoreResponse(
        Long id,
        String time,
        Integer score,
        Integer mode,
        String gameRule,
        Integer timeLimitSeconds,
        Integer wpm,
        Integer accuracy,
        Integer missCount,
        Integer correctCharacterCount,
        String date) {
}
