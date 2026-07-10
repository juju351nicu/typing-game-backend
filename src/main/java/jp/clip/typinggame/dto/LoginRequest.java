package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * ログインAPIのリクエストDTOです。
 */
@Getter
@Setter
public class LoginRequest {

    /** ログインメールアドレスです。 */
    @JsonProperty("loginEmail")
    @Schema(description = "ログインメールアドレス", example = "user@example.com")
    @NotEmpty(message = "メールアドレスを入力してください。")
    @Email(message = "メールアドレスの形式で入力してください。")
    private String loginEmail;

    /** パスワードです。 */
    @JsonProperty("password")
    @Schema(description = "パスワード", example = "password123")
    @NotEmpty(message = "パスワードを入力してください。")
    private String password;
}
