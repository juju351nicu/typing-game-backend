package jp.clip.typinggame.dto;

/**
 * スコア情報を返却するAPIレスポンスDTOです。
 *
 * @param id スコアID
 * @param time クリアタイム
 * @param score ゲームスコア
 * @param mode 難易度を表すモード値
 * @param gameRule ゲームルール
 * @param timeLimitSeconds タイムアタック時の制限時間（秒）
 * @param wpm 1分あたりの入力文字数
 * @param accuracy 入力の正確率
 * @param missCount ミスタイプ数
 * @param correctCharacterCount 正しく入力した文字数
 * @param date スコア作成日時
 */
public record ScoreResponse(
        Long id,
        String time,
        Integer score,
        Integer mode,
        String gameRule,
        Integer timeLimitSeconds,
        Integer wpm,
        Integer accuracy,
        Integer missCount,
        Integer correctCharacterCount,
        String date) {
}
