package jp.clip.typinggame.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

import jp.clip.typinggame.dao.UserDao;
import jp.clip.typinggame.support.TestDataCleaner;

/**
 * ログインユーザー向け通知APIのController層を確認するテストです。
 *
 * <p>
 * 通知は管理者APIから送信し、利用者側のAPIで受け取れることを確認します。
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ログインユーザー向け通知API")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDao userDao;

    /** 管理者のアクセストークンです。 */
    private String adminToken;

    /** 通知を受け取る一般ユーザーのアクセストークンです。 */
    private String userToken;

    /** 別の一般ユーザーのアクセストークンです。 */
    private String otherUserToken;

    /**
     * 各テスト実行前にDBを空にし、管理者と一般ユーザー2人を用意します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @BeforeEach
    void setUp() throws Exception {
        TestDataCleaner.deleteAll(jdbcTemplate);
        adminToken = registerAndLogin("admin@example.com");
        jdbcTemplate.update("UPDATE users SET role = 'admin' WHERE login_email = ?", "admin@example.com");
        userToken = registerAndLogin("user@example.com");
        otherUserToken = registerAndLogin("other@example.com");
    }

    /**
     * 自分宛てと全体宛ての通知だけを受け取ることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/me/notifications は自分宛てと全体宛てを返し、他人宛てを返さない")
    void findMyNotificationsReturnsOwnAndBroadcast() throws Exception {
        sendNotification("全体宛て", null);
        sendNotification("自分宛て", userIdOf("user@example.com"));
        sendNotification("他人宛て", userIdOf("other@example.com"));

        mockMvc.perform(get("/api/me/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("自分宛て"))
                .andExpect(jsonPath("$[0].unread").value(true))
                .andExpect(jsonPath("$[0].category").value("announce"))
                .andExpect(jsonPath("$[1].title").value("全体宛て"));

        mockMvc.perform(get("/api/me/notifications/unread-count")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.announce").value(2));
    }

    /**
     * 一覧取得では既読化しないことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/me/notifications は既読化しない")
    void findMyNotificationsDoesNotMarkRead() throws Exception {
        sendNotification("全体宛て", null);

        mockMvc.perform(get("/api/me/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/me/notifications/unread-count")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.announce").value(1));
    }

    /**
     * 既読化後は未読件数が0になり、その後の新着だけが未読になることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/me/notifications/mark-read の後は新着だけを未読として数える")
    void markReadResetsUnreadCount() throws Exception {
        sendNotification("既読にする通知", null);

        markRead(userToken);

        mockMvc.perform(get("/api/me/notifications/unread-count")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.announce").value(0));

        sendNotification("新着", null);

        mockMvc.perform(get("/api/me/notifications/unread-count")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.announce").value(1));

        mockMvc.perform(get("/api/me/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("新着"))
                .andExpect(jsonPath("$[0].unread").value(true))
                .andExpect(jsonPath("$[1].unread").value(false));
    }

    /**
     * 既読化は行の有無に関係なく何度呼んでも成功することを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("POST /api/me/notifications/mark-read は2回呼んでも成功する")
    void markReadIsIdempotent() throws Exception {
        markRead(userToken);
        markRead(userToken);

        Integer rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_notification_states WHERE user_id = ?",
                Integer.class,
                userIdOf("user@example.com"));
        assertEquals(1, rows);
    }

    /**
     * 保持期間より古い通知を一覧・件数に含めないことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("GET /api/me/notifications は保持期間より古い通知を返さない")
    void findMyNotificationsExcludesExpired() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO notifications (category, user_id, title, body, link_path, created_by, created_at)
                VALUES ('announce', NULL, '古い通知', '本文', NULL, ?, ?)
                """, userIdOf("admin@example.com"), LocalDateTime.now().minusDays(31));

        mockMvc.perform(get("/api/me/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/me/notifications/unread-count")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.announce").value(0));
    }

    /**
     * 不正な通知種別を拒否することを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("通知APIは不正な通知種別の場合400を返す")
    void notificationApiReturnsBadRequestWhenCategoryIsInvalid() throws Exception {
        mockMvc.perform(get("/api/me/notifications")
                .param("category", "invalid")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("category"));

        mockMvc.perform(post("/api/me/notifications/mark-read")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("category", "invalid"))))
                .andExpect(status().isBadRequest());
    }

    /**
     * 未ログインでは通知APIを呼び出せないことを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("通知APIは未ログインの場合401を返す")
    void notificationApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/me/notifications"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/me/notifications/unread-count"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/me/notifications/mark-read")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("category", "announce"))))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 管理者APIから通知を送信します。
     *
     * @param title 件名
     * @param userId 宛先ユーザーID。nullの場合は全ユーザー宛て
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    private void sendNotification(String title, Long userId) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("title", title);
        request.put("body", "本文");
        request.put("userId", userId);

        mockMvc.perform(post("/api/admin/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /**
     * 指定ユーザーの通知を既読にします。
     *
     * @param token アクセストークン
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    private void markRead(String token) throws Exception {
        mockMvc.perform(post("/api/me/notifications/mark-read")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("category", "announce"))))
                .andExpect(status().isNoContent());
    }

    /**
     * ログインメールアドレスからユーザーIDを取得します。
     *
     * @param loginEmail ログインメールアドレス
     * @return ユーザーID
     */
    private Long userIdOf(String loginEmail) {
        return userDao.selectByLoginEmail(loginEmail).orElseThrow().getId();
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
