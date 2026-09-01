package jp.clip.typinggame.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;

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

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    /**
     * 各テスト実行前にスコアテーブルとユーザーテーブルを空にします。
     */
    @BeforeEach
    void setUp() {
        scoreRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * ログインAPIの正常系と、JWTを使ったログイン中ユーザー取得を確認します。
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
                .andExpect(cookie().doesNotExist("JSESSIONID"))
                .andReturn();

        String accessToken = extractAccessToken(loginResult);
        assertNull(loginResult.getRequest().getSession(false));
        Long userId = userRepository.findByLoginEmail("user@example.com").orElseThrow().getId();
        assertEquals(String.valueOf(userId), jwtDecoder.decode(accessToken).getSubject());
        assertNull(jwtDecoder.decode(accessToken).getClaim("userId"));

        // 個人情報を含まないユーザーID subjectのJWTだけで同じユーザーを復元できることを確認します。
        mockMvc.perform(get("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken)))
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
     * 不正なBearer tokenでログイン中ユーザー取得APIを呼び出した場合の認証エラーを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/auth/me は不正なBearer tokenの場合401を返す")
    void meReturnsUnauthorizedWhenBearerTokenIsInvalid() throws Exception {
        // 署名検証できないtokenを渡し、JWT認証失敗時も共通の401レスポンスになることを確認します。
        mockMvc.perform(get("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken("invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ログインしてください。"));
    }

    /**
     * issuerが異なる署名済みJWTを拒否することを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/auth/me はissuerが異なるBearer tokenの場合401を返す")
    void meReturnsUnauthorizedWhenIssuerDoesNotMatch() throws Exception {
        registerUser("user@example.com", "password123");
        Long userId = userRepository.findByLoginEmail("user@example.com").orElseThrow().getId();
        String accessToken = createAccessToken("another-issuer", String.valueOf(userId), 0);

        mockMvc.perform(get("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ログインしてください。"));
    }

    /**
     * subjectがユーザーIDでない署名済みJWTを拒否することを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/auth/me はsubjectが数値でないBearer tokenの場合401を返す")
    void meReturnsUnauthorizedWhenSubjectIsNotUserId() throws Exception {
        String accessToken = createAccessToken(
                "typing-game-backend-test",
                "user@example.com",
                0);

        mockMvc.perform(get("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ログインしてください。"));
    }

    /**
     * ログアウト後に発行済みJWTが利用できなくなることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/auth/logout は発行済みBearer tokenを失効させる")
    void logoutRevokesIssuedBearerToken() throws Exception {
        registerUser("user@example.com", "password123");
        String accessToken = loginAndExtractAccessToken("user@example.com", "password123");

        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken)))
                .andExpect(status().isUnauthorized());

        String newAccessToken = loginAndExtractAccessToken("user@example.com", "password123");
        mockMvc.perform(get("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(newAccessToken)))
                .andExpect(status().isOk());
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

        // 各テストで必要なユーザーをAPI経由で作成し、Controller層の動きに寄せて確認します。
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /**
     * 登録済みユーザーでログインし、アクセストークンを取得します。
     *
     * @param loginEmail ログインメールアドレス
     * @param password パスワード
     * @return アクセストークン
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    private String loginAndExtractAccessToken(String loginEmail, String password) throws Exception {
        Map<String, Object> request = Map.of(
                "loginEmail", loginEmail,
                "password", password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        return extractAccessToken(result);
    }

    /**
     * テスト用の署名済みJWTを生成します。
     *
     * @param issuer issuer claim
     * @param subject subject claim
     * @param tokenVersion トークン世代
     * @return JWT文字列
     */
    private String createAccessToken(String issuer, String subject, long tokenVersion) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(3600))
                .subject(subject)
                .claim("tokenVersion", tokenVersion)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * ログインレスポンスからアクセストークンを取得します。
     *
     * @param loginResult ログインAPIの実行結果
     * @return アクセストークン
     * @throws Exception レスポンスJSONの読み取りに失敗した場合
     */
    private String extractAccessToken(MvcResult loginResult) throws Exception {
        // JWT認証のテストで再利用するため、レスポンスJSONからaccessTokenだけを取り出します。
        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
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
