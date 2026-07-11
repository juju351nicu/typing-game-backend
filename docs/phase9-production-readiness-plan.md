# Phase9 本番公開準備メモ

## 目的

Phase9では、すぐにEC2へ公開するのではなく、将来公開するときに迷わないようにバックエンド設定を整理します。

現時点では、ローカル開発と学習を優先します。
そのため、Phase9のゴールは「本番公開する」ではなく、「本番公開へ進む前に必要な設定項目を洗い出し、ローカル設定と本番設定を分けられる状態にする」です。

## 現在の到達点

2026-07-11時点では、以下まで完了しています。

- MySQL + Flyway でスキーマ管理できる。
- スコア保存、ランキング、ユーザー登録、ログインAPIがある。
- Spring Securityを使っている。
- JWT Bearer認証でログインユーザー向けAPIを呼べる。
- Swagger UIでBearer tokenを使ってAPI確認できる。
- セッションCookie方式は移行期間とローカル学習用として残している。
- FEはGitHub Pages向けに、バックエンドAPI無効でもlocalStorage保存で動く。

## Phase9でやること

### 1. ローカル設定と本番設定の責務を分ける

現在の `application.yml` はローカル開発向けの設定です。

主な現状:

```yaml
server:
  address: localhost
  port: 8091

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/typing_game...
    username: root
    password: ${DB_PASSWORD:}

app:
  cors:
    allowed-origins:
      - http://localhost:8081
      - http://localhost:5173
  jwt:
    secret: ${JWT_SECRET:local-development-jwt-secret-key-please-change-32bytes}
    expires-in-seconds: ${JWT_EXPIRES_IN_SECONDS:3600}
```

ローカルではこのままで良いですが、本番公開時は以下を環境変数または `application-prod.yml` 側へ分けます。

```text
DB接続URL
DBユーザー
DBパスワード
JWT secret
JWT有効期限
CORS許可Origin
server.address
server.port
Swagger UIを公開するかどうか
```

### 2. JWT secretを本番用に必ず外出しする

`JWT_SECRET` は本番で固定のローカル文字列を使ってはいけません。

理由:

- GitHubに載っている値は秘密情報ではない。
- secretが漏れると、第三者が署名済みtokenを作れる可能性がある。
- JWT Bearer方式を主方式にするため、secretの管理は重要になる。

本番では、最低でも以下のように環境変数で渡します。

```bash
JWT_SECRET="本番用の十分に長いランダム文字列"
JWT_EXPIRES_IN_SECONDS=3600
```

将来EC2で動かす場合は、`systemd` のEnvironment、環境変数ファイル、またはAWS側のSecret管理を検討します。
最初の学習段階では、EC2上の環境変数ファイルから始めて問題ありません。

### 3. CORS許可Originを環境ごとに分ける

ローカルでは以下を許可しています。

```text
http://localhost:8081
http://localhost:5173
```

GitHub PagesからバックエンドAPIへ接続する場合は、GitHub PagesのURLを追加する必要があります。

例:

```text
https://juju351nicu.github.io
https://juju351nicu.github.io/typingGame
```

注意:

- CORSのOriginは、パスを含めず `scheme + host + port` で扱われる。
- GitHub Pagesの実際のOriginは `https://ユーザー名.github.io` になる。
- `https://juju351nicu.github.io/typingGame` のようなパス付き文字列をCORSのOriginとして扱うかは、設定方法とブラウザのOrigin判定を確認する。
- 本番で `*` と `allowCredentials=true` を組み合わせない。

JWT Bearer方式を主方式にする場合、Cookie送受信への依存は減ります。
ただし、現時点ではセッションCookie方式も移行期間として残しているため、`allowCredentials=true` は維持しています。

### 4. server.addressの扱いを整理する

現在はローカル開発向けに以下です。

```yaml
server:
  address: localhost
```

これはローカルでは安全ですが、EC2など外部からアクセスさせる環境では不向きです。
本番公開時は、外部から受ける構成に応じて以下を検討します。

```yaml
server:
  address: 0.0.0.0
```

ただし、いきなり公開ポートを開けるのではなく、Nginxのリバースプロキシ、セキュリティグループ、HTTPSを合わせて確認します。

### 5. Swagger UIの公開範囲を決める

ローカルではSwagger UIを使います。

```text
http://localhost:8091/swagger-ui.html
```

本番公開時は、以下を検討します。

- Swagger UIを本番でも公開するか。
- 公開する場合、誰でも見られる状態で良いか。
- 学習用途だけなら、本番では無効にするか。

本番で無効にする候補:

```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

最初のEC2学習では、短期間だけ有効にして疎通確認後に無効化する運用でも良いです。

### 6. セッションCookie方式を残すか削除するか

Phase9開始時点では、セッションCookie方式は削除しません。

理由:

- JWT方式と比較して学習できる。
- 既存テストがセッション方式の挙動も確認している。
- まだEC2公開前で、削除によるリスクを取る必要が低い。

ただし、最終的な主方式はJWT Bearer認証です。
GitHub Pages FE + 別ホストBEの構成では、CookieよりAuthorizationヘッダー方式の方が扱いやすいからです。

削除を検討するタイミング:

- FEからJWTだけでログイン、スコア保存、ランキング表示が安定した後。
- Swagger UIとcurlでJWT認証APIを確認できた後。
- 本番公開構成でCookieを使わない方針が固まった後。

削除する場合に見る箇所:

```text
SecurityConfig の SessionCreationPolicy
AuthService の HTTPセッション保存処理
logout APIのセッション破棄処理
FE fetchClient の credentials: "include"
セッション方式を前提にしたテスト
README/docsの説明
```

### 7. DBユーザーとroot利用を見直す

ローカルでは `root` で接続しています。
学習段階では動作確認しやすいですが、本番では避けます。

本番では、typingGame用のDBユーザーを作ります。

例:

```sql
CREATE USER 'typing_game_app'@'%' IDENTIFIED BY 'password';
GRANT SELECT, INSERT, UPDATE, DELETE ON typing_game.* TO 'typing_game_app'@'%';
```

Flywayを本番でアプリ起動時に流すか、別手順で流すかも検討します。
最初の学習ではアプリ起動時Flywayで良いですが、将来的にはマイグレーション実行手順を分けることもあります。

## Phase9でまだやらないこと

以下はPhase10以降で良いです。

- EC2インスタンス作成
- Nginx設定
- HTTPS証明書設定
- 独自ドメイン設定
- RDS化
- GitHub ActionsからEC2へ自動デプロイ
- refresh token導入
- OpenAPI Generator導入

## 推奨する実装順

Phase9で実装に進む場合は、以下の順番がきれいです。

1. `application-prod.yml` の方針を決める。
2. `app.cors.allowed-origins` を環境変数から受け取れる形にする。
3. `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` を環境変数化する。
4. `JWT_SECRET` のローカルデフォルトを本番では使わないことをREADMEに明記する。
5. Swagger UIを本番で有効にするか決める。
6. ローカルの `./mvnw test` を維持する。
7. EC2学習に入る前に、起動手順と確認手順をREADMEへ追加する。

## 完了条件

Phase9の完了条件は以下です。

- ローカル設定と本番想定設定の違いがdocsにまとまっている。
- 本番で環境変数化する値が明確になっている。
- JWT Bearer認証を主方式にする判断がdocsに残っている。
- セッションCookie方式を残す理由と削除タイミングがdocsに残っている。
- FE側のGitHub Pages単体公開方針と矛盾していない。
- EC2学習へ進む前のチェックリストがある。
