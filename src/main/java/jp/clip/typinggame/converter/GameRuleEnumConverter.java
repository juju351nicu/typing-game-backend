package jp.clip.typinggame.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jp.clip.typinggame.enums.GameRuleEnum;

/**
 * {@link GameRuleEnum} とDBカラム値を相互変換するJPA Converterです。
 *
 * <p>
 * Entityではenumとして扱い、DBには {@code normal} や {@code timeAttack}
 * のようなキー値を保存します。
 * </p>
 */
@Converter
public class GameRuleEnumConverter implements AttributeConverter<GameRuleEnum, String> {

    /**
     * EntityのenumをDBへ保存する文字列へ変換します。
     *
     * @param attribute Entityで保持しているゲームルール
     * @return DBへ保存するゲームルールキー
     */
    @Override
    public String convertToDatabaseColumn(GameRuleEnum attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getKey();
    }

    /**
     * DBの文字列をEntityで扱うenumへ変換します。
     *
     * @param dbData DBに保存されているゲームルールキー
     * @return Entityで扱うゲームルール
     */
    @Override
    public GameRuleEnum convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return GameRuleEnum.fromKey(dbData);
    }
}
