package jp.clip.typinggame.enums;

/**
 * APIやDBに保存するコード値と、画面表示用のラベルを持つenumの共通インターフェースです。
 */
public interface CodeEnum {

    /**
     * APIやDBに保存する外向きのコード値を取得します。
     *
     * @return コード値
     */
    String getKey();

    /**
     * 画面表示や説明に使うラベルを取得します。
     *
     * @return 表示ラベル
     */
    String getValue();
}
