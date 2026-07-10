package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * ユーザー登録APIのリクエストDTOです。
 */
@Getter
@Setter
public class RegisterUserRequest {

    /** ログインメールアドレスです。 */
    @JsonProperty("loginEmail")
    @Schema(description = "ログインメールアドレス", example = "user@example.com")
    @NotEmpty(message = "メールアドレスを入力してください。")
    @Email(message = "メールアドレスの形式で入力してください。")
    private String loginEmail;

    /** ログインパスワードです。 */
    @JsonProperty("password")
    @Schema(description = "ログインパスワード。8文字以上72文字以内で入力します。", example = "password123")
    @NotEmpty(message = "パスワードを入力してください。")
    @Size(min = 8, max = 72, message = "パスワードは8文字以上72文字以内で入力してください。")
    private String password;
}
