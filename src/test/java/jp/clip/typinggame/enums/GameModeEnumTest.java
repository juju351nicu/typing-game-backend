package jp.clip.typinggame.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ゲーム難易度enumの変換を確認するテストです。
 */
@DisplayName("ゲーム難易度enum")
class GameModeEnumTest {

    /**
     * キー値からゲーム難易度enumを取得できることを確認します。
     */
    @Test
    @DisplayName("fromKey はキー値からゲーム難易度を取得する")
    void fromKeyReturnsGameMode() {
        assertThat(GameModeEnum.fromKey(0)).isEqualTo(GameModeEnum.EASY);
        assertThat(GameModeEnum.fromKey(1)).isEqualTo(GameModeEnum.NORMAL);
        assertThat(GameModeEnum.fromKey(2)).isEqualTo(GameModeEnum.HARD);
    }

    /**
     * enumからAPIやDBで使うキー値と表示ラベルを取得できることを確認します。
     */
    @Test
    @DisplayName("getKey と getValue はキー値と表示ラベルを返す")
    void getKeyAndGetValueReturnValues() {
        assertThat(GameModeEnum.HARD.getKey()).isEqualTo(2);
        assertThat(GameModeEnum.HARD.getValue()).isEqualTo("Hard");
    }

    /**
     * 不正なキー値の場合に例外を送出することを確認します。
     */
    @Test
    @DisplayName("fromKey は不正なキー値の場合例外を送出する")
    void fromKeyThrowsExceptionWhenInvalid() {
        assertThatThrownBy(() -> GameModeEnum.fromKey(9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("難易度は0、1、2のいずれかで入力してください。");
    }
}
