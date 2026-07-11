# JWT化 移行計画

## 目的

GitHub Pages のフロントエンドと、将来EC2などで公開するバックエンドを接続しやすくするため、現在のセッションCookie方式からJWT方式への移行を検討します。

## 現在の状態

現在は Spring Security のセッションCookie方式とJWT Bearer token方式を並行しています。
ログイン成功時に `accessToken`、`tokenType`、`expiresIn` を返し、`Authorization: Bearer {token}` からログインユーザーを復元できます。

既存の結合確認済み動作を壊さないため、現時点ではセッションCookie方式も残しています。

```text
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout
POST /api/me/scores
GET  /api/me/scores
```

FE側は移行期間中、セッションCookie方式では `credentials: "include"`、JWT方式では `Authorization` ヘッダーを使います。

この方式はローカル学習には分かりやすい一方、GitHub Pages と別ホストのBEをつなぐ場合、CORS、SameSite、HTTPS、Cookieドメインの影響を受けやすくなります。

## JWT化する理由

- GitHub Pages FE から別ホストBEへ接続しやすくする。
- `Authorization` ヘッダーで認証状態を明示できる。
- EC2公開前に、認証方式の切り分けをローカルで済ませる。
- Spring Security の filter、認証情報復元、未ログインエラー処理を学習できる。

## 初期方針

最初は access token のみで小さく実装します。

```text
ログイン成功
-> access token を返す
-> FEがtokenを保持
-> Authorization: Bearer {token} でAPIを呼ぶ
-> BEのJWT filterで認証情報を復元
```

refresh token は、access token方式が安定してから検討します。

## 採用方針

Phase8の最初の実装では、以下の方針にします。

```text
認証方式: Bearer token
token種類: access tokenのみ
FE保存場所: sessionStorage
送信方法: Authorization: Bearer {token}
有効期限: 30分〜60分を候補にする
refresh token: 後回し
既存セッションCookie方式: JWT移行完了までは必要に応じて残す
```

Phase8以降の判断として、typingGameの最終的な主方式はJWT Bearer認証に寄せます。
セッションCookie方式は、ローカル学習と移行期間の互換用として残します。

理由:

- GitHub PagesなどのFE単体公開と、将来EC2などで公開するBEを分けやすい。
- Swagger UIやcurlで `Authorization: Bearer {token}` を明示して試しやすい。
- Cookieの `SameSite`、`Secure`、ドメイン、CORS、HTTPS条件に依存する部分を減らせる。
- Spring SecurityのResource Server構成、JWT検証、認証情報復元、401応答を学習できる。

現時点では、すぐにセッションCookie方式を削除しません。
JWT化で問題が起きた場合に、既存のセッション方式と比較して原因を切り分けるためです。

### sessionStorageを選ぶ理由

最初のJWT化では、tokenを `sessionStorage` に保存します。

理由:

- 画面リロード後も同じタブ内ではログイン状態を復元できる。
- ブラウザを閉じるとtokenが消えるため、localStorageより残り続けにくい。
- httpOnly Cookieより実装が単純で、JWTの仕組みを学びやすい。
- GitHub Pages FE から別ホストBEへ `Authorization` ヘッダーで送信しやすい。

注意:

- `sessionStorage` でもXSSで読み取られるリスクはある。
- 将来、より実務寄りにする場合は、短命access token + refresh token + httpOnly Secure Cookieの構成を検討する。
- 今回は学習と小規模アプリの扱いやすさを優先する。

### localStorageを最初に選ばない理由

`localStorage` はブラウザを閉じてもtokenが残るため、ユーザー体験は良くなります。
ただし、XSS時にtokenが長く残りやすく、ログアウト忘れや共有端末での残留も起きやすくなります。

typingGameでは、ログイン状態を長期間維持する重要度はまだ高くありません。
そのため、最初は `sessionStorage` で十分です。

### httpOnly Cookieを最初に選ばない理由

httpOnly Cookie はJavaScriptからtokenを読めないため、XSSへの耐性は上がります。
一方で、GitHub Pages FE と別ホストBEの構成では、Cookieの `SameSite=None`、`Secure`、HTTPS、CORS、ドメイン設定が絡みます。

今回は「セッションCookie方式で困った点をJWTで切り分ける」ことが目的なので、最初は `Authorization` ヘッダー方式に寄せます。

## Cookie無効時とSpring Securityの関係

Cookieを無効にしても、Spring Securityそのものが使えなくなるわけではありません。

Spring Securityは、認証方式を1つだけに固定する仕組みではありません。
セッションCookie方式、Basic認証、フォームログイン、JWT Bearer認証、OAuth2 Resource Serverなど、複数の認証方式を扱えます。

Cookie無効時に影響を受けるのは、主に「HTTPセッションをCookieで維持する方式」です。

```text
セッションCookie方式
ログイン成功
-> サーバー側にSecurityContextを保存
-> ブラウザへ JSESSIONID Cookie を返す
-> 次回API呼び出しで JSESSIONID Cookie を送る
-> サーバーが同じログインユーザーとして扱う
```

この流れでは、ブラウザがCookieを保存・送信できないと、次回リクエストで同じHTTPセッションを特定できません。
そのため、ログイン直後のレスポンスは成功しても、次の `/api/auth/me` や `/api/me/scores` で未ログイン扱いになりやすくなります。

一方、JWT Bearer認証ではCookieを使わず、リクエストヘッダーで認証情報を送ります。

```text
JWT Bearer方式
ログイン成功
-> accessToken をレスポンスで返す
-> FEが sessionStorage に保存
-> 次回API呼び出しで Authorization: Bearer {token} を送る
-> Spring Security Resource Server がJWTを検証する
-> ログインユーザーとして扱う
```

この方式では、ブラウザのCookieが無効でも、`Authorization` ヘッダーを送れる限りSpring Securityで認証できます。

整理:

```text
Cookie無効
-> Spring Security全体が使えないわけではない
-> セッションCookie方式はログイン継続が難しくなる
-> JWT Bearer方式はAuthorizationヘッダーなので利用できる
```

typingGameでは、将来FEとBEを別ホストで動かす可能性が高いため、最終的にはJWT Bearer方式を主方式にします。
セッションCookie方式は、Spring Securityの基本学習と移行期間の比較用として扱います。

## localStorage / sessionStorage / Cookie の違い

認証方式の判断では、`localStorage`、`sessionStorage`、Cookieを混同しないようにします。

typingGameでの役割は以下です。

```text
localStorage
-> 未ログインユーザーのスコア保存
-> API失敗時のfallback表示
-> 認証tokenの保存場所にはしない

sessionStorage
-> JWT access token の保存
-> 同じタブではリロード後も使える
-> ブラウザまたはタブを閉じると消える

Cookie
-> セッションCookie方式で JSESSIONID を保持
-> httpOnly Cookie方式のtoken保存にも使える
-> FE/BE別ホストでは SameSite / Secure / CORS / HTTPS の考慮が必要
```

`localStorage` を使っているからといって、セッションCookie方式やJWT方式と同じ意味にはなりません。
typingGameの `localStorage` は、ゲーム体験をFE単体で成立させるためのスコア保存先です。
ログイン状態の管理とは別の責務として扱います。

tokenを `localStorage` に保存する案もありますが、現時点では採用しません。
ブラウザを閉じてもtokenが残り続けるため、共有端末やXSS時の影響が大きくなりやすいからです。
学習段階では、まず `sessionStorage` に保存して、tokenの発行・送信・期限切れ処理を小さく理解する方針です。

## 依存ライブラリ方針

JWT実装では、Spring Security の Resource Server / JOSE 系を使います。

追加済みの依存:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

理由:

- Spring Security の認証フィルターに乗せやすい。
- `Authorization: Bearer ...` の扱いをSpring Securityの標準構成に寄せられる。
- 独自filterをすべて手書きするより、実務で見る構成に近い。
- JWT検証、認証情報復元、401応答をSpring Securityの流れで学習できる。

補足:

- jjwt のようなJWT専用ライブラリを使う方法もある。
- ただし、Spring Securityと組み合わせる場合は、Resource Server方式の方が認可設定と一体で扱いやすい。
- typingGameではSpring Security標準寄りの流れを学ぶため、Resource Server / JOSE 系を採用する。

### todo-backendとの関係

`todo-backend` にはAuth0 `java-jwt` と独自filterを使ったJWT実装があります。
typingGameでは直接流用せず、以下の考え方を参考にします。

- リクエストヘッダーからtokenを取得する。
- tokenを検証する。
- 検証後にSpring Securityの認証情報を `SecurityContext` へ入れる。
- 認証が必要なAPIは、Spring Securityの認可設定で保護する。

一方、typingGameではSpring Security Resource Server / JOSE系を使うため、署名検証やBearer tokenの取り出しはSpring Securityに寄せます。
独自filterを丸ごと書くより、今回の構成では標準機能に寄せた方が学習・保守の両面で扱いやすいためです。

## 実装済みのJWT基盤

2026-07-11時点で、以下を実装済みです。

- `spring-boot-starter-oauth2-resource-server` を追加。
- `app.jwt.secret`、`app.jwt.expires-in-seconds`、`app.jwt.issuer` を設定化。
- `JwtProperties` でJWT設定値を保持。
- `JwtConfig` で `JwtEncoder` と `JwtDecoder` をBean化。
- `JwtTokenService` でログインユーザーからJWTアクセストークンを生成。
- `LoginResponse` に `accessToken`、`tokenType`、`expiresIn` を追加。
- `POST /api/auth/login` 成功時にJWTアクセストークンを返す。
- `SecurityConfig` でResource ServerのJWT認証を有効化。
- `JwtLoginUserDetailsConverter` で検証済みJWTから `LoginUserDetails` を復元。
- `Authorization: Bearer {token}` で `/api/auth/me` と `/api/me/scores` を呼べる。
- 不正なBearer tokenでも `fieldErrors` 形式の401を返す。
- Swagger UIのAuthorizeからBearer tokenを設定できる。
- `AuthControllerTest` でログインレスポンスにJWT項目が含まれることを確認。
- `AuthControllerTest` と `MyScoreControllerTest` でBearer token認証を確認。
- `OpenApiConfigTest` でOpenAPI JSONにBearer認証定義が含まれることを確認。

現時点の役割:

```text
ログイン成功
-> セッションCookieを作る
-> 追加でaccess tokenも返す
Authorization: Bearer {token}
-> BEでJWTを検証
-> SecurityContextにログインユーザー情報を復元
-> /api/me/** をJWTで呼べるようにする
```

## API仕様変更案

ログインレスポンスは、従来の `user` に加えて `accessToken`、`tokenType`、`expiresIn` を返します。

従来:

```json
{
  "user": {
    "id": 1,
    "loginEmail": "user@example.com"
  }
}
```

現在:

```json
{
  "accessToken": "xxxxx.yyyyy.zzzzz",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "loginEmail": "user@example.com"
  }
}
```

項目:

- `accessToken`: API呼び出しに使うJWT
- `tokenType`: まずは `Bearer` 固定
- `expiresIn`: token有効期限（秒）
- `user`: ログイン中ユーザー情報

`GET /api/auth/me` と `/api/me/**` は、JWTの `Authorization` ヘッダーからログインユーザーを取得できます。
現時点では、移行期間のためセッションCookie方式でも動作します。

```http
Authorization: Bearer xxxxx.yyyyy.zzzzz
```

## エラーレスポンス方針

JWT化後も、エラー形式は既存の `fieldErrors` 形式を維持します。

tokenなし:

```json
{
  "fieldErrors": [
    {
      "errorCode": "UNAUTHORIZED",
      "field": "",
      "message": "ログインしてください。"
    }
  ]
}
```

token期限切れ:

```json
{
  "fieldErrors": [
    {
      "errorCode": "TOKEN_EXPIRED",
      "field": "",
      "message": "ログインの有効期限が切れました。再度ログインしてください。"
    }
  ]
}
```

token不正:

```json
{
  "fieldErrors": [
    {
      "errorCode": "INVALID_TOKEN",
      "field": "",
      "message": "ログイン情報が正しくありません。再度ログインしてください。"
    }
  ]
}
```

## セッションCookie方式との関係

JWT化の実装中は、セッションCookie方式をすぐには削除しません。

理由:

- 現在のローカル結合確認済みの動作を壊さずに比較できる。
- JWT化で問題が出た場合、既存方式に戻して切り分けできる。
- Spring Securityの設定差分を学習しやすい。

ただし、最終的な主方式はJWT Bearer認証に寄せます。
セッションCookie方式は、移行期間と学習用の比較対象として残します。

Phase8完了時の目標は以下です。

```text
開発・将来公開の主方式: JWT
セッションCookie方式: 移行期間とローカル学習用として残す
localStorage: 認証ではなく未ログインスコア保存用として残す
```

今後削除を検討するタイミング:

- FE/BE別ホスト構成でJWT認証が安定した後。
- Swagger UI、curl、FE画面からJWT認証APIを一通り確認した後。
- セッションCookie方式のテストが学習目的を終えた後。

削除する場合は、`SessionCreationPolicy.IF_REQUIRED`、ログイン時のHTTPセッション保存、`credentials: "include"` 前提のFE実装、セッション方式のテストをまとめて整理します。
残す場合は、「JWTが主方式、セッションCookieはローカル学習・互換用」とREADME/docsに明記し続けます。

## 検討事項

- JWT署名鍵の管理方法
- access token の有効期限
- refresh token を使うか
- token保存場所を `sessionStorage` で開始して問題ないか
- XSS対策
- ログアウト時のtoken破棄
- 期限切れ時のFE表示
- 未ログイン時の `fieldErrors` 形式
- Swagger UIでJWTを試す方法
- 既存セッションCookie方式をいつ削除するか

## 影響範囲

Backend:

```text
SecurityConfig
AuthController
AuthService
LoginResponse
CurrentUserService
新規JWT utility
JwtLoginUserDetailsConverter
```

Frontend:

```text
src/stores/auth.ts
src/services/authService.ts
src/utils/fetchClient.ts
src/views/LoginPage.vue
```

## 実装順

1. Spring Security OAuth2 Resource Server / JOSE系の依存を追加する。完了。
2. JWT署名鍵と有効期限を `application.yml` または環境変数で設定できるようにする。完了。
3. JWT生成・検証用の設定またはutilityを追加する。完了。
4. `LoginResponse` に `accessToken`、`tokenType`、`expiresIn` を追加する。完了。
5. ログイン成功時にaccess tokenを返す。完了。
6. FEでtokenを `sessionStorage` に保持する。完了。
7. `fetchClient.ts` で `Authorization` ヘッダーを付けられるようにする。完了。
8. BEでJWTから認証情報を復元する。完了。
9. `/api/me/**` をJWT認証で保護する。完了。
10. 未ログイン、期限切れ、改ざんtokenのエラーレスポンスを確認する。一部完了。
11. BE / FEのテストを追加する。一部完了。
12. セッションCookie方式を残すか削除するか判断する。

## 完了条件

- ログイン成功時にJWTが返る。
- FEから `Authorization` ヘッダー付きで `/api/me/scores` を呼べる。
- tokenなしではログインユーザー向けAPIが401になる。
- token不正、期限切れ時のエラー表示が分かりやすい。
- SwaggerまたはcurlでJWT認証APIを確認できる。
- FE単体モードとlocalStorage fallbackが壊れていない。
