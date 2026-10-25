package jp.clip.typinggame.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jp.clip.typinggame.dao.ScoreDao;
import jp.clip.typinggame.entity.Score;
import jp.clip.typinggame.enums.GameModeEnum;
import jp.clip.typinggame.enums.GameRuleEnum;
import jp.clip.typinggame.support.TestDataCleaner;

/**
 * ランキングAPIのController層を確認するテストです。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ランキングAPI")
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScoreDao scoreDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 各テスト実行前にスコアテーブルを空にします。
     */
    @BeforeEach
    void setUp() {
        TestDataCleaner.deleteAll(jdbcTemplate);
    }

    /**
     * ランキング取得APIの並び順と取得件数を確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/rankings はスコア降順、タイム昇順でランキングを返す")
    void findRankingsReturnsOrderedScores() throws Exception {
        saveScore("00:00:30.00", 10, 2, "timeAttack", 60);
        saveScore("00:00:20.00", 10, 2, "timeAttack", 60);
        saveScore("00:00:40.00", 12, 1, "normal", null);

        mockMvc.perform(get("/api/rankings")
                .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].score").value(12))
                .andExpect(jsonPath("$[0].time").value("00:00:40.00"))
                .andExpect(jsonPath("$[1].score").value(10))
                .andExpect(jsonPath("$[1].time").value("00:00:20.00"));
    }

    /**
     * ランキング取得APIの絞り込み条件を確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/rankings は難易度、ルール、制限時間で絞り込む")
    void findRankingsFiltersScores() throws Exception {
        saveScore("00:00:28.00", 12, 2, "timeAttack", 60);
        saveScore("00:00:25.00", 20, 2, "timeAttack", 30);
        saveScore("00:00:22.00", 30, 1, "normal", null);

        mockMvc.perform(get("/api/rankings")
                .param("mode", "2")
                .param("gameRule", "timeAttack")
                .param("timeLimitSeconds", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].score").value(12))
                .andExpect(jsonPath("$[0].mode").value(2))
                .andExpect(jsonPath("$[0].gameRule").value("timeAttack"))
                .andExpect(jsonPath("$[0].timeLimitSeconds").value(60));
    }

    /**
     * ランキング取得APIのvalidationエラーを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/rankings は不正な取得件数の場合400を返す")
    void findRankingsReturnsBadRequestWhenLimitIsInvalid() throws Exception {
        mockMvc.perform(get("/api/rankings")
                .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("limit"));
    }

    /**
     * ページ単位のランキング取得APIが、LIMIT/OFFSETで指定ページの行と総件数を返すことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/rankings/page は指定ページの行と総件数を返す")
    void findRankingsPageReturnsRequestedPage() throws Exception {
        for (int scoreValue = 1; scoreValue <= 5; scoreValue++) {
            saveScore("00:00:30.00", scoreValue, 1, "normal", null);
        }

        mockMvc.perform(get("/api/rankings/page")
                .param("page", "2")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].score").value(3))
                .andExpect(jsonPath("$.items[1].score").value(2))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalCount").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    /**
     * 最終ページは残りの件数だけを返すことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/rankings/page は最終ページで残りの件数だけを返す")
    void findRankingsPageReturnsRemainingItemsOnLastPage() throws Exception {
        for (int scoreValue = 1; scoreValue <= 5; scoreValue++) {
            saveScore("00:00:30.00", scoreValue, 1, "normal", null);
        }

        mockMvc.perform(get("/api/rankings/page")
                .param("page", "3")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].score").value(1));
    }

    /**
     * 総件数も絞り込み条件に一致する件数だけを数えることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/rankings/page は絞り込み条件に一致する件数だけを総件数にする")
    void findRankingsPageCountsFilteredScores() throws Exception {
        saveScore("00:00:28.00", 12, 2, "timeAttack", 60);
        saveScore("00:00:25.00", 20, 2, "timeAttack", 30);
        saveScore("00:00:22.00", 30, 1, "normal", null);

        mockMvc.perform(get("/api/rankings/page")
                .param("mode", "2")
                .param("gameRule", "timeAttack"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].score").value(20))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    /**
     * 該当データがない場合も、空の1ページ目として返すことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/rankings/page は該当データがない場合も総ページ数1を返す")
    void findRankingsPageReturnsSinglePageWhenEmpty() throws Exception {
        mockMvc.perform(get("/api/rankings/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    /**
     * ページ番号のvalidationエラーを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/rankings/page は不正なページ番号の場合400を返す")
    void findRankingsPageReturnsBadRequestWhenPageIsInvalid() throws Exception {
        mockMvc.perform(get("/api/rankings/page")
                .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("page"));
    }

    /**
     * テスト用のスコアを保存します。
     *
     * @param time クリアタイム
     * @param score スコア
     * @param mode 難易度
     * @param gameRule ゲームルール
     * @param timeLimitSeconds タイムアタック時の制限時間（秒）
     */
    private void saveScore(String time, int scoreValue, int mode, String gameRule, Integer timeLimitSeconds) {
        Score score = new Score();
        score.setTime(time);
        score.setScore(scoreValue);
        score.setMode(GameModeEnum.fromKey(mode));
        score.setGameRule(GameRuleEnum.fromKey(gameRule));
        score.setTimeLimitSeconds(timeLimitSeconds);
        score.setWpm(32);
        score.setAccuracy(96);
        score.setMissCount(2);
        score.setCorrectCharacterCount(80);
        score.setCreatedAt(LocalDateTime.now());
        scoreDao.insert(score);
    }
}
