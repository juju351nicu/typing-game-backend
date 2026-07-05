package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    @NotEmpty(message = "メールアドレスを入力してください。")
    @Email(message = "メールアドレスの形式で入力してください。")
    private String loginEmail;

    /** パスワードです。 */
    @JsonProperty("password")
    @NotEmpty(message = "パスワードを入力してください。")
    private String password;
}
