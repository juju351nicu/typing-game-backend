package jp.clip.typinggame.service;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import jp.clip.typinggame.config.JwtProperties;
import lombok.RequiredArgsConstructor;

/**
 * JWTアクセストークンを生成するサービスです。
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    /** JWTを文字列へエンコードするSpring Securityの部品です。 */
    private final JwtEncoder jwtEncoder;

    /** JWTのissuerや有効期限を保持する設定値です。 */
    private final JwtProperties jwtProperties;

    /**
     * ログインユーザー情報からアクセストークンを生成します。
     *
     * @param userDetails ログインユーザー情報
     * @return JWTアクセストークン
     */
    public String generateAccessToken(LoginUserDetails userDetails) {
        // 現在時刻を基準に、発行日時と有効期限をJWT claimへ入れます。
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(jwtProperties.getExpiresInSeconds());

        // subjectはログインメールアドレス、userIdはアプリ内のユーザー特定用claimとして持たせます。
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getUserId())
                .build();

        // FEへ返すtokenは、Spring SecurityのDecoderで検証できるHS256署名付きJWTにします。
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * アクセストークンの有効期間を秒で返します。
     *
     * @return アクセストークンの有効期間（秒）
     */
    public long getExpiresInSeconds() {
        return jwtProperties.getExpiresInSeconds();
    }
}
