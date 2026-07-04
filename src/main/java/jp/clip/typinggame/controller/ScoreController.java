package jp.clip.typinggame.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.clip.typinggame.dto.SaveScoreRequest;
import jp.clip.typinggame.dto.ScoreResponse;
import jp.clip.typinggame.service.ScoreService;
import lombok.RequiredArgsConstructor;

/**
 * スコア情報を扱うREST Controllerです。
 */
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    /** スコア情報の業務処理を行うサービスです。 */
    private final ScoreService scoreService;

    /**
     * ゲーム終了時のスコア情報を保存します。
     *
     * @param request 保存するスコア情報
     * @return 保存後のスコア情報
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScoreResponse save(@Valid @RequestBody SaveScoreRequest request) {
        return scoreService.save(request);
    }

    /**
     * 保存済みスコア情報を取得します。
     *
     * @return 保存済みスコア情報の一覧
     */
    @GetMapping
    public List<ScoreResponse> findAll() {
        return scoreService.findAll();
    }
}
