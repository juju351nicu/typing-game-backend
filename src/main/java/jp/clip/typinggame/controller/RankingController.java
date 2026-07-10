package jp.clip.typinggame.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jp.clip.typinggame.dto.ScoreResponse;
import jp.clip.typinggame.service.ScoreService;
import lombok.RequiredArgsConstructor;

/**
 * ランキング情報を扱うREST Controllerです。
 */
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
@Validated
public class RankingController {

    /** スコア情報の業務処理を行うサービスです。 */
    private final ScoreService scoreService;

    /**
     * ランキング表示用のスコア情報を取得します。
     *
     * @param mode 難易度
     * @param gameRule ゲームルール
     * @param timeLimitSeconds タイムアタック時の制限時間（秒）
     * @param limit 取得件数
     * @return ランキング表示用のスコア情報一覧
     */
    @GetMapping
    public List<ScoreResponse> findRankings(
            @Parameter(description = "難易度。0: Easy、1: Normal、2: Hard", example = "2")
            @RequestParam(required = false)
            @Min(value = 0, message = "難易度は0以上で入力してください。")
            @Max(value = 2, message = "難易度は2以下で入力してください。")
            Integer mode,

            @Parameter(description = "ゲームルール。normal または timeAttack", example = "timeAttack")
            @RequestParam(required = false)
            @Pattern(regexp = "normal|timeAttack", message = "ゲームルールはnormalまたはtimeAttackを入力してください。")
            String gameRule,

            @Parameter(description = "タイムアタック時の制限時間（秒）", example = "60")
            @RequestParam(required = false)
            @Min(value = 30, message = "制限時間は30秒以上で入力してください。")
            @Max(value = 90, message = "制限時間は90秒以下で入力してください。")
            Integer timeLimitSeconds,

            @Parameter(description = "取得件数", example = "20")
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "取得件数は1以上で入力してください。")
            @Max(value = 100, message = "取得件数は100以下で入力してください。")
            Integer limit) {
        return scoreService.findRankings(mode, gameRule, timeLimitSeconds, limit);
    }
}
