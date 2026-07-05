package jp.clip.typinggame.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.clip.typinggame.repository.ScoreRepository;

/**
 * スコアAPIのController層を確認するテストです。
 *
 * <p>
 * JUnit 5の {@link DisplayName} を使い、テストレポート上でも確認しているAPI仕様が
 * 日本語で分かるようにしています。
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("スコアAPI")
class ScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScoreRepository scoreRepository;

    /**
     * 各テスト実行前にスコアテーブルを空にします。
     *
     * <p>
     * テスト同士がDBデータを共有すると実行順序に依存しやすいため、テストごとに初期状態をそろえます。
     * </p>
     */
    @BeforeEach
    void setUp() {
        scoreRepository.deleteAll();
    }

    /**
     * スコア保存APIの正常系を確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/scores はスコアを保存して201を返す")
    void saveScoreReturnsCreatedScore() throws Exception {
        Map<String, Object> request = Map.of(
                "time", "00:00:28.00",
                "score", 12,
                "mode", 2,
                "gameRule", "timeAttack",
                "timeLimitSeconds", 60,
                "wpm", 32,
                "accuracy", 96,
                "missCount", 2,
                "correctCharacterCount", 80);

        mockMvc.perform(post("/api/scores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.time").value("00:00:28.00"))
                .andExpect(jsonPath("$.score").value(12))
                .andExpect(jsonPath("$.mode").value(2))
                .andExpect(jsonPath("$.gameRule").value("timeAttack"))
                .andExpect(jsonPath("$.timeLimitSeconds").value(60))
                .andExpect(jsonPath("$.wpm").value(32))
                .andExpect(jsonPath("$.accuracy").value(96))
                .andExpect(jsonPath("$.missCount").value(2))
                .andExpect(jsonPath("$.correctCharacterCount").value(80))
                .andExpect(jsonPath("$.date").isString());
    }

    /**
     * スコア一覧取得APIの正常系を確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/scores は保存済みスコア一覧を返す")
    void findAllReturnsSavedScores() throws Exception {
        Map<String, Object> request = Map.of(
                "time", "00:01:10.00",
                "score", 18,
                "mode", 1,
                "gameRule", "normal",
                "wpm", 24,
                "accuracy", 94,
                "missCount", 4,
                "correctCharacterCount", 92);

        mockMvc.perform(post("/api/scores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/scores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].time").value("00:01:10.00"))
                .andExpect(jsonPath("$[0].score").value(18))
                .andExpect(jsonPath("$[0].gameRule").value("normal"));
    }

    /**
     * スコア保存APIのvalidationエラーを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/scores は不正なスコアの場合400を返す")
    void saveScoreReturnsBadRequestWhenInvalid() throws Exception {
        Map<String, Object> request = Map.of(
                "time", "00:00:28.00",
                "score", -1,
                "mode", 2,
                "gameRule", "timeAttack");

        mockMvc.perform(post("/api/scores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}
