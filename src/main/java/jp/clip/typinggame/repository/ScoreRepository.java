package jp.clip.typinggame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.clip.typinggame.entity.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    List<Score> findAllByOrderByCreatedAtDesc();
}
