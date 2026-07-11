package jp.clip.typinggame.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * JWTアクセストークンの設定値を保持するクラスです。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** JWT署名に利用する秘密鍵文字列です。 */
    private String secret;

    /** アクセストークンの有効期間（秒）です。 */
    private long expiresInSeconds = 3600;

    /** JWTの発行者を表す値です。 */
    private String issuer = "typing-game-backend";
}
