package jp.clip.typinggame.dao;

import java.util.List;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.boot.ConfigAutowireable;

import jp.clip.typinggame.entity.Score;
import jp.clip.typinggame.enums.GameModeEnum;
import jp.clip.typinggame.enums.GameRuleEnum;

/**
 * スコア情報へアクセスするDomaのDAOです。
 *
 * <p>
 * SQLは {@code META-INF/jp/clip/typinggame/dao/ScoreDao/} 配下のファイルに書きます。
 * </p>
 */
@Dao
@ConfigAutowireable
public interface ScoreDao {

    /**
     * スコアを登録します。採番したIDは引数のEntityへ設定されます。
     *
     * @param score スコア情報
     * @return 登録件数
     */
    @Insert
    int insert(Score score);

    /**
     * 指定ユーザーのスコア情報を作成日時の降順で取得します。
     *
     * @param userId ユーザーID
     * @return 作成日時の降順で並んだスコア情報の一覧
     */
    @Select
    List<Score> selectByUserId(long userId);

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
     * @param limit 取得件数
     * @return ランキング表示用のスコア情報一覧
     */
    @Select
    List<Score> selectRankings(GameModeEnum mode, GameRuleEnum gameRule, Integer timeLimitSeconds, int limit);
}
