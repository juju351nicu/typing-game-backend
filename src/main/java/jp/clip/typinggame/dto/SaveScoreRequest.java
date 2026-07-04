package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "クリアタイムを入力してください。")
    private String time;

    /** ゲームスコアです。 */
    @JsonProperty("score")
    @NotNull(message = "スコアを入力してください。")
    @Min(value = 0, message = "スコアは0以上で入力してください。")
    private Integer score;

    /** 難易度を表すモード値です。 */
    @JsonProperty("mode")
    @NotNull(message = "難易度を入力してください。")
    @Min(value = 0, message = "難易度は0以上で入力してください。")
    @Max(value = 2, message = "難易度は2以下で入力してください。")
    private Integer mode;

    /** ゲームルールです。 */
    @JsonProperty("gameRule")
    @NotBlank(message = "ゲームルールを入力してください。")
    @Pattern(regexp = "normal|timeAttack", message = "ゲームルールはnormalまたはtimeAttackを入力してください。")
    private String gameRule;

    /** タイムアタック時の制限時間（秒）です。 */
    @JsonProperty("timeLimitSeconds")
    @Min(value = 30, message = "制限時間は30秒以上で入力してください。")
    @Max(value = 90, message = "制限時間は90秒以下で入力してください。")
    private Integer timeLimitSeconds;

    /** 1分あたりの入力文字数です。 */
    @JsonProperty("wpm")
    @Min(value = 0, message = "WPMは0以上で入力してください。")
    private Integer wpm;

    /** 入力の正確率です。 */
    @JsonProperty("accuracy")
    @Min(value = 0, message = "正確率は0以上で入力してください。")
    @Max(value = 100, message = "正確率は100以下で入力してください。")
    private Integer accuracy;

    /** ミスタイプ数です。 */
    @JsonProperty("missCount")
    @Min(value = 0, message = "ミスタイプ数は0以上で入力してください。")
    private Integer missCount;

    /** 正しく入力した文字数です。 */
    @JsonProperty("correctCharacterCount")
    @Min(value = 0, message = "正しく入力した文字数は0以上で入力してください。")
    private Integer correctCharacterCount;
}
