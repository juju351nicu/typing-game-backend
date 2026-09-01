package jp.clip.typinggame.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;

import jp.clip.typinggame.repository.ScoreRepository;
import jp.clip.typinggame.repository.UserRepository;

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

    @Autowired
    private UserRepository userRepository;

    /** 各テストで認証必須の保存APIへ渡すアクセストークンです。 */
    private String accessToken;

    /**
     * 各テスト実行前にスコアテーブルを空にします。
     *
     * <p>
     * テスト同士がDBデータを共有すると実行順序に依存しやすいため、テストごとに初期状態をそろえます。
     * </p>
     */
    @BeforeEach
    void setUp() throws Exception {
        scoreRepository.deleteAll();
        userRepository.deleteAll();
        accessToken = registerAndLogin("score-test@example.com", "password123");
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
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
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
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
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
     * 未認証ユーザーが公開ランキングへスコアを投稿できないことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/scores は未ログインの場合401を返す")
    void saveScoreReturnsUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(post("/api/scores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ログインしてください。"));
    }

    /**
     * Bearer認証ではCookieを使わないため、CORSで資格情報を許可しないことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("OPTIONS /api/scores は資格情報付きCORSを許可しない")
    void preflightDoesNotAllowCredentials() throws Exception {
        mockMvc.perform(options("/api/scores")
                .header(HttpHeaders.ORIGIN, "http://localhost:8081")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
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
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    /**
     * スコア保存APIで不正なゲームルールを受け取った場合のエラーを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/scores は不正なゲームルールの場合400を返す")
    void saveScoreReturnsBadRequestWhenGameRuleIsInvalid() throws Exception {
        Map<String, Object> request = Map.of(
                "time", "00:00:28.00",
                "score", 12,
                "mode", 2,
                "gameRule", "invalid",
                "timeLimitSeconds", 60,
                "wpm", 32,
                "accuracy", 96,
                "missCount", 2,
                "correctCharacterCount", 80);

        mockMvc.perform(post("/api/scores")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].errorCode").value("INVALID_REQUEST"));
    }

    /**
     * スコア保存APIで不正な難易度を受け取った場合のエラーを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/scores は不正な難易度の場合400を返す")
    void saveScoreReturnsBadRequestWhenModeIsInvalid() throws Exception {
        Map<String, Object> request = Map.of(
                "time", "00:00:28.00",
                "score", 12,
                "mode", 9,
                "gameRule", "timeAttack",
                "timeLimitSeconds", 60,
                "wpm", 32,
                "accuracy", 96,
                "missCount", 2,
                "correctCharacterCount", 80);

        mockMvc.perform(post("/api/scores")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].errorCode").value("INVALID_REQUEST"));
    }

    /**
     * スコア関連の数値が上限を超える場合のvalidationエラーを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/scores は数値項目が上限を超える場合400を返す")
    void saveScoreReturnsBadRequestWhenValuesExceedMaximums() throws Exception {
        Map<String, Object> request = Map.of(
                "time", "00:00:28.00",
                "score", 100001,
                "mode", 2,
                "gameRule", "timeAttack",
                "timeLimitSeconds", 60,
                "wpm", 1001,
                "accuracy", 96,
                "missCount", 100001,
                "correctCharacterCount", 100001);

        mockMvc.perform(post("/api/scores")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItems(
                        "score", "wpm", "missCount", "correctCharacterCount")));
    }

    /**
     * DBカラム長を超えるクリアタイムを入口で拒否することを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/scores はクリアタイムが20文字を超える場合400を返す")
    void saveScoreReturnsBadRequestWhenTimeIsTooLong() throws Exception {
        Map<String, Object> request = Map.of(
                "time", "123456789012:00:00.00",
                "score", 12,
                "mode", 2,
                "gameRule", "timeAttack",
                "timeLimitSeconds", 60,
                "wpm", 32,
                "accuracy", 96,
                "missCount", 2,
                "correctCharacterCount", 80);

        mockMvc.perform(post("/api/scores")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("time"));
    }

    /**
     * クリアタイムの区切りや各桁が不正な場合に拒否されることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/scores はクリアタイムの形式が不正な場合400を返す")
    void saveScoreReturnsBadRequestWhenTimeFormatIsInvalid() throws Exception {
        List<String> invalidTimes = List.of(
                "0:00:28.00",
                "00:60:28.00",
                "00:00:61.00",
                "00:00:28.000",
                "00:00:28",
                "28.00");

        for (String invalidTime : invalidTimes) {
            Map<String, Object> request = new HashMap<>(validRequest());
            request.put("time", invalidTime);

            mockMvc.perform(post("/api/scores")
                    .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("time"));
        }
    }

    /**
     * FEが生成する代表的なクリアタイム形式を受け付けることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/scores はFEが送る形式のクリアタイムを受け付ける")
    void saveScoreAcceptsFrontendTimeFormat() throws Exception {
        for (String validTime : List.of("00:00:28.00", "00:05:23.45", "123:59:59.99")) {
            Map<String, Object> request = new HashMap<>(validRequest());
            request.put("time", validTime);

            mockMvc.perform(post("/api/scores")
                    .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    /**
     * スコア一覧の取得件数上限を確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/scores は取得件数が100を超える場合400を返す")
    void findAllReturnsBadRequestWhenLimitExceedsMaximum() throws Exception {
        mockMvc.perform(get("/api/scores").param("limit", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("limit"));
    }

    /** 正常なスコア保存リクエストを生成します。 */
    private Map<String, Object> validRequest() {
        return Map.of(
                "time", "00:00:28.00",
                "score", 12,
                "mode", 2,
                "gameRule", "timeAttack",
                "timeLimitSeconds", 60,
                "wpm", 32,
                "accuracy", 96,
                "missCount", 2,
                "correctCharacterCount", 80);
    }

    /** テストユーザーを登録してログインし、アクセストークンを返します。 */
    private String registerAndLogin(String loginEmail, String password) throws Exception {
        Map<String, Object> registerRequest = Map.of(
                "loginEmail", loginEmail,
                "password", password);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
    }

    /** Authorizationヘッダー用のBearer token文字列を返します。 */
    private String toBearerToken(String token) {
        return "Bearer " + token;
    }
}
