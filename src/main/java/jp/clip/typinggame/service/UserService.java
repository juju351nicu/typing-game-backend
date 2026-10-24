package jp.clip.typinggame.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.seasar.doma.jdbc.UniqueConstraintException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jp.clip.typinggame.dao.UserDao;
import jp.clip.typinggame.dto.RegisterUserRequest;
import jp.clip.typinggame.dto.UserResponse;
import jp.clip.typinggame.entity.User;
import jp.clip.typinggame.enums.UserRoleEnum;
import lombok.RequiredArgsConstructor;

/**
 * ユーザー登録とユーザー情報の変換を行うサービスです。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    /** ユーザー情報へアクセスするDAOです。 */
    private final UserDao userDao;

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
        if (userDao.existsByLoginEmail(request.getLoginEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "メールアドレスは既に登録されています。");
        }

        User user = toUserEntity(request);
        try {
            userDao.insert(user);
        } catch (UniqueConstraintException e) {
            // 存在確認の後に同じメールアドレスで同時登録された場合も、一意制約違反を409として返します。
            throw new ResponseStatusException(HttpStatus.CONFLICT, "メールアドレスは既に登録されています。");
        }
        return toResponse(user);
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
        response.setRole(user.getRole());
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
        // 管理者はAPIから登録できないため、新規登録は常に一般ユーザーです。
        user.setRole(UserRoleEnum.USER);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }
}
