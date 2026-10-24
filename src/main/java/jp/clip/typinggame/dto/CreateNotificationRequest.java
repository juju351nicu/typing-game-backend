package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理者が通知を送信するAPIのリクエストDTOです。
 */
@Getter
@Setter
public class CreateNotificationRequest {

    /** 件名です。 */
    @JsonProperty("title")
    @Schema(description = "件名", example = "メンテナンスのお知らせ")
    @NotBlank(message = "件名を入力してください。")
    @Size(max = 100, message = "件名は100文字以内で入力してください。")
    private String title;

    /** 本文です。 */
    @JsonProperty("body")
    @Schema(description = "本文", example = "9月10日 2:00〜3:00 はランキングを利用できません。")
    @NotBlank(message = "本文を入力してください。")
    @Size(max = 500, message = "本文は500文字以内で入力してください。")
    private String body;

    /** 遷移先パスです。遷移不要な場合はnullです。 */
    @JsonProperty("linkPath")
    @Schema(description = "遷移先パス。アプリ内のパスを/から入力します。遷移不要な場合はnull", example = "/ranking")
    @Size(max = 200, message = "遷移先パスは200文字以内で入力してください。")
    // 外部サイトへの誘導に使われないよう、//で始まるプロトコル相対URLも拒否します。
    @Pattern(regexp = "^/(?!/).*$", message = "遷移先パスは/から始まるアプリ内のパスで入力してください。")
    private String linkPath;

    /** 宛先ユーザーIDです。nullの場合は全ユーザー宛てです。 */
    @JsonProperty("userId")
    @Schema(description = "宛先ユーザーID。nullの場合は全ユーザー宛て", example = "1")
    @Positive(message = "宛先ユーザーIDは1以上で入力してください。")
    private Long userId;
}
