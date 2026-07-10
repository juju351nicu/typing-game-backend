package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jp.clip.typinggame.enums.GameModeEnum;
import jp.clip.typinggame.enums.GameRuleEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * スコア保存APIのリクエストDTOです。
 *
 * <p>
 * フロントエンドから送られてきたJSONの値を受け取り、Controllerで入力チェックを行うためのクラスです。
 * このプロジェクトでは、現場でよく使われる JavaBeans 形式に合わせて、privateフィールド、
 * getter/setter、{@link JsonProperty} を使っています。
 * </p>
 *
 * <p>
 * Java 16以降では {@code public record SaveScoreRequest(...)} のようにrecordでDTOを書くこともできます。
 * recordはコンストラクタ、getter相当のアクセサ、{@code equals}、{@code hashCode}、{@code toString}
 * が自動生成されるため、値を受け渡すだけのDTOを短く書けます。
 * 一方で、アクセサ名が {@code getTime()} ではなく {@code time()} になり、setterも持ちません。
 * 既存プロジェクトや現場の規約がgetter/setter前提の場合は、このクラスのようなJavaBeans形式に
 * 揃える方が読みやすく、レビューもしやすくなります。
 * </p>
 */
@Getter
@Setter
public class SaveScoreRequest {

    /** クリアタイムです。 */
    @JsonProperty("time")
    @Schema(description = "クリアタイム", example = "00:00:28.00")
    @NotBlank(message = "クリアタイムを入力してください。")
    private String time;

    /** ゲームスコアです。 */
    @JsonProperty("score")
    @Schema(description = "ゲームスコア", example = "12")
    @NotNull(message = "スコアを入力してください。")
    @Min(value = 0, message = "スコアは0以上で入力してください。")
    private Integer score;

    /** 難易度を表すモード値です。 */
    @JsonProperty("mode")
    @Schema(description = "難易度を表すモード値。0: Easy、1: Normal、2: Hard", example = "2", implementation = GameModeEnum.class)
    @NotNull(message = "難易度を入力してください。")
    private GameModeEnum mode;

    /** ゲームルールです。 */
    @JsonProperty("gameRule")
    @Schema(description = "ゲームルール。normal または timeAttack", example = "timeAttack", implementation = GameRuleEnum.class)
    @NotNull(message = "ゲームルールを入力してください。")
    private GameRuleEnum gameRule;

    /** タイムアタック時の制限時間（秒）です。 */
    @JsonProperty("timeLimitSeconds")
    @Schema(description = "タイムアタック時の制限時間（秒）", example = "60")
    @Min(value = 30, message = "制限時間は30秒以上で入力してください。")
    @Max(value = 90, message = "制限時間は90秒以下で入力してください。")
    private Integer timeLimitSeconds;

    /** 1分あたりの入力文字数です。 */
    @JsonProperty("wpm")
    @Schema(description = "1分あたりの入力文字数", example = "32")
    @Min(value = 0, message = "WPMは0以上で入力してください。")
    private Integer wpm;

    /** 入力の正確率です。 */
    @JsonProperty("accuracy")
    @Schema(description = "入力の正確率", example = "96")
    @Min(value = 0, message = "正確率は0以上で入力してください。")
    @Max(value = 100, message = "正確率は100以下で入力してください。")
    private Integer accuracy;

    /** ミスタイプ数です。 */
    @JsonProperty("missCount")
    @Schema(description = "ミスタイプ数", example = "2")
    @Min(value = 0, message = "ミスタイプ数は0以上で入力してください。")
    private Integer missCount;

    /** 正しく入力した文字数です。 */
    @JsonProperty("correctCharacterCount")
    @Schema(description = "正しく入力した文字数", example = "80")
    @Min(value = 0, message = "正しく入力した文字数は0以上で入力してください。")
    private Integer correctCharacterCount;
}
