package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * ユーザー情報を返すレスポンスDTOです。
 */
@Getter
@Setter
public class UserResponse {

    /** ユーザーIDです。 */
    @JsonProperty("id")
    private Long id;

    /** ログインメールアドレスです。 */
    @JsonProperty("loginEmail")
    private String loginEmail;
}
