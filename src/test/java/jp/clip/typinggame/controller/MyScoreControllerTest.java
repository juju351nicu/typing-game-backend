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

        mockMvc.perform(post("/api/me/scores")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
