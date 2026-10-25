package jp.clip.typinggame.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ScoreServiceのページ計算を確認するテストです。
 */
@DisplayName("ScoreServiceのページ計算")
class ScoreServiceTest {

    @Test
    @DisplayName("1ページ目のOFFSETは0になる")
    void calculateOffsetReturnsZeroForFirstPage() {
        assertThat(ScoreService.calculateOffset(1, 10)).isZero();
    }

    @Test
    @DisplayName("2ページ目以降のOFFSETは前ページまでの件数になる")
    void calculateOffsetSkipsPreviousPages() {
        assertThat(ScoreService.calculateOffset(3, 10)).isEqualTo(20);
    }

    @Test
    @DisplayName("総件数がページサイズで割り切れる場合は商がページ数になる")
    void calculateTotalPagesWhenDivisible() {
        assertThat(ScoreService.calculateTotalPages(20, 10)).isEqualTo(2);
    }

    @Test
    @DisplayName("端数がある場合はページ数を切り上げる")
    void calculateTotalPagesRoundsUp() {
        assertThat(ScoreService.calculateTotalPages(21, 10)).isEqualTo(3);
    }

    @Test
    @DisplayName("0件の場合も空の1ページとして扱う")
    void calculateTotalPagesReturnsOneWhenEmpty() {
        assertThat(ScoreService.calculateTotalPages(0, 10)).isEqualTo(1);
    }
}
