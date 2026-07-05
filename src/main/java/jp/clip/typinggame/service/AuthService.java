package jp.clip.typinggame.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

    /**
     * ログイン認証を行い、認証情報をHTTPセッションに保存します。
     *
     * @param request ログインリクエスト
     * @param httpRequest HTTPリクエスト
     * @return ログイン結果
     */
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLoginEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "メールアドレスまたはパスワードが正しくありません。");
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        User user = currentUserService.findAuthenticatedUser(authentication);
        user.setLastLoginAt(LocalDateTime.now());

        LoginResponse response = new LoginResponse();
        response.setUser(userService.toResponse(user));
        return response;
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
