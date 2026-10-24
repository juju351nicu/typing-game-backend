package jp.clip.typinggame.dao;

import java.time.LocalDateTime;
import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.boot.ConfigAutowireable;

import jp.clip.typinggame.enums.NotificationCategoryEnum;

/**
 * ユーザーごとの通知確認状態へアクセスするDomaのDAOです。
 *
 * <p>
 * 既読は「通知 × ユーザー」ではなく、ユーザーと通知種別ごとの最終確認日時1行で管理します。
 * SQLは {@code META-INF/jp/clip/typinggame/dao/UserNotificationStateDao/} 配下のファイルに書きます。
 * </p>
 */
@Dao
@ConfigAutowireable
public interface UserNotificationStateDao {

    /**
     * 最終確認日時を取得します。
     *
     * @param userId ユーザーID
     * @param category 通知種別
     * @return 最終確認日時。まだ一度も既読化していない場合は空
     */
    @Select
    Optional<LocalDateTime> selectLastReadAt(long userId, NotificationCategoryEnum category);

    /**
     * 最終確認日時を更新します。行が無い場合は作成します。
     *
     * <p>
     * 同一ユーザーが複数タブから同時に既読化した場合、「SELECT → 判定 → INSERT」の2段階では
     * 主キー制約違反になり得ます。{@code ON DUPLICATE KEY UPDATE} の単一SQLにすることで、
     * 競合をDB側で吸収します。
     * </p>
     *
     * @param userId ユーザーID
     * @param category 通知種別
     * @param readAt 最終確認日時
     * @return 登録・更新件数
     */
    @Insert(sqlFile = true)
    int upsertLastReadAt(long userId, NotificationCategoryEnum category, LocalDateTime readAt);
}
