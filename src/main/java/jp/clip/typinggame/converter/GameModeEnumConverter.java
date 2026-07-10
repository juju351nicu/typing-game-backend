package jp.clip.typinggame.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jp.clip.typinggame.enums.GameModeEnum;

/**
 * {@link GameModeEnum} とDBカラム値を相互変換するJPA Converterです。
 *
 * <p>
 * Entityではenumとして扱い、DBには既存仕様に合わせて {@code 0}、{@code 1}、{@code 2}
 * の数値を保存します。
 * </p>
 */
@Converter
public class GameModeEnumConverter implements AttributeConverter<GameModeEnum, Integer> {

    /**
     * EntityのenumをDBへ保存する数値へ変換します。
     *
     * @param attribute Entityで保持しているゲーム難易度
     * @return DBへ保存する難易度コード
     */
    @Override
    public Integer convertToDatabaseColumn(GameModeEnum attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getKey();
    }

    /**
     * DBの数値をEntityで扱うenumへ変換します。
     *
     * @param dbData DBに保存されている難易度コード
     * @return Entityで扱うゲーム難易度
     */
    @Override
    public GameModeEnum convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return GameModeEnum.fromKey(dbData);
    }
}
