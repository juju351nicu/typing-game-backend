package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jp.clip.typinggame.enums.NotificationCategoryEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * ログインユーザー向けの通知を返すレスポンスDTOです。
 */
@Getter
@Setter
public class NotificationResponse {

    /** 通知IDです。 */
    @JsonProperty("id")
    @Schema(description = "通知ID", example = "12")
    private Long id;

    /** 通知種別です。 */
    @JsonProperty("category")
    @Schema(description = "通知種別", example = "announce")
    private NotificationCategoryEnum category;

    /** 件名です。 */
    @JsonProperty("title")
    @Schema(description = "件名", example = "メンテナンスのお知らせ")
    private String title;

    /** 本文です。 */
    @JsonProperty("body")
    @Schema(description = "本文", example = "9月10日 2:00〜3:00 はランキングを利用できません。")
    private String body;

    /** 遷移先パスです。遷移不要な場合はnullです。 */
    @JsonProperty("linkPath")
    @Schema(description = "遷移先パス。遷移不要な場合はnull", example = "/ranking")
    private String linkPath;

    /** 通知日時です。 */
    @JsonProperty("createdAt")
    @Schema(description = "通知日時", example = "2026-09-01 10:00:00")
    private String createdAt;

    /** 未読の場合はtrueです。 */
    @JsonProperty("unread")
    @Schema(description = "未読の場合はtrue", example = "true")
    private Boolean unread;
}
