package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * ログインAPIのレスポンスDTOです。
 */
@Getter
@Setter
public class LoginResponse {

    /** ログイン済みユーザー情報です。 */
    @JsonProperty("user")
    private UserResponse user;
}
