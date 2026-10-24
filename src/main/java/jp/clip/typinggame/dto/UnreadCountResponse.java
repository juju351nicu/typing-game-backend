package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知種別ごとの未読件数を返すレスポンスDTOです。
 *
 * <p>
 * FEがバッジ表示のため定期的に呼ぶので、通知本文は含めず件数だけを返します。
 * </p>
 */
@Getter
@Setter
public class UnreadCountResponse {

    /** 運営からのお知らせの未読件数です。 */
    @JsonProperty("announce")
    @Schema(description = "お知らせの未読件数", example = "3")
    private long announce;
}
