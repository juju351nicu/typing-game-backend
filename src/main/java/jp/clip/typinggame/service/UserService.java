package jp.clip.typinggame.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jp.clip.typinggame.dto.RegisterUserRequest;
import jp.clip.typinggame.dto.UserResponse;
import jp.clip.typinggame.entity.User;
import jp.clip.typinggame.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * ユーザー登録とユーザー情報の変換を行うサービスです。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    /** ユーザー情報へアクセスするRepositoryです。 */
    private final UserRepository userRepository;

    /** パスワードを暗号化するEncoderです。 */
    private final PasswordEncoder passwordEncoder;

    /**
     * ユーザーを新規登録します。
     *
     * @param request ユーザー登録リクエスト
     * @return 登録後のユーザー情報
     */
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByLoginEmail(request.getLoginEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "メールアドレスは既に登録されています。");
        }

        User user = toUserEntity(request);
        return toResponse(userRepository.save(user));
    }

    /**
     * ユーザーEntityをレスポンスDTOへ変換します。
     *
     * @param user ユーザーEntity
     * @return ユーザーレスポンスDTO
     */
    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setLoginEmail(user.getLoginEmail());
        return response;
    }

    /**
     * ユーザー登録リクエストをEntityへ変換します。
     *
     * @param request ユーザー登録リクエスト
     * @return ユーザーEntity
     */
    private User toUserEntity(RegisterUserRequest request) {
        User user = new User();
        user.setLoginEmail(request.getLoginEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return user;
    }
}
