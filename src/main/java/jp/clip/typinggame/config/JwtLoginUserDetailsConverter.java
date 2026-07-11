package jp.clip.typinggame.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import jp.clip.typinggame.service.LoginUserDetails;
import jp.clip.typinggame.service.LoginUserDetailsService;
import lombok.RequiredArgsConstructor;

/**
 * 検証済みJWTからSpring Securityの認証情報を生成するConverterです。
 */
@RequiredArgsConstructor
public class JwtLoginUserDetailsConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /** JWTのsubjectからログインユーザー情報を取得するサービスです。 */
    private final LoginUserDetailsService loginUserDetailsService;

    /**
     * 検証済みJWTを既存のLoginUserDetails認証へ変換します。
     *
     * @param jwt Spring Securityが署名・期限を検証済みのJWT
     * @return Spring Securityへ保存する認証情報
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String loginEmail = jwt.getSubject();
        if (StringUtils.isBlank(loginEmail)) {
            throw new BadCredentialsException("JWT subjectが空です。");
        }

        // JWTのsubjectにはログインメールアドレスを入れているため、既存のUserDetailsServiceで復元します。
        LoginUserDetails userDetails = (LoginUserDetails) loginUserDetailsService.loadUserByUsername(loginEmail);

        // 既存のセッション認証と同じprincipal型にそろえ、CurrentUserServiceを共通利用します。
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                jwt.getTokenValue(),
                userDetails.getAuthorities());
        authentication.setDetails(jwt);
        return authentication;
    }
}
