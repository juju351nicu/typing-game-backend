package jp.clip.typinggame.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jp.clip.typinggame.dao.NotificationDao;
import jp.clip.typinggame.dao.UserDao;
import jp.clip.typinggame.dao.UserNotificationStateDao;
import jp.clip.typinggame.dto.AdminNotificationResponse;
import jp.clip.typinggame.dto.CreateNotificationRequest;
import jp.clip.typinggame.dto.NotificationResponse;
import jp.clip.typinggame.dto.UnreadCountResponse;
import jp.clip.typinggame.entity.Notification;
import jp.clip.typinggame.enums.NotificationCategoryEnum;
import lombok.RequiredArgsConstructor;

/**
 * 通知の送信、一覧取得、未読件数取得、既読化を行うサービスです。
 *
 * <p>
 * 既読は通知ごとではなく、ユーザーと通知種別ごとの最終確認日時で管理します。
 * 最終確認日時より後に作成された通知が未読です。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    /** 通知の保持期間（日）です。期間外の通知は一覧・件数のいずれにも含めません。 */
    public static final int RETENTION_DAYS = 30;

    /** 一覧の最大取得件数です。 */
    public static final int MAX_LIST_SIZE = 50;

    /**
     * 最終確認日時の行が無い場合に使う値です。
     *
     * <p>
     * 十分に過去の値を使い、すべての通知を未読として扱います。
     * 行の作成を既読化APIだけに集約することで、ポーリングされる読み取りAPIから
     * INSERTの副作用を取り除き、読み取り専用トランザクションにそろえています。
     * </p>
     */
    private static final LocalDateTime NEVER_READ = LocalDateTime.of(1900, 1, 1, 0, 0);

    /** APIレスポンスで返却する日時フォーマットです。 */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 通知へアクセスするDAOです。 */
    private final NotificationDao notificationDao;

    /** 通知確認状態へアクセスするDAOです。 */
    private final UserNotificationStateDao userNotificationStateDao;

    /** 宛先ユーザーの存在確認に使うDAOです。 */
    private final UserDao userDao;

    /**
     * 管理者として通知を送信します。
     *
     * @param request 通知送信リクエスト
     * @param adminUserId 送信する管理者のユーザーID
     * @return 送信した通知
     */
    @Transactional
    public AdminNotificationResponse create(CreateNotificationRequest request, long adminUserId) {
        if (request.getUserId() != null && userDao.selectById(request.getUserId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "宛先ユーザーが見つかりません。");
        }

        Notification notification = new Notification();
        notification.setCategory(NotificationCategoryEnum.ANNOUNCE);
        notification.setUserId(request.getUserId());
        notification.setTitle(request.getTitle());
        notification.setBody(request.getBody());
        notification.setLinkPath(request.getLinkPath());
        notification.setCreatedBy(adminUserId);
        notification.setCreatedAt(now());
        notificationDao.insert(notification);
        return toAdminResponse(notification);
    }

    /**
     * 管理画面向けに、送信済みの通知を新しい順に取得します。
     *
     * @return 送信済み通知の一覧
     */
    @Transactional(readOnly = true)
    public List<AdminNotificationResponse> findSent() {
        return notificationDao.selectRecent(MAX_LIST_SIZE).stream()
                .map(this::toAdminResponse)
                .toList();
    }

    /**
     * ログインユーザー宛ての通知一覧を未読フラグ付きで取得します。既読化は行いません。
     *
     * @param userId ログインユーザーID
     * @param category 通知種別
     * @return 通知一覧
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> findMyNotifications(long userId, NotificationCategoryEnum category) {
        LocalDateTime lastReadAt = findLastReadAt(userId, category);
        return notificationDao.selectVisible(category, userId, since(), MAX_LIST_SIZE).stream()
                .map(notification -> toResponse(notification, lastReadAt))
                .toList();
    }

    /**
     * ログインユーザーの未読件数を通知種別ごとに取得します。
     *
     * @param userId ログインユーザーID
     * @return 未読件数
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse countUnread(long userId) {
        UnreadCountResponse response = new UnreadCountResponse();
        response.setAnnounce(notificationDao.countUnread(
                NotificationCategoryEnum.ANNOUNCE,
                userId,
                since(),
                findLastReadAt(userId, NotificationCategoryEnum.ANNOUNCE)));
        return response;
    }

    /**
     * 指定種別の通知をすべて既読にします。
     *
     * @param userId ログインユーザーID
     * @param category 通知種別
     */
    @Transactional
    public void markRead(long userId, NotificationCategoryEnum category) {
        userNotificationStateDao.upsertLastReadAt(userId, category, now());
    }

    /**
     * 最終確認日時を取得します。行が無い場合はすべて未読とするための値を返します。
     *
     * @param userId ログインユーザーID
     * @param category 通知種別
     * @return 最終確認日時
     */
    private LocalDateTime findLastReadAt(long userId, NotificationCategoryEnum category) {
        return userNotificationStateDao.selectLastReadAt(userId, category).orElse(NEVER_READ);
    }

    /**
     * 取得対象期間の開始日時を返します。
     *
     * @return 保持期間の開始日時
     */
    private LocalDateTime since() {
        return now().minusDays(RETENTION_DAYS);
    }

    /**
     * DBのTIMESTAMP(6)に合わせ、マイクロ秒で切り捨てた現在日時を返します。
     *
     * @return 現在日時
     */
    private LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }

    /**
     * 通知Entityを利用者向けのレスポンスDTOへ変換します。
     *
     * @param notification 通知Entity
     * @param lastReadAt 最終確認日時
     * @return 利用者向けのレスポンスDTO
     */
    private NotificationResponse toResponse(Notification notification, LocalDateTime lastReadAt) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setCategory(notification.getCategory());
        response.setTitle(notification.getTitle());
        response.setBody(notification.getBody());
        response.setLinkPath(notification.getLinkPath());
        response.setCreatedAt(notification.getCreatedAt().format(DATE_FORMATTER));
        response.setUnread(notification.getCreatedAt().isAfter(lastReadAt));
        return response;
    }

    /**
     * 通知Entityを管理画面向けのレスポンスDTOへ変換します。
     *
     * @param notification 通知Entity
     * @return 管理画面向けのレスポンスDTO
     */
    private AdminNotificationResponse toAdminResponse(Notification notification) {
        AdminNotificationResponse response = new AdminNotificationResponse();
        response.setId(notification.getId());
        response.setCategory(notification.getCategory());
        response.setUserId(notification.getUserId());
        response.setTitle(notification.getTitle());
        response.setBody(notification.getBody());
        response.setLinkPath(notification.getLinkPath());
        response.setCreatedAt(notification.getCreatedAt().format(DATE_FORMATTER));
        return response;
    }
}
