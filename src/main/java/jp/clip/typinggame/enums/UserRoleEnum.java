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
 * ユーザーの権限を表すenumです。
 *
 * <p>
 * APIとDBでは {@code user} / {@code admin} のキー値を使い、
 * Spring Securityには {@link #getAuthority()} の {@code ROLE_USER} / {@code ROLE_ADMIN} として渡します。
 * DBとの相互変換は、Domaのドメインとして {@link #fromKey(String)} と {@link #getKey()} で行います。
 * </p>
 */
@Schema(description = "ユーザー権限。user: 一般ユーザー、admin: 管理者", enumAsRef = true)
@Domain(valueType = String.class, factoryMethod = "fromKey", accessorMethod = "getKey")
@AllArgsConstructor
public enum UserRoleEnum implements CodeEnum<String> {

    /** 一般ユーザーです。 */
    USER("user", "一般ユーザー"),

    /** 管理者です。通知の送信など管理用APIを利用できます。 */
    ADMIN("admin", "管理者");

    /** APIやDBで扱うキー値です。 */
    private final String key;

    /** 画面表示や説明で扱うラベルです。 */
    private final String value;

    /** キー値からenumを高速に取得するためのMapです。 */
    private static final Map<String, UserRoleEnum> KEY_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(role -> role.getKey(), role -> role));

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
     * Spring Securityの権限名を取得します。
     *
     * @return {@code ROLE_} を付けた権限名
     */
    public String getAuthority() {
        return "ROLE_" + name();
    }

    /**
     * キー値からユーザー権限を取得します。
     *
     * @param key ユーザー権限のキー値
     * @return キー値に対応するユーザー権限
     * @throws IllegalArgumentException 対応するユーザー権限が存在しない場合
     */
    @JsonCreator
    public static UserRoleEnum fromKey(String key) {
        UserRoleEnum role = key == null ? null : KEY_MAP.get(key);
        if (role == null) {
            throw new IllegalArgumentException("ユーザー権限はuserまたはadminを入力してください。");
        }
        return role;
    }
}
