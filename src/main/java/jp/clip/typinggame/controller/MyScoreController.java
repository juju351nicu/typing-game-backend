package jp.clip.typinggame.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.clip.typinggame.dto.SaveScoreRequest;
import jp.clip.typinggame.dto.ScoreResponse;
import jp.clip.typinggame.entity.User;
import jp.clip.typinggame.service.CurrentUserService;
import jp.clip.typinggame.service.ScoreService;
import lombok.RequiredArgsConstructor;

/**
 * ログインユーザー自身のスコア情報を扱うREST Controllerです。
 */
@RestController
@RequestMapping("/api/me/scores")
@RequiredArgsConstructor
public class MyScoreController {

    /** スコア情報の業務処理を行うサービスです。 */
    private final ScoreService scoreService;

    /** ログイン中ユーザーを取得するサービスです。 */
    private final CurrentUserService currentUserService;

    /**
     * ログインユーザーに紐づくスコア情報を保存します。
     *
     * @param request 保存するスコア情報
     * @param authentication 認証情報
     * @return 保存後のスコア情報
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScoreResponse save(@Valid @RequestBody SaveScoreRequest request, Authentication authentication) {
        User user = currentUserService.findAuthenticatedUser(authentication);
        return scoreService.saveForUser(request, user);
    }

    /**
     * ログインユーザーに紐づく保存済みスコア情報を取得します。
     *
     * @param authentication 認証情報
     * @return ログインユーザーに紐づく保存済みスコア情報の一覧
     */
    @GetMapping
    public List<ScoreResponse> findAll(Authentication authentication) {
        User user = currentUserService.findAuthenticatedUser(authentication);
        return scoreService.findAllByUser(user);
    }
}
