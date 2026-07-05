package jp.clip.typinggame.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * APIエラー時に画面へ返すフィールド単位のエラー情報です。
 */
@Getter
@Setter
@NoArgsConstructor
public class CustomFieldError {

    /** エラーコードです。 */
    @JsonProperty("errorCode")
    private String errorCode;

    /** エラー対象のフィールド名です。 */
    @JsonProperty("field")
    private String field;

    /** 画面表示用のエラーメッセージです。 */
    @JsonProperty("message")
    private String message;

    /**
     * エラー情報を生成します。
     *
     * @param errorCode エラーコード
     * @param field エラー対象のフィールド名
     * @param message 画面表示用のエラーメッセージ
     */
    public CustomFieldError(String errorCode, String field, String message) {
        this.errorCode = errorCode;
        this.field = field;
        this.message = message;
    }
}
