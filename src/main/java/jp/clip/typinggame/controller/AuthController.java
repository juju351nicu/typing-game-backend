package jp.clip.typinggame.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jp.clip.typinggame.dto.LoginRequest;
import jp.clip.typinggame.dto.LoginResponse;
import jp.clip.typinggame.dto.UserResponse;
import jp.clip.typinggame.service.AuthService;
import lombok.RequiredArgsConstructor;

/**
 * ログインとログイン中ユーザー情報を扱うREST Controllerです。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 認証処理を行うサービスです。 */
    private final AuthService authService;

    /**
     * ログイン認証を行います。
     *
     * @param request ログインリクエスト
     * @param httpRequest HTTPリクエスト
     * @return ログイン結果
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest);
    }

    /**
     * ログイン中のユーザー情報を取得します。
     *
     * @param authentication 認証情報
     * @return ログイン中ユーザー情報
     */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.findCurrentUser(authentication);
    }
}
