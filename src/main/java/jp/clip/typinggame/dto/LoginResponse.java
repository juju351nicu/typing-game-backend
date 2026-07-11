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

    /**
     * API認証に利用するアクセストークンです。
     *
     * <p>
     * JWT化の移行期間中はセッションCookieも残しますが、FE側ではこの値を
     * AuthorizationヘッダーのBearerトークンとして送信する方針です。
     * </p>
     */
    @JsonProperty("accessToken")
    private String accessToken;

    /** アクセストークンの種別です。通常はBearerです。 */
    @JsonProperty("tokenType")
    private String tokenType;

    /** アクセストークンの有効期間（秒）です。 */
    @JsonProperty("expiresIn")
    private Long expiresIn;
}
