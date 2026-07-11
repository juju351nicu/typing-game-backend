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

ローカルでバックエンドを起動する場合は、先にMySQLを起動してDBを用意します。

MySQLを起動します。

```bash
mysql.server start
```

`typing_game` データベースが未作成の場合は、以下を実行します。

```bash
mysql -u root < src/main/resources/create-database.sql
```

テーブル作成と変更は Flyway migration で管理します。

```text
src/main/resources/db/migration
```

初期テーブル定義は `V1__create_initial_schema.sql`、サンプルスコア投入は `V2__insert_sample_scores.sql` に置いています。
既存ローカルDB向けの補正migrationは、MySQL専用として `db/vendor/mysql` に置いています。

既存のローカルDBに `flyway_schema_history` がない場合は、`baseline-on-migrate` により現在のDBを基準化してからmigrationを実行します。
手元DBのテーブル定義が古く、JPA validate でカラム不足などが出る場合は、学習用DBを作り直すか、足りないカラムを手動で追加してください。

## ローカル起動手順

基本的には以下の順番で起動確認します。

1. MySQLを起動する。
2. 初回だけDBを作成する。
3. Spring Bootを起動する。
4. Swagger UIを開く。
5. 必要に応じてcurlでAPI疎通確認する。

MySQLを起動します。

```bash
mysql.server start
```

初回だけDBを作成します。

```bash
mysql -u root < src/main/resources/create-database.sql
```

Spring Bootを起動します。

```bash
./mvnw spring-boot:run
```

デフォルトでは以下で起動します。

```text
http://localhost:8091
```

API疎通確認は以下で行えます。

```bash
curl http://localhost:8091/api/scores
```

MySQLを停止したい場合は以下を実行します。

```bash
mysql.server stop
```

## Swagger UI

API仕様は Swagger UI で確認できます。

```text
http://localhost:8091/swagger-ui/index.html
```

環境によっては以下でも開けます。

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
  "password": "password123"
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
  "password": "password123"
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

### ランキング取得

```http
GET /api/rankings
```

全ユーザーまたは条件別のランキングを取得します。
未ログインでも取得できます。
並び順は、スコア降順、クリアタイム昇順、作成日時降順です。

想定クエリ:

```text
mode=2
gameRule=timeAttack
timeLimitSeconds=60
limit=20
```

クエリパラメータ:

- `mode`: 難易度。`0` Easy、`1` Normal、`2` Hard
- `gameRule`: ゲームルール。`normal` または `timeAttack`
- `timeLimitSeconds`: タイムアタック時の制限時間（秒）
- `limit`: 取得件数。未指定時は `20`

## フロントエンド連携方針

- 未ログインユーザーは、これまで通り localStorage にスコアを保存する。
- 登録済みユーザーは、`POST /api/me/scores` でバックエンドAPIにスコアを保存する。
- API接続に失敗した場合でも、フロントエンド側の localStorage fallback は残す。
- まずは既存のスコア保存体験を壊さず、DB保存を追加する。
- ログインAPIはセッションCookie方式で開始するため、FE側のAPI呼び出しでは `credentials: "include"` を使う。
- ログイン画面は todo-frontend の `Login.vue` を参考にする。ただし typingGame はセッションCookie方式のため、token/localStorage保存の実装はそのまま流用しない。
- APIエラーは `fieldErrors` 形式で返し、FE側は同じ形式でエラー表示する。

## 今後の実装順

### Phase 7: FE/BE結合確認

目安は半日〜1日です。

- バックエンドAPI有効時に、ユーザー登録、ログイン、スコア保存、ランキング表示を確認する。
- バックエンドAPI無効時に、フロントエンド単体でlocalStorage保存が動くことを確認する。
- API保存・取得に失敗しても、フロントエンド側のlocalStorage fallbackを維持する。
- 確認結果をREADMEまたはdocsに追記する。

Status: 完了。`npm run dev:api` で `http://localhost:8081` に固定してAPI有効確認し、`npm run dev` でFE単体モード確認を行いました。

確認済み:

- ユーザー登録、ログイン、ログイン中ユーザー取得ができる。
- ログイン状態でゲーム結果を保存できる。
- ユーザー別スコア一覧と全体ランキングを表示できる。
- FE単体モードではログイン導線を表示せず、localStorage保存でゲームを継続できる。
- API有効確認はCORS許可済みの `http://localhost:8081` で行う。

### Phase 8: JWT化

目安は3日〜1週間です。

詳細計画:

```text
docs/jwt-migration-plan.md
```

- ログイン成功時にJWTを発行する。
- フロントエンドから `Authorization` ヘッダーでログインユーザー向けAPIを呼び出す。
- HTTPセッション依存を減らし、GitHub PagesのFEと別ホストのBEを接続しやすくする。
- ログアウト、認証切れ、未ログイン時のエラー形式を整理する。

初期設計メモ:

- 現在のセッションCookie方式はローカル学習用として残し、JWT化は別フェーズとして扱う。
- JWT化後はログイン成功時にaccess tokenを返す。
- FEは `Authorization: Bearer {token}` でログインユーザー向けAPIを呼び出す。
- token保存場所、期限切れ、ログアウト時の破棄、refresh tokenを使うかは実装前に決める。
- EC2公開やGitHub Pages連携の前に、ローカルでJWT認証の正常系、期限切れ、未ログインエラーを確認する。

### Phase 9: 本番公開準備

目安は2日〜1週間です。

- 本番用CORS設定を整理する。
- `application-prod` と環境変数でDB接続情報を切り替える。
- GitHub PagesからバックエンドAPIへ接続する前提で設定を整理する。
- 起動手順、環境変数、確認手順をREADME/docsにまとめる。

### Phase 10: EC2デプロイ学習

目安は1〜2週間です。

- EC2にJava、MySQL、Nginxを準備する。
- Spring Bootアプリを起動する。
- `systemd` でサービス化する。
- セキュリティグループ、ポート、HTTPSを確認する。
- GitHub PagesのFEからEC2上のBEへ疎通確認する。

### 後続フェーズの考え方

現時点では、OpenAPI Generatorは導入しない方針です。
typingGameの規模では、手書きのTypeScript型とSwagger UIで十分に管理できます。
API数が増え、FE/BEの型同期コストが大きくなった段階で再検討します。

期間の目安は、最短で1〜2週間、現実的には3〜4週間、学習を丁寧に進める場合は1〜2か月です。
まずはFE/BE結合確認を終わらせ、その後にJWT化、本番公開準備、EC2学習の順で進めます。

## 実装方針

詳しいコーディング方針は以下を参照してください。

- [docs/coding-guidelines.md](docs/coding-guidelines.md)
