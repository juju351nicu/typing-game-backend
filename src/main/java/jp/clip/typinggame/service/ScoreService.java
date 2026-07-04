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

/**
 * スコア情報の保存と取得を行うサービスです。
 */
@Service
@RequiredArgsConstructor
public class ScoreService {

    /** APIレスポンスで返却する日時フォーマットです。 */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** スコア情報へアクセスするRepositoryです。 */
    private final ScoreRepository scoreRepository;

    /**
     * スコア情報を保存します。
     *
     * @param request 保存するスコア情報
     * @return 保存後のスコア情報
     */
    @Transactional
    public ScoreResponse save(SaveScoreRequest request) {
        Score score = new Score();
        score.setTime(request.time());
        score.setScore(request.score());
        score.setMode(request.mode());
        score.setGameRule(request.gameRule());
        score.setTimeLimitSeconds(request.timeLimitSeconds());
        score.setWpm(request.wpm());
        score.setAccuracy(request.accuracy());
        score.setMissCount(request.missCount());
        score.setCorrectCharacterCount(request.correctCharacterCount());

        return toResponse(scoreRepository.save(score));
    }

    /**
     * 保存済みスコア情報を作成日時の降順で取得します。
     *
     * @return 保存済みスコア情報の一覧
     */
    @Transactional(readOnly = true)
    public List<ScoreResponse> findAll() {
        return scoreRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * EntityをAPIレスポンス用DTOへ変換します。
     *
     * @param score スコアEntity
     * @return APIレスポンス用DTO
     */
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
