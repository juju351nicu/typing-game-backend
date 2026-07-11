package jp.clip.typinggame.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * JWTの署名・検証に利用するSpring SecurityのBean設定です。
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    /**
     * JWTを生成するEncoderを生成します。
     *
     * @param properties JWT設定値
     * @return JWT Encoder
     */
    @Bean
    JwtEncoder jwtEncoder(JwtProperties properties) {
        // ログイン成功時のJWT発行では、同じ秘密鍵でHMAC-SHA256署名を行います。
        return new NimbusJwtEncoder(new ImmutableSecret<>(createSecretKey(properties)));
    }

    /**
     * JWTを検証するDecoderを生成します。
     *
     * @param properties JWT設定値
     * @return JWT Decoder
     */
    @Bean
    JwtDecoder jwtDecoder(JwtProperties properties) {
        // API呼び出し時のBearer token検証でも、発行時と同じ秘密鍵・アルゴリズムを使います。
        return NimbusJwtDecoder.withSecretKey(createSecretKey(properties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * 設定値からHMAC-SHA256用の秘密鍵を生成します。
     *
     * @param properties JWT設定値
     * @return 署名・検証用の秘密鍵
     */
    private SecretKey createSecretKey(JwtProperties properties) {
        String secret = properties.getSecret();
        if (StringUtils.length(secret) < 32) {
            // HS256の署名鍵が短すぎると安全性が落ちるため、ローカルでも最低長をそろえます。
            throw new IllegalStateException("JWT secretは32文字以上で設定してください。");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
