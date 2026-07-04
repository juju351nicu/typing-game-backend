package jp.clip.typinggame.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ゲーム終了時に保存するスコア情報を表すEntityです。
 *
 * <p>
 * {@code scores} テーブルの1レコードに対応し、スコア、ゲームルール、
 * WPM、正確率などランキング表示や分析に使う値を保持します。
 * </p>
 */
@Entity
@Table(name = "scores")
@Getter
@Setter
@NoArgsConstructor
public class Score {

    /** スコアIDです。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** クリアタイムです。 */
    @Column(nullable = false, length = 20)
    private String time;

    /** ゲームスコアです。 */
    @Column(nullable = false)
    private Integer score;

    /** 難易度を表すモード値です。 */
    @Column(nullable = false)
    private Integer mode;

    /** ゲームルールです。 */
    @Column(nullable = false, length = 20)
    private String gameRule;

    /** タイムアタック時の制限時間（秒）です。 */
    private Integer timeLimitSeconds;

    /** 1分あたりの入力文字数です。 */
    private Integer wpm;

    /** 入力の正確率です。 */
    private Integer accuracy;

    /** ミスタイプ数です。 */
    private Integer missCount;

    /** 正しく入力した文字数です。 */
    private Integer correctCharacterCount;

    /** スコア作成日時です。 */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
