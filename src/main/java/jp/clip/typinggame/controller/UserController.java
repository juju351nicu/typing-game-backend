package jp.clip.typinggame.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.clip.typinggame.dto.RegisterUserRequest;
import jp.clip.typinggame.dto.UserResponse;
import jp.clip.typinggame.service.UserService;
import lombok.RequiredArgsConstructor;

/**
 * ユーザー登録を扱うREST Controllerです。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    /** ユーザー登録の業務処理を行うサービスです。 */
    private final UserService userService;

    /**
     * ユーザーを新規登録します。
     *
     * @param request ユーザー登録リクエスト
     * @return 登録後のユーザー情報
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return userService.register(request);
    }
}
