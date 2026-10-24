package jp.clip.typinggame.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.clip.typinggame.dto.AdminNotificationResponse;
import jp.clip.typinggame.dto.CreateNotificationRequest;
import jp.clip.typinggame.service.CurrentUserService;
import jp.clip.typinggame.service.NotificationService;
import lombok.RequiredArgsConstructor;

/**
 * 管理者が通知を送信・確認するREST Controllerです。
 *
 * <p>
 * {@code /api/admin/**} はSecurityConfigで管理者だけに許可しているため、
 * Controllerでは権限を判定しません。
 * </p>
 */
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    /** 通知の業務処理を行うサービスです。 */
    private final NotificationService notificationService;

    /** ログイン中ユーザーを取得するサービスです。 */
    private final CurrentUserService currentUserService;

    /**
     * 通知を送信します。宛先ユーザーIDが無い場合は全ユーザー宛てです。
     *
     * @param request 通知送信リクエスト
     * @param authentication 認証情報
     * @return 送信した通知
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminNotificationResponse create(
            @Valid @RequestBody CreateNotificationRequest request,
            Authentication authentication) {
        long adminUserId = currentUserService.findAuthenticatedUser(authentication).getId();
        return notificationService.create(request, adminUserId);
    }

    /**
     * 送信済みの通知を新しい順に取得します。
     *
     * @return 送信済み通知の一覧
     */
    @GetMapping
    public List<AdminNotificationResponse> findSent() {
        return notificationService.findSent();
    }
}
