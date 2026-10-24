package jp.clip.typinggame.entity;

import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.jdbc.entity.NamingType;

import jp.clip.typinggame.enums.UserRoleEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * アプリケーションに登録されたユーザー情報を表すEntityです。
 *
 * <p>
 * {@code users} テーブルの1レコードに対応し、ログインメールアドレス、
 * 暗号化済みパスワード、権限、ログイン日時などを保持します。
 * カラム名はフィールド名をスネークケースへ変換した名前です。
 * </p>
 */
@Entity(naming = NamingType.SNAKE_LOWER_CASE)
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    /** ユーザーIDです。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ログインに利用するメールアドレスです。 */
    private String loginEmail;

    /** BCryptで暗号化したパスワードです。 */
    private String password;

    /** ユーザー権限です。DBにはuser / adminのキー値で保存します。 */
    private UserRoleEnum role;

    /** 最後にログインした日時です。 */
    private LocalDateTime lastLoginAt;

    /** ログアウト時に進め、過去に発行したJWTを無効化する世代番号です。 */
    private long tokenVersion;

    /** ユーザー作成日時です。 */
    private LocalDateTime createdAt;

    /** ユーザー更新日時です。 */
    private LocalDateTime updatedAt;
}
