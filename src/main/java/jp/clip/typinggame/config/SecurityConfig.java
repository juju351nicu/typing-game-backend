package jp.clip.typinggame.config;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Securityの認証・認可設定です。
 *
 * <p>
 * todo-backend のSecurity設定を参考にしつつ、typingGameではAPI用の最小構成として
 * セッションCookie方式、BCryptパスワード、CSRF無効化から開始します。
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * APIごとの認証・認可ルールを設定します。
     *
     * @param http HTTPセキュリティ設定
     * @return セキュリティフィルターチェーン
     * @throws Exception 設定に失敗した場合
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> {
        });
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("""
                            {"fieldErrors":[{"errorCode":"UNAUTHORIZED","field":"","message":"ログインしてください。"}]}
                            """);
                }));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users", "/api/auth/login").permitAll()
                .requestMatchers("/api/scores/**").permitAll()
                .requestMatchers("/api/rankings/**").permitAll()
                .anyRequest().authenticated());

        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value())));

        return http.build();
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
