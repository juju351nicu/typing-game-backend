package jp.clip.typinggame.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * ページ単位のランキング情報を返却するAPIレスポンスDTOです。
 *
 * <p>
 * 画面のページ送りに必要な総件数・総ページ数を、表示する行と一緒に返します。
 * 全件をフロントエンドへ返さずに済むため、スコアが増えても転送量が一定に保たれます。
 * </p>
 */
@Getter
@Setter
public class RankingPageResponse {

    /** 指定ページに表示するスコア情報です。 */
    @JsonProperty("items")
    private List<ScoreResponse> items;

    /** 現在のページ番号（1始まり）です。 */
    @JsonProperty("page")
    private Integer page;

    /** 1ページあたりの件数です。 */
    @JsonProperty("size")
    private Integer size;

    /** 絞り込み条件に一致する総件数です。 */
    @JsonProperty("totalCount")
    private Long totalCount;

    /** 総ページ数です。該当データがない場合も1を返します。 */
    @JsonProperty("totalPages")
    private Integer totalPages;
}
