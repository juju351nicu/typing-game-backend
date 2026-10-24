package jp.clip.typinggame.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.seasar.doma.Domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

/**
 * 通知の種別を表すenumです。
 *
 * <p>
 * 既読管理は種別ごとの最終確認時刻で行うため、種別を増やした場合も
 * {@code user_notification_states} にカラムを足さずに済みます。
 * DBとの相互変換は、Domaのドメインとして {@link #fromKey(String)} と {@link #getKey()} で行います。
 * </p>
 */
@Schema(description = "通知種別。announce: 運営からのお知らせ", enumAsRef = true)
@Domain(valueType = String.class, factoryMethod = "fromKey", accessorMethod = "getKey")
@AllArgsConstructor
public enum NotificationCategoryEnum implements CodeEnum<String> {

    /** 運営からのお知らせです。管理者だけが作成できます。 */
    ANNOUNCE("announce", "お知らせ");

    /** APIやDBで扱うキー値です。 */
    private final String key;

    /** 画面表示や説明で扱うラベルです。 */
    private final String value;

    /** キー値からenumを高速に取得するためのMapです。 */
    private static final Map<String, NotificationCategoryEnum> KEY_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(category -> category.getKey(), category -> category));

    /**
     * APIやDBに保存する外向きのコード値を取得します。
     *
     * @return コード値
     */
    @JsonValue
    @Override
    public String getKey() {
        return key;
    }

    /**
     * 画面表示や説明に使うラベルを取得します。
     *
     * @return 表示ラベル
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * キー値から通知種別を取得します。
     *
     * @param key 通知種別のキー値
     * @return キー値に対応する通知種別
     * @throws IllegalArgumentException 対応する通知種別が存在しない場合
     */
    @JsonCreator
    public static NotificationCategoryEnum fromKey(String key) {
        NotificationCategoryEnum category = key == null ? null : KEY_MAP.get(key);
        if (category == null) {
            throw new IllegalArgumentException("通知種別はannounceを入力してください。");
        }
        return category;
    }
}
