package jp.clip.typinggame.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * テストごとにDBを初期状態へ戻すための補助クラスです。
 *
 * <p>
 * 外部キーの参照元から順に削除するため、テーブルの削除順をこのクラスに集約します。
 * </p>
 */
public final class TestDataCleaner {

    /** インスタンス化させないためのコンストラクタです。 */
    private TestDataCleaner() {
    }

    /**
     * アプリケーションが利用するテーブルのデータをすべて削除します。
     *
     * @param jdbcTemplate テスト用DBへ接続するJdbcTemplate
     */
    public static void deleteAll(JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate,
                "user_notification_states",
                "notifications",
                "scores",
                "users");
    }
}
