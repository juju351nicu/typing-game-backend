package jp.clip.typinggame.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.Getter;
import lombok.Setter;

/**
 * フロントエンドからAPIを呼び出すためのCORS設定です。
 */
@Configuration
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsConfig {

    /** APIアクセスを許可するOriginの一覧です。 */
    private List<String> allowedOrigins = List.of("http://localhost:8081");

    /**
     * APIパスに適用するCORS設定を生成します。
     *
     * @return CORS設定
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // application.yml の app.cors.allowed-origins から許可Originを切り替えます。
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Accept", "Authorization", "X-Requested-With"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
