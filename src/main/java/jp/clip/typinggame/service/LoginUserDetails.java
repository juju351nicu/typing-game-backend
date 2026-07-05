package jp.clip.typinggame.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.Setter;

/**
 * Spring Securityの認証処理で利用するログインユーザー情報です。
 */
@Getter
@Setter
public class LoginUserDetails implements UserDetails {

    /** シリアルバージョンUIDです。 */
    private static final long serialVersionUID = 1L;

    /** ユーザーIDです。 */
    private Long userId;

    /** ログインメールアドレスです。 */
    private String username;

    /** 暗号化済みパスワードです。 */
    private String password;

    /**
     * ユーザーに付与されている権限を返します。
     *
     * @return 権限一覧
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    /**
     * アカウントが有効期限内かを返します。
     *
     * @return 有効期限内の場合はtrue
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * アカウントがロックされていないかを返します。
     *
     * @return ロックされていない場合はtrue
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 資格情報が有効期限内かを返します。
     *
     * @return 有効期限内の場合はtrue
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * アカウントが有効かを返します。
     *
     * @return 有効な場合はtrue
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
