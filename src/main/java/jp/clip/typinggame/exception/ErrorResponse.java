package jp.clip.typinggame.exception;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * APIエラー時に返す共通レスポンスです。
 */
@Getter
@Setter
@NoArgsConstructor
public class ErrorResponse {

    /** フィールド単位のエラー情報一覧です。 */
    @JsonProperty("fieldErrors")
    private List<CustomFieldError> fieldErrors;

    /**
     * エラーレスポンスを生成します。
     *
     * @param fieldErrors フィールド単位のエラー情報一覧
     */
    public ErrorResponse(List<CustomFieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    /**
     * 単一エラーのレスポンスを生成します。
     *
     * @param errorCode エラーコード
     * @param field エラー対象のフィールド名
     * @param message 画面表示用のエラーメッセージ
     * @return エラーレスポンス
     */
    public static ErrorResponse of(String errorCode, String field, String message) {
        return new ErrorResponse(List.of(new CustomFieldError(errorCode, field, message)));
    }
}
