package jp.clip.typinggame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.clip.typinggame.entity.User;

/**
 * ユーザー情報へアクセスするRepositoryです。
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * ログインメールアドレスからユーザー情報を取得します。
     *
     * @param loginEmail ログインメールアドレス
     * @return ユーザー情報
     */
    Optional<User> findByLoginEmail(String loginEmail);

    /**
     * ログインメールアドレスが登録済みか判定します。
     *
     * @param loginEmail ログインメールアドレス
     * @return 登録済みの場合はtrue
     */
    boolean existsByLoginEmail(String loginEmail);
}
