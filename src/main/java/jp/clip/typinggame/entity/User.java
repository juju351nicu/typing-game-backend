package jp.clip.typinggame.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
 * アプリケーションに登録されたユーザー情報を表すEntityです。
 *
 * <p>
 * {@code users} テーブルの1レコードに対応し、ログインメールアドレス、
 * 暗号化済みパスワード、ログイン日時などを保持します。
 * </p>
 */
@Entity
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
    @Column(nullable = false, unique = true, length = 255)
    private String loginEmail;

    /** BCryptで暗号化したパスワードです。 */
    @Column(nullable = false, length = 100)
    private String password;

    /** 最後にログインした日時です。 */
    private LocalDateTime lastLoginAt;

    /** ユーザー作成日時です。 */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** ユーザー更新日時です。 */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
