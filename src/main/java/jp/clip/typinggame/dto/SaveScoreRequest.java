package jp.clip.typinggame.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * スコア保存APIのリクエストDTOです。
 *
 * @param time クリアタイム
 * @param score ゲームスコア
 * @param mode 難易度を表すモード値
 * @param gameRule ゲームルール
 * @param timeLimitSeconds タイムアタック時の制限時間（秒）
 * @param wpm 1分あたりの入力文字数
 * @param accuracy 入力の正確率
 * @param missCount ミスタイプ数
 * @param correctCharacterCount 正しく入力した文字数
 */
public record SaveScoreRequest(
        @NotBlank
        String time,

        @NotNull
        @Min(0)
        Integer score,

        @NotNull
        @Min(0)
        @Max(2)
        Integer mode,

        @NotBlank
        @Pattern(regexp = "normal|timeAttack")
        String gameRule,

        @Min(30)
        @Max(90)
        Integer timeLimitSeconds,

        @Min(0)
        Integer wpm,

        @Min(0)
        @Max(100)
        Integer accuracy,

        @Min(0)
        Integer missCount,

        @Min(0)
        Integer correctCharacterCount) {
}
