package jp.clip.typinggame.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.clip.typinggame.entity.Score;
import jp.clip.typinggame.entity.User;

/**
 * スコア情報へアクセスするRepositoryです。
 */
public interface ScoreRepository extends JpaRepository<Score, Long> {

    /**
     * スコア情報を作成日時の降順で取得します。
     *
     * @return 作成日時の降順で並んだスコア情報の一覧
     */
    List<Score> findAllByOrderByCreatedAtDesc();

    /**
     * 指定ユーザーのスコア情報を作成日時の降順で取得します。
     *
     * @param user ユーザー情報
     * @return 作成日時の降順で並んだスコア情報の一覧
     */
    List<Score> findAllByUserOrderByCreatedAtDesc(User user);

    /**
     * ランキング表示用のスコア情報を取得します。
     *
     * <p>
     * 条件がnullの場合は、その条件では絞り込みません。
     * 並び順はスコア降順、クリアタイム昇順、作成日時降順です。
     * </p>
     *
     * @param mode 難易度
     * @param gameRule ゲームルール
     * @param timeLimitSeconds タイムアタック時の制限時間（秒）
     * @param pageable 取得件数
     * @return ランキング表示用のスコア情報一覧
     */
    @Query("""
            SELECT score
            FROM Score score
            WHERE (:mode IS NULL OR score.mode = :mode)
              AND (:gameRule IS NULL OR score.gameRule = :gameRule)
              AND (:timeLimitSeconds IS NULL OR score.timeLimitSeconds = :timeLimitSeconds)
            ORDER BY score.score DESC, score.time ASC, score.createdAt DESC
            """)
    List<Score> findRankings(
            @Param("mode") Integer mode,
            @Param("gameRule") String gameRule,
            @Param("timeLimitSeconds") Integer timeLimitSeconds,
            Pageable pageable);
}
