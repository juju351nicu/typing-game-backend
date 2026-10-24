package jp.clip.typinggame.entity;

import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.clip.typinggame.enums.GameModeEnum;
import jp.clip.typinggame.enums.GameRuleEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ゲーム終了時に保存するスコア情報を表すEntityです。
 *
 * <p>
 * {@code scores} テーブルの1レコードに対応し、スコア、ゲームルール、
 * WPM、正確率などランキング表示や分析に使う値を保持します。
 * ユーザーはEntityの関連ではなくIDで持ち、必要な結合はSQLファイルに書きます。
 * </p>
 */
@Entity(naming = NamingType.SNAKE_LOWER_CASE)
@Table(name = "scores")
@Getter
@Setter
@NoArgsConstructor
public class Score {

    /** スコアIDです。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** スコアを保存したユーザーのIDです。移行前の既存データではnullの場合があります。 */
    private Long userId;

    /** クリアタイムです。 */
    private String time;

    /** ゲームスコアです。 */
    private Integer score;

    /** 難易度を表すモード値です。DBには0 / 1 / 2の数値で保存します。 */
    private GameModeEnum mode;

    /** ゲームルールです。DBにはnormal / timeAttackのキー値で保存します。 */
    private GameRuleEnum gameRule;

    /** タイムアタック時の制限時間（秒）です。 */
    private Integer timeLimitSeconds;

    /** 1分あたりの入力文字数です。 */
    private Integer wpm;

    /** 入力の正確率です。 */
    private Integer accuracy;

    /** ミスタイプ数です。 */
    private Integer missCount;

    /** 正しく入力した文字数です。 */
    private Integer correctCharacterCount;

    /** スコア作成日時です。 */
    private LocalDateTime createdAt;
}
