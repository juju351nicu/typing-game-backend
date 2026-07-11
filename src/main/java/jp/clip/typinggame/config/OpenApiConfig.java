package jp.clip.typinggame.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

/**
 * Swagger UIへ表示するOpenAPIの基本情報と認証設定です。
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "typing-game-backend API",
        version = "0.0.1",
        description = "typingGameのスコア保存、ユーザー登録、ログインを扱うAPIです。"),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SECURITY_SCHEME))
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH_SECURITY_SCHEME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {

    /** Swagger UIでJWT Bearer認証を指定するときのsecurity scheme名です。 */
    public static final String BEARER_AUTH_SECURITY_SCHEME = "bearerAuth";
}
