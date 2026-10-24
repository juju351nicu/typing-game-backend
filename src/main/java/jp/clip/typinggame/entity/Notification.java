package jp.clip.typinggame.entity;

import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.clip.typinggame.enums.NotificationCategoryEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 管理者が送信した通知を表すEntityです。
 *
 * <p>
 * {@code userId} がnullの場合は全ユーザー宛て、値がある場合はそのユーザー宛てです。
 * 全体宛ての通知はユーザー数に関係なく1行で表現します。
 * </p>
 */
@Entity(naming = NamingType.SNAKE_LOWER_CASE)
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    /** 通知IDです。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 通知種別です。 */
    private NotificationCategoryEnum category;

    /** 宛先ユーザーIDです。nullの場合は全ユーザー宛てです。 */
    private Long userId;

    /** 件名です。 */
    private String title;

    /** 本文です。 */
    private String body;

    /** 通知から遷移させる画面のパスです。遷移不要な場合はnullです。 */
    private String linkPath;

    /** 通知を送信した管理者のユーザーIDです。 */
    private Long createdBy;

    /** 通知作成日時です。 */
    private LocalDateTime createdAt;
}
