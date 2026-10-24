package jp.clip.typinggame.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jp.clip.typinggame.dto.MarkReadRequest;
import jp.clip.typinggame.dto.NotificationResponse;
import jp.clip.typinggame.dto.UnreadCountResponse;
import jp.clip.typinggame.enums.NotificationCategoryEnum;
import jp.clip.typinggame.service.CurrentUserService;
import jp.clip.typinggame.service.NotificationService;
import lombok.RequiredArgsConstructor;

/**
 * ログインユーザー宛ての通知を扱うREST Controllerです。
 *
 * <p>
 * 一覧取得、既読化、未読件数取得を別のAPIに分け、一覧取得では既読化しません。
 * 未読件数はFEのバッジ表示のため定期的に呼ばれるので、件数だけを返します。
 * </p>
 */
@RestController
@RequestMapping("/api/me/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    /** 通知の業務処理を行うサービスです。 */
    private final NotificationService notificationService;

    /** ログイン中ユーザーを取得するサービスです。 */
    private final CurrentUserService currentUserService;

    /**
     * 保持期間内の自分宛て・全体宛て通知を未読フラグ付きで新しい順に取得します。
     *
     * @param category 通知種別
     * @param authentication 認証情報
     * @return 通知一覧
     */
    @GetMapping
    public List<NotificationResponse> findMyNotifications(
            @Parameter(description = "通知種別", example = "announce")
            @RequestParam(defaultValue = "announce")
            @Pattern(regexp = "announce", message = "通知種別はannounceを入力してください。")
            String category,
            Authentication authentication) {
        long userId = currentUserService.findAuthenticatedUser(authentication).getId();
        return notificationService.findMyNotifications(userId, NotificationCategoryEnum.fromKey(category));
    }

    /**
     * 指定種別の通知をすべて既読にします。
     *
     * @param request 既読化リクエスト
     * @param authentication 認証情報
     */
    @PostMapping("/mark-read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@Valid @RequestBody MarkReadRequest request, Authentication authentication) {
        long userId = currentUserService.findAuthenticatedUser(authentication).getId();
        notificationService.markRead(userId, request.getCategory());
    }

    /**
     * 通知種別ごとの未読件数を取得します。
     *
     * @param authentication 認証情報
     * @return 未読件数
     */
    @GetMapping("/unread-count")
    public UnreadCountResponse countUnread(Authentication authentication) {
        long userId = currentUserService.findAuthenticatedUser(authentication).getId();
        return notificationService.countUnread(userId);
    }
}
