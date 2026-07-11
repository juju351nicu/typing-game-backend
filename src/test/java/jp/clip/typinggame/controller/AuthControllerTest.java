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
 * 認証APIのController層を確認するテストです。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("認証API")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    /**
     * 各テスト実行前にスコアテーブルとユーザーテーブルを空にします。
     */
    @BeforeEach
    void setUp() {
        scoreRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * ログインAPIの正常系と、セッションを使ったログイン中ユーザー取得を確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/auth/login はログインしてユーザー情報を返す")
    void loginReturnsCurrentUser() throws Exception {
        registerUser("user@example.com", "password123");

        Map<String, Object> loginRequest = Map.of(
                "loginEmail", "user@example.com",
                "password", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.loginEmail").value("user@example.com"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginEmail").value("user@example.com"));
    }

    /**
     * ログインAPIの認証失敗を確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/auth/login はパスワード不一致の場合401を返す")
    void loginReturnsUnauthorizedWhenPasswordDoesNotMatch() throws Exception {
        registerUser("user@example.com", "password123");

        Map<String, Object> loginRequest = Map.of(
                "loginEmail", "user@example.com",
                "password", "wrong-password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("メールアドレスまたはパスワードが正しくありません。"));
    }

    /**
     * 未ログインでログイン中ユーザー取得APIを呼び出した場合の認証エラーを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/auth/me は未ログインの場合401を返す")
    void meReturnsUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ログインしてください。"));
    }

    /**
     * テスト用ユーザーを登録します。
     *
     * @param loginEmail ログインメールアドレス
     * @param password パスワード
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    private void registerUser(String loginEmail, String password) throws Exception {
        Map<String, Object> request = Map.of(
                "loginEmail", loginEmail,
                "password", password);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
