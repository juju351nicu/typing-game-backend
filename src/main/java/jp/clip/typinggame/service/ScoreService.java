package jp.clip.typinggame.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.clip.typinggame.dao.ScoreDao;
import jp.clip.typinggame.dto.RankingPageResponse;
import jp.clip.typinggame.dto.SaveScoreRequest;
import jp.clip.typinggame.dto.ScoreResponse;
import jp.clip.typinggame.entity.Score;
import jp.clip.typinggame.entity.User;
import jp.clip.typinggame.enums.GameModeEnum;
import jp.clip.typinggame.enums.GameRuleEnum;
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

    /** スコア情報へアクセスするDAOです。 */
    private final ScoreDao scoreDao;

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
        score.setUserId(user.getId());
        score.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        scoreDao.insert(score);
        return toResponse(score);
    }

    /**
     * 指定ユーザーに紐づく保存済みスコア情報を作成日時の降順で取得します。
     *
     * @param user ユーザー情報
     * @return 指定ユーザーに紐づく保存済みスコア情報の一覧
     */
    @Transactional(readOnly = true)
    public List<ScoreResponse> findAllByUser(User user) {
        return scoreDao.selectByUserId(user.getId()).stream()
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
        GameModeEnum gameModeEnum = mode == null ? null : GameModeEnum.fromKey(mode);
        GameRuleEnum gameRuleEnum = gameRule == null ? null : GameRuleEnum.fromKey(gameRule);
        return scoreDao.selectRankings(
                gameModeEnum,
                gameRuleEnum,
                timeLimitSeconds,
                limit).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * ランキング表示用のスコア情報をページ単位で取得します。
     *
     * <p>
     * ページ番号はフロントエンドの表示と同じ1始まりで受け取り、
     * SQLのOFFSETへは {@code (page - 1) * size} に変換して渡します。
     * </p>
     *
     * @param mode 難易度
     * @param gameRule ゲームルール
     * @param timeLimitSeconds タイムアタック時の制限時間（秒）
     * @param page ページ番号（1始まり）
     * @param size 1ページあたりの件数
     * @return 指定ページのスコア情報と総件数
     */
    @Transactional(readOnly = true)
    public RankingPageResponse findRankingsPage(
            Integer mode,
            String gameRule,
            Integer timeLimitSeconds,
            int page,
            int size) {
        GameModeEnum gameModeEnum = mode == null ? null : GameModeEnum.fromKey(mode);
        GameRuleEnum gameRuleEnum = gameRule == null ? null : GameRuleEnum.fromKey(gameRule);
        long totalCount = scoreDao.countRankings(gameModeEnum, gameRuleEnum, timeLimitSeconds);
        List<ScoreResponse> items = scoreDao.selectRankingsPage(
                gameModeEnum,
                gameRuleEnum,
                timeLimitSeconds,
                size,
                calculateOffset(page, size)).stream()
                .map(this::toResponse)
                .toList();

        RankingPageResponse response = new RankingPageResponse();
        response.setItems(items);
        response.setPage(page);
        response.setSize(size);
        response.setTotalCount(totalCount);
        response.setTotalPages(calculateTotalPages(totalCount, size));
        return response;
    }

    /**
     * 1始まりのページ番号からSQLのOFFSET値を求めます。
     *
     * @param page ページ番号（1始まり）
     * @param size 1ページあたりの件数
     * @return 読み飛ばす件数
     */
    static int calculateOffset(int page, int size) {
        return (page - 1) * size;
    }

    /**
     * 総件数から総ページ数を求めます。
     *
     * <p>
     * 該当データが0件でも画面には空の1ページ目を表示するため、最小値は1です。
     * </p>
     *
     * @param totalCount 総件数
     * @param size 1ページあたりの件数
     * @return 総ページ数
     */
    static int calculateTotalPages(long totalCount, int size) {
        return (int) Math.max(1, (totalCount + size - 1) / size);
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
