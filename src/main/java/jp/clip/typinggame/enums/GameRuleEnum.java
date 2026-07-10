package jp.clip.typinggame.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

/**
 * ゲームルールを表すenumです。
 *
 * <p>
 * APIとDBでは {@code normal} や {@code timeAttack} のようなキー値を使い、
 * Javaコード上では {@link #NORMAL} や {@link #TIME_ATTACK} として扱います。
 * 文字列を直接扱う箇所を減らすことで、入力できる値をこのenumに集約できます。
 * </p>
 */
@Schema(description = "ゲームルール", enumAsRef = true)
@AllArgsConstructor
public enum GameRuleEnum implements CodeEnum {

    /** 通常モードです。 */
    NORMAL("normal", "通常モード"),

    /** タイムアタックです。 */
    TIME_ATTACK("timeAttack", "タイムアタック");

    /** APIやDBで扱うキー値です。 */
    private final String key;

    /** 画面表示や説明で扱うラベルです。 */
    private final String value;

    /** キー値からenumを高速に取得するためのMapです。 */
    private static final Map<String, GameRuleEnum> KEY_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    gameRule -> normalize(gameRule.getKey()),
                    gameRule -> gameRule));

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
     * キー値からゲームルールを取得します。
     *
     * <p>
     * JSONリクエストで {@code "timeAttack"} のような文字列を受け取った場合も、
     * Jacksonがこのメソッドを使ってenumへ変換します。
     * </p>
     *
     * @param key ゲームルールのキー値
     * @return キー値に対応するゲームルール
     * @throws IllegalArgumentException 対応するゲームルールが存在しない場合
     */
    @JsonCreator
    public static GameRuleEnum fromKey(String key) {
        GameRuleEnum gameRule = KEY_MAP.get(normalize(key));
        if (gameRule == null) {
            throw new IllegalArgumentException("ゲームルールはnormalまたはtimeAttackを入力してください。");
        }
        return gameRule;
    }

    /**
     * 比較用にキー値を正規化します。
     *
     * @param key ゲームルールのキー値
     * @return 前後空白を除去し、小文字化したキー値
     */
    private static String normalize(String key) {
        return StringUtils.trimToEmpty(key).toLowerCase(Locale.ROOT);
    }
}
