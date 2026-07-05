package jp.clip.typinggame.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * Swagger UIへ表示するOpenAPIの基本情報です。
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "typing-game-backend API",
        version = "0.0.1",
        description = "typingGameのスコア保存、ユーザー登録、ログインを扱うAPIです。"))
public class OpenApiConfig {
}
