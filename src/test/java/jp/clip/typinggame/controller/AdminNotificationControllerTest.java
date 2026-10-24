package jp.clip.typinggame.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;

import jp.clip.typinggame.support.TestDataCleaner;

/**
 * 管理者向け通知APIのController層を確認するテストです。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("管理者向け通知API")
class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 管理者のアクセストークンです。 */
    private String adminToken;

    /** 一般ユーザーのアクセストークンです。 */
    private String userToken;

    /**
     * 各テスト実行前にDBを空にし、管理者と一般ユーザーを1人ずつ用意します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @BeforeEach
    void setUp() throws Exception {
        TestDataCleaner.deleteAll(jdbcTemplate);
        adminToken = registerAndLogin("admin@example.com");
        jdbcTemplate.update("UPDATE users SET role = 'admin' WHERE login_email = ?", "admin@example.com");
        userToken = registerAndLogin("user@example.com");
    }

    /**
     * 管理者が通知を送信し、送信済み一覧で確認できることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/admin/notifications は管理者の場合に通知を作成して201を返す")
    void createReturnsCreatedForAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.category").value("announce"))
                .andExpect(jsonPath("$.userId").isEmpty())
                .andExpect(jsonPath("$.title").value("メンテナンスのお知らせ"))
                .andExpect(jsonPath("$.linkPath").value("/ranking"))
                .andExpect(jsonPath("$.createdAt").isString());

        mockMvc.perform(get("/api/admin/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("メンテナンスのお知らせ"));
    }

    /**
     * 一般ユーザーは通知を送信できないことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/admin/notifications は一般ユーザーの場合403を返す")
    void createReturnsForbiddenForUser() throws Exception {
        mockMvc.perform(post("/api/admin/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.fieldErrors[0].errorCode").value("FORBIDDEN"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("この操作を行う権限がありません。"));

        mockMvc.perform(get("/api/admin/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    /**
     * 未ログインでは管理者APIを呼び出せないことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/admin/notifications は未ログインの場合401を返す")
    void findSentReturnsUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/admin/notifications"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 権限の変更が再ログインを待たずに反映されることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("管理者権限を外すと発行済みのトークンでも403になる")
    void revokedAdminIsForbiddenWithIssuedToken() throws Exception {
        jdbcTemplate.update("UPDATE users SET role = 'user' WHERE login_email = ?", "admin@example.com");

        mockMvc.perform(get("/api/admin/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    /**
     * 入力チェックのエラーを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/admin/notifications は件名が空、遷移先が外部URLの場合400を返す")
    void createReturnsBadRequestWhenInvalid() throws Exception {
        Map<String, Object> request = new HashMap<>(validRequest());
        request.put("title", " ");
        request.put("linkPath", "//example.com");

        mockMvc.perform(post("/api/admin/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
    }

    /**
     * 存在しないユーザー宛てには送信できないことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/admin/notifications は宛先ユーザーが存在しない場合400を返す")
    void createReturnsBadRequestWhenUserDoesNotExist() throws Exception {
        Map<String, Object> request = new HashMap<>(validRequest());
        request.put("userId", 999999);

        mockMvc.perform(post("/api/admin/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("宛先ユーザーが見つかりません。"));
    }

    /**
     * 正常な通知送信リクエストを生成します。
     *
     * @return 通知送信リクエスト
     */
    private Map<String, Object> validRequest() {
        return Map.of(
                "title", "メンテナンスのお知らせ",
                "body", "9月10日 2:00〜3:00 はランキングを利用できません。",
                "linkPath", "/ranking");
    }

    /**
     * テスト用ユーザーを登録してログインします。
     *
     * @param loginEmail ログインメールアドレス
     * @return アクセストークン
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    private String registerAndLogin(String loginEmail) throws Exception {
        Map<String, Object> request = Map.of(
                "loginEmail", loginEmail,
                "password", "password123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asString();
    }
}
