package jp.clip.typinggame.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

/**
 * ゲームの難易度を表すenumです。
 *
 * <p>
 * APIとDBでは既存仕様に合わせて {@code 0}、{@code 1}、{@code 2} の数値を使い、
 * Javaコード上では {@link #EASY}、{@link #NORMAL}、{@link #HARD} として扱います。
 * </p>
 */
@Schema(description = "ゲームの難易度。0: Easy、1: Normal、2: Hard", enumAsRef = true)
@AllArgsConstructor
public enum GameModeEnum implements CodeEnum<Integer> {

    /** Easyです。 */
    EASY(0, "Easy"),

    /** Normalです。 */
    NORMAL(1, "Normal"),

    /** Hardです。 */
    HARD(2, "Hard");

    /** APIやDBで扱う難易度コードです。 */
    private final Integer key;

    /** 画面表示や説明で扱うラベルです。 */
    private final String value;

    /** キー値からenumを高速に取得するためのMapです。 */
    private static final Map<Integer, GameModeEnum> KEY_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(GameModeEnum::getKey, gameMode -> gameMode));

    /**
     * APIやDBに保存する外向きのコード値を取得します。
     *
     * @return 難易度コード
     */
    @JsonValue
    @Override
    public Integer getKey() {
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
     * キー値からゲーム難易度を取得します。
     *
     * <p>
     * JSONリクエストで {@code 2} のような数値を受け取った場合も、
     * Jacksonがこのメソッドを使ってenumへ変換します。
     * </p>
     *
     * @param key 難易度コード
     * @return キー値に対応するゲーム難易度
     * @throws IllegalArgumentException 対応するゲーム難易度が存在しない場合
     */
    @JsonCreator
    public static GameModeEnum fromKey(Integer key) {
        GameModeEnum gameMode = KEY_MAP.get(key);
        if (gameMode == null) {
            throw new IllegalArgumentException("難易度は0、1、2のいずれかで入力してください。");
        }
        return gameMode;
    }
}
