package jp.clip.typinggame.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jp.clip.typinggame.dto.LoginRequest;
import jp.clip.typinggame.dto.LoginResponse;
import jp.clip.typinggame.dto.UserResponse;
import jp.clip.typinggame.entity.User;
import lombok.RequiredArgsConstructor;

/**
 * ログイン状態を扱う認証サービスです。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /** Spring Securityの認証処理を実行するManagerです。 */
    private final AuthenticationManager authenticationManager;

    /** ユーザー情報をレスポンスDTOへ変換するサービスです。 */
    private final UserService userService;

    /** 認証情報からログイン中ユーザーを取得するサービスです。 */
    private final CurrentUserService currentUserService;

    /** JWTアクセストークンを生成するサービスです。 */
    private final JwtTokenService jwtTokenService;

    /**
     * ログイン認証を行い、JWTアクセストークンを発行します。
     *
     * @param request ログインリクエスト
     * @return ログイン結果
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLoginEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "メールアドレスまたはパスワードが正しくありません。");
        }

        User user = currentUserService.findAuthenticatedUser(authentication);
        user.setLastLoginAt(LocalDateTime.now());

        LoginResponse response = new LoginResponse();
        response.setUser(userService.toResponse(user));
        response.setAccessToken(jwtTokenService.generateAccessToken((LoginUserDetails) authentication.getPrincipal()));
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenService.getExpiresInSeconds());
        return response;
    }

    /**
     * ユーザーのトークン世代を進め、発行済みJWTをすべて失効させます。
     *
     * @param authentication 認証情報
     */
    @Transactional
    public void logout(Authentication authentication) {
        User user = currentUserService.findAuthenticatedUser(authentication);
        user.setTokenVersion(user.getTokenVersion() + 1);
    }

    /**
     * 現在ログイン中のユーザー情報を取得します。
     *
     * @param authentication 認証情報
     * @return ログイン中ユーザー情報
     */
    @Transactional(readOnly = true)
    public UserResponse findCurrentUser(Authentication authentication) {
        return userService.toResponse(currentUserService.findAuthenticatedUser(authentication));
    }
}
