package jp.clip.typinggame.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.boot.ConfigAutowireable;

import jp.clip.typinggame.entity.Notification;
import jp.clip.typinggame.enums.NotificationCategoryEnum;

/**
 * 通知へアクセスするDomaのDAOです。
 *
 * <p>
 * SQLは {@code META-INF/jp/clip/typinggame/dao/NotificationDao/} 配下のファイルに書きます。
 * </p>
 */
@Dao
@ConfigAutowireable
public interface NotificationDao {

    /**
     * 通知を登録します。採番したIDは引数のEntityへ設定されます。
     *
     * @param notification 通知
     * @return 登録件数
     */
    @Insert
    int insert(Notification notification);

    /**
     * 指定期間内の自分宛て・全体宛て通知を新しい順に取得します。
     *
     * @param category 通知種別
     * @param userId ログインユーザーID
     * @param since 取得対象期間の開始日時
     * @param limit 取得件数
     * @return 通知一覧
     */
    @Select
    List<Notification> selectVisible(NotificationCategoryEnum category, long userId, LocalDateTime since, int limit);

    /**
     * 最終確認日時より新しい自分宛て・全体宛て通知の件数を取得します。
     *
     * @param category 通知種別
     * @param userId ログインユーザーID
     * @param since 取得対象期間の開始日時
     * @param lastReadAt 最終確認日時
     * @return 未読件数
     */
    @Select
    long countUnread(NotificationCategoryEnum category, long userId, LocalDateTime since, LocalDateTime lastReadAt);

    /**
     * 管理画面向けに、送信済みの通知を宛先に関係なく新しい順に取得します。
     *
     * @param limit 取得件数
     * @return 通知一覧
     */
    @Select
    List<Notification> selectRecent(int limit);
}
