# JWT化 移行計画

## 目的

GitHub Pages のフロントエンドと、将来EC2などで公開するバックエンドを接続しやすくするため、現在のセッションCookie方式からJWT方式への移行を検討します。

## 現在の状態

現在は Spring Security のセッションCookie方式です。

```text
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout
POST /api/me/scores
GET  /api/me/scores
```

FE側は `credentials: "include"` を付けてCookieを送受信します。

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

## 検討事項

- JWT署名鍵の管理方法
- access token の有効期限
- refresh token を使うか
- token保存場所を localStorage / sessionStorage / memory のどれにするか
- XSS対策
- ログアウト時のtoken破棄
- 期限切れ時のFE表示
- 未ログイン時の `fieldErrors` 形式
- Swagger UIでJWTを試す方法

## 影響範囲

Backend:

```text
SecurityConfig
AuthController
AuthService
LoginResponse
CurrentUserService
新規JWT utility
新規JWT filter
```

Frontend:

```text
src/stores/auth.ts
src/services/authService.ts
src/utils/fetchClient.ts
src/views/LoginPage.vue
```

## 実装順

1. JWT署名鍵と有効期限を `application.yml` または環境変数で設定できるようにする。
2. JWT生成・検証用のutilityを追加する。
3. ログイン成功時にaccess tokenを返す。
4. FEでtokenを保持する。
5. `fetchClient.ts` で `Authorization` ヘッダーを付けられるようにする。
6. BEにJWT filterを追加し、tokenから認証情報を復元する。
7. `/api/me/**` をJWT認証で保護する。
8. 未ログイン、期限切れ、改ざんtokenのエラーレスポンスを確認する。
9. BE / FEのテストを追加する。
10. セッションCookie方式を残すか削除するか判断する。

## 完了条件

- ログイン成功時にJWTが返る。
- FEから `Authorization` ヘッダー付きで `/api/me/scores` を呼べる。
- tokenなしではログインユーザー向けAPIが401になる。
- token不正、期限切れ時のエラー表示が分かりやすい。
- SwaggerまたはcurlでJWT認証APIを確認できる。
- FE単体モードとlocalStorage fallbackが壊れていない。

