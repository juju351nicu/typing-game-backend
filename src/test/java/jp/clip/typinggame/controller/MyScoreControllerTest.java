package jp.clip.typinggame.controller;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.clip.typinggame.repository.ScoreRepository;
import jp.clip.typinggame.repository.UserRepository;

/**
 * ログインユーザー別スコアAPIのController層を確認するテストです。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ログインユーザー別スコアAPI")
class MyScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 各テスト実行前にスコアテーブルとユーザーテーブルを空にします。
     */
    @BeforeEach
    void setUp() {
        scoreRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * ログインユーザーに紐づくスコア保存と一覧取得の正常系を確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/me/scores はログインユーザーに紐づくスコアを保存する")
    void saveCreatesScoreForLoggedInUser() throws Exception {
        MockHttpSession session = registerAndLogin("user@example.com", "password123");

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

        mockMvc.perform(post("/api/me/scores")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.score").value(12))
                .andExpect(jsonPath("$.gameRule").value("timeAttack"));

        mockMvc.perform(get("/api/me/scores").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(12))
                .andExpect(jsonPath("$[0].gameRule").value("timeAttack"));
    }

    /**
     * Bearer tokenでログインユーザー別スコア保存と一覧取得ができることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/me/scores はBearer tokenでもログインユーザーに紐づくスコアを保存する")
    void saveCreatesScoreForBearerTokenUser() throws Exception {
        String accessToken = registerAndLoginForAccessToken("jwt-user@example.com", "password123");

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

        // Authorizationヘッダーだけでログインユーザーを復元し、スコアを保存できることを確認します。
        mockMvc.perform(post("/api/me/scores")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.score").value(12))
                .andExpect(jsonPath("$.gameRule").value("timeAttack"));

        // 保存したスコアもBearer tokenだけで取得できることを確認します。
        mockMvc.perform(get("/api/me/scores")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(12))
                .andExpect(jsonPath("$[0].gameRule").value("timeAttack"));
    }

    /**
     * ユーザー別スコア一覧がログインユーザーごとに分離されることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/me/scores はログインユーザー自身のスコアだけを返す")
    void findAllReturnsOnlyLoggedInUserScores() throws Exception {
        MockHttpSession firstUserSession = registerAndLogin("first@example.com", "password123");
        MockHttpSession secondUserSession = registerAndLogin("second@example.com", "password123");

        saveScore(firstUserSession, 30);
        saveScore(secondUserSession, 99);

        mockMvc.perform(get("/api/me/scores").session(firstUserSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].score").value(30));
    }

    /**
     * 未ログインでユーザー別スコアAPIを呼び出した場合の認証エラーを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/me/scores は未ログインの場合401を返す")
    void findAllReturnsUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/me/scores"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ログインしてください。"));
    }

    /**
     * テスト用ユーザーを登録してログインします。
     *
     * @param loginEmail ログインメールアドレス
     * @param password パスワード
     * @return ログイン済みセッション
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    private MockHttpSession registerAndLogin(String loginEmail, String password) throws Exception {
        Map<String, Object> registerRequest = Map.of(
                "loginEmail", loginEmail,
                "password", password);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        Map<String, Object> loginRequest = Map.of(
                "loginEmail", loginEmail,
                "password", password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    /**
     * テスト用ユーザーを登録してログインし、アクセストークンを取得します。
     *
     * @param loginEmail ログインメールアドレス
     * @param password パスワード
     * @return アクセストークン
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    private String registerAndLoginForAccessToken(String loginEmail, String password) throws Exception {
        Map<String, Object> registerRequest = Map.of(
                "loginEmail", loginEmail,
                "password", password);

        // Bearer token認証のテストでも、ユーザー作成は実際の登録API経由で行います。
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        Map<String, Object> loginRequest = Map.of(
                "loginEmail", loginEmail,
                "password", password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // ログインレスポンスから、Authorizationヘッダーに使うaccessTokenを取り出します。
        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
    }

    /**
     * テスト用のユーザー別スコアを保存します。
     *
     * @param session ログイン済みセッション
     * @param scoreValue スコア値
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    private void saveScore(MockHttpSession session, int scoreValue) throws Exception {
        Map<String, Object> request = Map.of(
                "time", "00:00:30.00",
                "score", scoreValue,
                "mode", 1,
                "gameRule", "normal",
                "wpm", 24,
                "accuracy", 94,
                "missCount", 4,
                "correctCharacterCount", 92);

        // セッション方式の既存テスト用に、指定ユーザーのスコアを1件作成します。
        mockMvc.perform(post("/api/me/scores")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /**
     * Authorizationヘッダー用のBearer token文字列を作成します。
     *
     * @param accessToken アクセストークン
     * @return Bearer token文字列
     */
    private String toBearerToken(String accessToken) {
        // ヘッダーの組み立てを共通化し、テストごとの文字列揺れを避けます。
        return "Bearer " + accessToken;
    }
}
