package jp.clip.typinggame.config;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.clip.typinggame.service.LoginUserDetailsService;
import lombok.RequiredArgsConstructor;

/**
 * Spring Securityの認証・認可設定です。
 *
 * JWT Bearer認証、APIごとの認可ルール、認証失敗時のレスポンスを設定します。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWTのsubjectからログインユーザー情報を復元するサービスです。 */
    private final LoginUserDetailsService loginUserDetailsService;

    /**
     * APIごとの認証・認可ルールを設定します。
     *
     * @param http HTTPセキュリティ設定
     * @return セキュリティフィルターチェーン
     * @throws Exception 設定に失敗した場合
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 認証情報はリクエストごとのBearer tokenから復元し、HTTPセッションには保存しません。
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        http.oauth2ResourceServer(oauth2 -> oauth2
                .authenticationEntryPoint(this::writeUnauthorizedResponse)
                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                        new JwtLoginUserDetailsConverter(loginUserDetailsService))));
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(this::writeUnauthorizedResponse));

        // 公開APIと認証必須APIを分け、/api/me/** などはJWT認証を要求します。
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users", "/api/auth/login").permitAll()
                .requestMatchers("/api/scores/**").permitAll()
                .requestMatchers("/api/rankings/**").permitAll()
                .anyRequest().authenticated());

        return http.build();
    }

    /**
     * 認証失敗時のレスポンスを書き込みます。
     *
     * @param request HTTPリクエスト
     * @param response HTTPレスポンス
     * @param authException 認証例外
     * @throws java.io.IOException レスポンス書き込みに失敗した場合
     */
    private void writeUnauthorizedResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws java.io.IOException {
        // FE側のエラー表示を共通化するため、JWT認証失敗もfieldErrors形式で返します。
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"fieldErrors":[{"errorCode":"UNAUTHORIZED","field":"","message":"ログインしてください。"}]}
                """);
    }

    /**
     * パスワード暗号化に利用するEncoderを生成します。
     *
     * @return BCryptのPasswordEncoder
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Controllerから認証処理を呼び出すためのAuthenticationManagerを取得します。
     *
     * @param configuration 認証設定
     * @return AuthenticationManager
     * @throws Exception 取得に失敗した場合
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
