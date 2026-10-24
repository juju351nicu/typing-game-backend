package jp.clip.typinggame.dao;

import java.time.LocalDateTime;
import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;

import jp.clip.typinggame.entity.User;

/**
 * ユーザー情報へアクセスするDomaのDAOです。
 *
 * <p>
 * SQLは {@code META-INF/jp/clip/typinggame/dao/UserDao/} 配下のファイルに書きます。
 * </p>
 */
@Dao
@ConfigAutowireable
public interface UserDao {

    /**
     * ユーザーIDからユーザー情報を取得します。
     *
     * @param id ユーザーID
     * @return ユーザー情報
     */
    @Select
    Optional<User> selectById(long id);

    /**
     * ログインメールアドレスからユーザー情報を取得します。
     *
     * @param loginEmail ログインメールアドレス
     * @return ユーザー情報
     */
    @Select
    Optional<User> selectByLoginEmail(String loginEmail);

    /**
     * ログインメールアドレスが登録済みか判定します。
     *
     * @param loginEmail ログインメールアドレス
     * @return 登録済みの場合はtrue
     */
    @Select
    boolean existsByLoginEmail(String loginEmail);

    /**
     * ユーザーを登録します。採番したIDは引数のEntityへ設定されます。
     *
     * @param user ユーザー情報
     * @return 登録件数
     */
    @Insert
    int insert(User user);

    /**
     * 最終ログイン日時を更新します。
     *
     * @param id ユーザーID
     * @param lastLoginAt 最終ログイン日時
     * @return 更新件数
     */
    @Update(sqlFile = true)
    int updateLastLoginAt(long id, LocalDateTime lastLoginAt);

    /**
     * トークン世代を1つ進め、発行済みJWTを失効させます。
     *
     * <p>
     * 読み込んだ値に1を足して書き戻すと、同時にログアウトした場合に更新が失われるため、
     * SQL上で {@code token_version + 1} として加算します。
     * </p>
     *
     * @param id ユーザーID
     * @param updatedAt 更新日時
     * @return 更新件数
     */
    @Update(sqlFile = true)
    int incrementTokenVersion(long id, LocalDateTime updatedAt);
}
