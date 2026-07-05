package jp.clip.typinggame.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.clip.typinggame.entity.User;
import jp.clip.typinggame.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Spring Securityの認証情報からログイン中ユーザーを取得するサービスです。
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    /** ユーザー情報へアクセスするRepositoryです。 */
    private final UserRepository userRepository;

    /**
     * 認証情報からユーザーEntityを取得します。
     *
     * @param authentication 認証情報
     * @return ログイン中ユーザー
     */
    @Transactional(readOnly = true)
    public User findAuthenticatedUser(Authentication authentication) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        return userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalStateException("ログインユーザーが見つかりません。"));
    }
}
