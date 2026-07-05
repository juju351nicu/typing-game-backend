# typing-game-backend

typingGame のバックエンドAPIです。

現在はスコア保存・スコア一覧取得、ユーザー登録、ログイン、ログインユーザー別スコア保存を実装しています。フロントエンド側は localStorage fallback を残したまま、ログイン済みユーザーだけAPI保存を追加していく方針です。

## 技術構成

- Java 21
- Spring Boot 3.5.16
- Maven
- Spring Web
- Spring Data JPA
- Spring Security
- springdoc-openapi / Swagger UI
- Bean Validation
- Flyway
- MySQL
- Lombok
- JUnit 5
- MockMvc
- H2 Database（テスト用）

## 起動前の準備

ローカルMySQLを起動します。

```bash
mysql.server start
```

データベースを作成します。

```bash
mysql -u root < src/main/resources/create-database.sql
```

テーブル作成と変更は Flyway migration で管理します。

```text
src/main/resources/db/migration
```

初期テーブル定義は `V1__create_initial_schema.sql`、サンプルスコア投入は `V2__insert_sample_scores.sql` に置いています。

既存のローカルDBに `flyway_schema_history` がない場合は、`baseline-on-migrate` により現在のDBを基準化してからmigrationを実行します。
手元DBのテーブル定義が古く、JPA validate でカラム不足などが出る場合は、学習用DBを作り直すか、足りないカラムを手動で追加してください。

## 起動

```bash
./mvnw spring-boot:run
```

デフォルトでは以下で起動します。

```text
http://localhost:8091
```

## Swagger UI

API仕様は Swagger UI で確認できます。

```text
http://localhost:8091/swagger-ui.html
```

OpenAPI JSON は以下で確認できます。

```text
http://localhost:8091/v3/api-docs
```

Spring Securityを使っているため、Swagger関連のパスは `SecurityConfig` で認証不要にしています。

## テスト

```bash
./mvnw test
```

テストではローカルMySQLではなく、H2 Database を使います。
テストDBのテーブルも Flyway migration から作成し、JPA はEntityとDB定義の差分を validate します。

## 現在実装済みのAPI

### スコア一覧取得

```http
GET /api/scores
```

保存済みスコアを一覧で取得します。

レスポンス例:

```json
[
  {
    "id": 1,
    "time": "00:00:28.00",
    "score": 12,
    "mode": 2,
    "gameRule": "timeAttack",
    "timeLimitSeconds": 60,
    "wpm": 32,
    "accuracy": 96,
    "missCount": 2,
    "correctCharacterCount": 80,
    "date": "2026-07-05 01:49:22"
  }
]
```

### スコア保存

```http
POST /api/scores
Content-Type: application/json
```

ゲーム終了時のスコアを保存します。

リクエスト例:

```json
{
  "time": "00:00:28.00",
  "score": 12,
  "mode": 2,
  "gameRule": "timeAttack",
  "timeLimitSeconds": 60,
  "wpm": 32,
  "accuracy": 96,
  "missCount": 2,
  "correctCharacterCount": 80
}
```

### ユーザー登録

```http
POST /api/users
```

新規ユーザーを登録します。
パスワードは平文では保存せず、Spring Security の `BCryptPasswordEncoder` で暗号化して保存します。

リクエスト例:

```json
{
  "loginEmail": "user@example.com",
  "password": "password"
}
```

### ログイン

```http
POST /api/auth/login
```

登録済みユーザーのログインを行います。
ログインに成功すると、Spring Security の認証情報をHTTPセッションに保存します。
フロントエンドから呼び出す場合は、Cookieを送受信できるように `credentials: "include"` を付ける想定です。

リクエスト例:

```json
{
  "loginEmail": "user@example.com",
  "password": "password"
}
```

### ログアウト

```http
POST /api/auth/logout
```

ログイン状態を終了します。
Spring Security の logout 機能でHTTPセッションを破棄します。

### ログイン中ユーザー取得

```http
GET /api/auth/me
```

ログイン中のユーザー情報を取得します。
未ログインの場合は `401 Unauthorized` になります。

### ユーザー別スコア保存

```http
POST /api/me/scores
```

ログイン中ユーザーに紐づくスコアを保存します。
未ログインの場合は `401 Unauthorized` になります。

リクエスト内容は `POST /api/scores` と同じです。

### ユーザー別スコア一覧取得

```http
GET /api/me/scores
```

ログイン中ユーザーに紐づくスコア一覧を取得します。
未ログインの場合は `401 Unauthorized` になります。

## 追加予定のAPI

### ランキング取得

```http
GET /api/rankings
```

全ユーザーまたは条件別のランキングを取得します。

想定クエリ:

```text
mode=2
gameRule=timeAttack
limit=20
```

## フロントエンド連携方針

- 未ログインユーザーは、これまで通り localStorage にスコアを保存する。
- 登録済みユーザーは、`POST /api/me/scores` でバックエンドAPIにスコアを保存する。
- API接続に失敗した場合でも、フロントエンド側の localStorage fallback は残す。
- まずは既存のスコア保存体験を壊さず、DB保存を追加する。
- ログインAPIはセッションCookie方式で開始するため、FE側のAPI呼び出しでは `credentials: "include"` を使う。
- ログイン画面は todo-frontend の `Login.vue` を参考にする。ただし typingGame はセッションCookie方式のため、token/localStorage保存の実装はそのまま流用しない。
- APIエラーは `fieldErrors` 形式で返し、FE側は同じ形式でエラー表示する。

## 今後の実装順

1. FEでログイン済みユーザーのスコア一覧を `GET /api/me/scores` から取得するか検討する。
2. ランキングAPIを追加する。
3. 必要になった段階で `Service` / `ServiceImpl` 化を検討する。

## 実装方針

詳しいコーディング方針は以下を参照してください。

- [docs/coding-guidelines.md](docs/coding-guidelines.md)
