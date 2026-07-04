package jp.clip.typinggame.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.clip.typinggame.dto.SaveScoreRequest;
import jp.clip.typinggame.dto.ScoreResponse;
import jp.clip.typinggame.entity.Score;
import jp.clip.typinggame.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ScoreRepository scoreRepository;

    @Transactional
    public ScoreResponse save(SaveScoreRequest request) {
        Score score = Score.builder()
                .time(request.time())
                .score(request.score())
                .mode(request.mode())
                .gameRule(request.gameRule())
                .timeLimitSeconds(request.timeLimitSeconds())
                .wpm(request.wpm())
                .accuracy(request.accuracy())
                .missCount(request.missCount())
                .correctCharacterCount(request.correctCharacterCount())
                .build();

        return toResponse(scoreRepository.save(score));
    }

    @Transactional(readOnly = true)
    public List<ScoreResponse> findAll() {
        return scoreRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private ScoreResponse toResponse(Score score) {
        return new ScoreResponse(
                score.getId(),
                score.getTime(),
                score.getScore(),
                score.getMode(),
                score.getGameRule(),
                score.getTimeLimitSeconds(),
                score.getWpm(),
                score.getAccuracy(),
                score.getMissCount(),
                score.getCorrectCharacterCount(),
                score.getCreatedAt().format(DATE_FORMATTER));
    }
}
