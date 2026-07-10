package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jp.clip.typinggame.enums.GameModeEnum;
import jp.clip.typinggame.enums.GameRuleEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * スコア情報を返却するAPIレスポンスDTOです。
 *
 * <p>
 * EntityをそのままJSONとして返すのではなく、フロントエンドが必要とする項目だけを返却するためのクラスです。
 * レスポンスDTOを分けることで、DBの内部構造をAPI利用側へ直接見せずに済みます。
 * </p>
 *
 * <p>
 * recordで書く場合は、各フィールドをコンストラクタ引数として宣言できるためコード量は少なくなります。
 * ただし、このプロジェクトでは学習と現場の書き方に合わせて、getter/setterを持つ通常のclassとして
 * 定義しています。
 * </p>
 */
@Getter
@Setter
public class ScoreResponse {

    /** スコアIDです。 */
    @JsonProperty("id")
    private Long id;

    /** クリアタイムです。 */
    @JsonProperty("time")
    private String time;

    /** ゲームスコアです。 */
    @JsonProperty("score")
    private Integer score;

    /** 難易度を表すモード値です。 */
    @JsonProperty("mode")
    private GameModeEnum mode;

    /** ゲームルールです。 */
    @JsonProperty("gameRule")
    private GameRuleEnum gameRule;

    /** タイムアタック時の制限時間（秒）です。 */
    @JsonProperty("timeLimitSeconds")
    private Integer timeLimitSeconds;

    /** 1分あたりの入力文字数です。 */
    @JsonProperty("wpm")
    private Integer wpm;

    /** 入力の正確率です。 */
    @JsonProperty("accuracy")
    private Integer accuracy;

    /** ミスタイプ数です。 */
    @JsonProperty("missCount")
    private Integer missCount;

    /** 正しく入力した文字数です。 */
    @JsonProperty("correctCharacterCount")
    private Integer correctCharacterCount;

    /** スコア作成日時です。 */
    @JsonProperty("date")
    private String date;
}
