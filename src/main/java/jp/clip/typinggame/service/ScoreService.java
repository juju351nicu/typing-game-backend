package jp.clip.typinggame.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.clip.typinggame.dto.SaveScoreRequest;
import jp.clip.typinggame.dto.ScoreResponse;
import jp.clip.typinggame.entity.Score;
import jp.clip.typinggame.entity.User;
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
        Score score = toScoreEntity(request);
        return toResponse(scoreRepository.save(score));
    }

    /**
     * ログインユーザーに紐づくスコア情報を保存します。
     *
     * @param request 保存するスコア情報
     * @param user ログイン中ユーザー
     * @return 保存後のスコア情報
     */
    @Transactional
    public ScoreResponse saveForUser(SaveScoreRequest request, User user) {
        Score score = toScoreEntity(request);
        score.setUser(user);
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
     * 指定ユーザーに紐づく保存済みスコア情報を作成日時の降順で取得します。
     *
     * @param user ユーザー情報
     * @return 指定ユーザーに紐づく保存済みスコア情報の一覧
     */
    @Transactional(readOnly = true)
    public List<ScoreResponse> findAllByUser(User user) {
        return scoreRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * ランキング表示用のスコア情報を取得します。
     *
     * @param mode 難易度
     * @param gameRule ゲームルール
     * @param timeLimitSeconds タイムアタック時の制限時間（秒）
     * @param limit 取得件数
     * @return ランキング表示用のスコア情報一覧
     */
    @Transactional(readOnly = true)
    public List<ScoreResponse> findRankings(
            Integer mode,
            String gameRule,
            Integer timeLimitSeconds,
            Integer limit) {
        return scoreRepository.findRankings(
                mode,
                gameRule,
                timeLimitSeconds,
                PageRequest.of(0, limit)).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * スコア保存リクエストをEntityへ変換します。
     *
     * @param request スコア保存リクエスト
     * @return スコアEntity
     */
    private Score toScoreEntity(SaveScoreRequest request) {
        Score score = new Score();
        score.setTime(request.getTime());
        score.setScore(request.getScore());
        score.setMode(request.getMode());
        score.setGameRule(request.getGameRule());
        score.setTimeLimitSeconds(request.getTimeLimitSeconds());
        score.setWpm(request.getWpm());
        score.setAccuracy(request.getAccuracy());
        score.setMissCount(request.getMissCount());
        score.setCorrectCharacterCount(request.getCorrectCharacterCount());
        return score;
    }

    /**
     * EntityをAPIレスポンス用DTOへ変換します。
     *
     * @param score スコアEntity
     * @return APIレスポンス用DTO
     */
    private ScoreResponse toResponse(Score score) {
        ScoreResponse response = new ScoreResponse();
        response.setId(score.getId());
        response.setTime(score.getTime());
        response.setScore(score.getScore());
        response.setMode(score.getMode());
        response.setGameRule(score.getGameRule());
        response.setTimeLimitSeconds(score.getTimeLimitSeconds());
        response.setWpm(score.getWpm());
        response.setAccuracy(score.getAccuracy());
        response.setMissCount(score.getMissCount());
        response.setCorrectCharacterCount(score.getCorrectCharacterCount());
        response.setDate(score.getCreatedAt().format(DATE_FORMATTER));
        return response;
    }
}
