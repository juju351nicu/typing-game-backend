package jp.clip.typinggame.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import jp.clip.typinggame.service.JwtTokenService;
import jp.clip.typinggame.service.LoginUserDetails;
import jp.clip.typinggame.service.LoginUserDetailsService;
import lombok.RequiredArgsConstructor;

/**
 * 検証済みJWTからSpring Securityの認証情報を生成するConverterです。
 */
@RequiredArgsConstructor
public class JwtLoginUserDetailsConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /** JWTのsubjectに設定したユーザーIDからログインユーザー情報を取得するサービスです。 */
    private final LoginUserDetailsService loginUserDetailsService;

    /**
     * 検証済みJWTを既存のLoginUserDetails認証へ変換します。
     *
     * @param jwt Spring Securityが署名・期限を検証済みのJWT
     * @return Spring Securityへ保存する認証情報
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String subject = jwt.getSubject();
        if (StringUtils.isBlank(subject)) {
            throw new BadCredentialsException("JWT subjectが空です。");
        }

        long userId;
        try {
            userId = Long.parseLong(subject);
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException("JWT subjectが不正です。", exception);
        }

        // JWTに個人情報を含めず、変更されない内部ユーザーIDから認証情報を復元します。
        LoginUserDetails userDetails = loginUserDetailsService.loadUserById(userId);
        Number tokenVersion = jwt.getClaim(JwtTokenService.TOKEN_VERSION_CLAIM);
        if (tokenVersion == null || tokenVersion.longValue() != userDetails.getTokenVersion()) {
            throw new BadCredentialsException("JWTは失効しています。");
        }

        // パスワード認証と同じprincipal型にそろえ、CurrentUserServiceを共通利用します。
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                jwt.getTokenValue(),
                userDetails.getAuthorities());
        authentication.setDetails(jwt);
        return authentication;
    }
}
