package jp.clip.typinggame.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ゲームルールenumの変換を確認するテストです。
 */
@DisplayName("ゲームルールenum")
class GameRuleEnumTest {

    /**
     * キー値からゲームルールenumを取得できることを確認します。
     */
    @Test
    @DisplayName("fromKey はキー値からゲームルールを取得する")
    void fromKeyReturnsGameRule() {
        assertThat(GameRuleEnum.fromKey("normal")).isEqualTo(GameRuleEnum.NORMAL);
        assertThat(GameRuleEnum.fromKey(" timeAttack ")).isEqualTo(GameRuleEnum.TIME_ATTACK);
        assertThat(GameRuleEnum.fromKey("TIMEATTACK")).isEqualTo(GameRuleEnum.TIME_ATTACK);
    }

    /**
     * enumからAPIやDBで使うキー値と表示ラベルを取得できることを確認します。
     */
    @Test
    @DisplayName("getKey と getValue はキー値と表示ラベルを返す")
    void getKeyAndGetValueReturnValues() {
        assertThat(GameRuleEnum.TIME_ATTACK.getKey()).isEqualTo("timeAttack");
        assertThat(GameRuleEnum.TIME_ATTACK.getValue()).isEqualTo("タイムアタック");
    }

    /**
     * 不正なキー値の場合に例外を送出することを確認します。
     */
    @Test
    @DisplayName("fromKey は不正なキー値の場合例外を送出する")
    void fromKeyThrowsExceptionWhenInvalid() {
        assertThatThrownBy(() -> GameRuleEnum.fromKey("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ゲームルールはnormalまたはtimeAttackを入力してください。");
    }
}
