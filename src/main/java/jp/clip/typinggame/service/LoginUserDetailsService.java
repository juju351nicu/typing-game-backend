package jp.clip.typinggame.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jp.clip.typinggame.dao.UserDao;
import jp.clip.typinggame.entity.User;
import lombok.RequiredArgsConstructor;

/**
 * Spring Securityが認証時にユーザー情報を取得するためのサービスです。
 */
@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

    /** ユーザー情報へアクセスするDAOです。 */
    private final UserDao userDao;

    /**
     * ログインメールアドレスから認証用ユーザー情報を取得します。
     *
     * @param username Spring Securityから渡されるログインメールアドレス
     * @return 認証用ユーザー情報
     * @throws UsernameNotFoundException ユーザーが存在しない場合
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.selectByLoginEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません。"));

        return toLoginUserDetails(user);
    }

    /**
     * ユーザーIDからJWT認証用ユーザー情報を取得します。
     *
     * @param userId ユーザーID
     * @return 認証用ユーザー情報
     * @throws UsernameNotFoundException ユーザーが存在しない場合
     */
    public LoginUserDetails loadUserById(long userId) throws UsernameNotFoundException {
        User user = userDao.selectById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません。"));

        return toLoginUserDetails(user);
    }

    /** ユーザーEntityをSpring Security用のユーザー情報へ変換します。 */
    private LoginUserDetails toLoginUserDetails(User user) {
        LoginUserDetails userDetails = new LoginUserDetails();
        userDetails.setUserId(user.getId());
        userDetails.setUsername(user.getLoginEmail());
        userDetails.setPassword(user.getPassword());
        userDetails.setTokenVersion(user.getTokenVersion());
        return userDetails;
    }
}
