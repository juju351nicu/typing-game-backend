package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jp.clip.typinggame.enums.NotificationCategoryEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知既読化APIのリクエストDTOです。
 */
@Getter
@Setter
public class MarkReadRequest {

    /** 既読にする通知種別です。 */
    @JsonProperty("category")
    @Schema(description = "通知種別", example = "announce", implementation = NotificationCategoryEnum.class)
    @NotNull(message = "通知種別を入力してください。")
    private NotificationCategoryEnum category;
}
