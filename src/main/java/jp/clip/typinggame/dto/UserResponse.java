package jp.clip.typinggame.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jp.clip.typinggame.enums.UserRoleEnum;
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

    /** ユーザー権限です。 */
    @JsonProperty("role")
    private UserRoleEnum role;
}
