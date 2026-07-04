package jp.clip.typinggame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.clip.typinggame.entity.Score;

/**
 * スコア情報へアクセスするRepositoryです。
 */
public interface ScoreRepository extends JpaRepository<Score, Long> {

    /**
     * スコア情報を作成日時の降順で取得します。
     *
     * @return 作成日時の降順で並んだスコア情報の一覧
     */
    List<Score> findAllByOrderByCreatedAtDesc();
}
