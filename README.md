# typing-game-backend

[Balloon Typing Game](https://github.com/juju351nicu/typingGame) のバックエンドAPIです。

Java 25 / Spring Bootで実装し、Spring Security OAuth2 Resource ServerによるJWT Bearer認証、MySQL / Flywayでのスキーマ管理、JUnit 5 / MockMvcでのテストを行っています。
AWS EC2上でNginxとsystemdを使って公開し、GitHub Pagesのフロントエンドから接続しています。

## リンク

- [Live Demo](https://juju351nicu.github.io/typingGame/)
- [Frontend Repository](https://github.com/juju351nicu/typingGame)
- [GitHub PagesのVueからAWS EC2上のSpring Boot APIへHTTPS接続するまで](https://juju351nicu.github.io/typingGame/guide/ec2-spring-boot-https-frontend-connection)
- [Spring Security Resource ServerでJWT Bearer認証を実装する](https://juju351nicu.github.io/typingGame/guide/spring-security-jwt-resource-server)

## 技術スタック

| 分類 | 技術 |
| --- | --- |
| 言語 / ビルド | Java 25, Maven |
| フレームワーク | Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA, Bean Validation, Lombok |
| 認証 | Spring Security, OAuth2 Resource Server (JWT), JOSE |
| データベース | MySQL 8.4 (Docker Compose), Flyway |
| API仕様 | springdoc-openapi / Swagger UI |
| テスト | JUnit 5, MockMvc, H2 Database |
| インフラ | AWS EC2 (Ubuntu 24.04), Nginx, systemd, Let's Encrypt |

## アーキテクチャ

```text
        GitHub Pages
   Vue 3 / TypeScript / Pinia
             │
             │  HTTPS + JWT Bearer
             ▼
      AWS EC2 (Ubuntu 24.04)
             │
        Nginx (リバースプロキシ / Let's Encrypt)
             │
        Spring Boot (systemd)
             │
        MySQL 8.4 (Docker Compose)
```

Spring Bootは `127.0.0.1` にbindし、外部からのアクセスはNginx経由のみに限定しています。

## 主な機能

- ユーザー登録（パスワードはBCryptでハッシュ化して保存）
- ログイン時のJWTアクセストークン発行と、`Authorization: Bearer {token}` による認証
- ログアウト時のトークン世代更新による、発行済みJWTの失効
- ログインユーザーに紐づくスコアの保存・取得
- 難易度、ゲームルール、制限時間で絞り込むランキング取得（認証不要）
- validationエラー・認証エラーを共通の `fieldErrors` 形式で返すエラーレスポンス
- CORS許可Origin、DB接続情報、JWT secretの環境変数切り替え

## ローカル開発

Rancher DesktopなどでDocker CLI / Docker Composeを使える状態にしてから起動します。

1. MySQLを起動する。

   ```bash
   docker compose up -d mysql
   ```

2. Spring Bootを起動する。

   ```bash
   ./mvnw spring-boot:run
   ```

   `http://localhost:8091` で起動します。

3. Swagger UIでAPIを確認する。

   ```text
   http://localhost:8091/swagger-ui/index.html
   ```

4. 停止する。

   ```bash
   docker compose down
   ```

   DBデータも含めて作り直す場合だけ、volumeも削除します。

   ```bash
   docker compose down -v
   ```

他PCでもMySQLのバージョンと設定をそろえるため、MySQLだけDocker Composeで固定しています。
Spring Bootアプリ本体はDocker化せず、ローカルのJava / Maven起動からDocker上のMySQLへ接続します。

ローカルにインストールしたMySQLを使う場合は、`src/main/resources/create-database.sql` でDBを作成し、`DB_USERNAME` / `DB_PASSWORD` を指定して起動します。
Docker MySQLとローカルMySQLはどちらも `3306` を使うため、同時起動はできません。

## データベースとFlyway

テーブル定義の作成・変更はFlyway migrationで管理し、JPAは `ddl-auto: validate` でEntityとの差分を検証するだけにしています。

```text
src/main/resources/db/migration        共通migration
src/main/resources/db/vendor/mysql     MySQL専用migration
```

- `V1__create_initial_schema.sql`: 初期テーブル定義
- `V2__insert_sample_scores.sql`: サンプルスコア投入
- `V4__add_token_version_to_users.sql`: JWT失効用のトークン世代カラム追加

既存ローカルDBの補正migrationはMySQL専用のため `db/vendor/mysql` に分離し、H2を使うテストでは実行されないようにしています。
`flyway_schema_history` がないDBは、`baseline-on-migrate` で現在の状態を基準化してからmigrationを実行します。

## 本番設定

本番用の設定は `application-prod.yml` に分離し、`SPRING_PROFILES_ACTIVE=prod` を指定した場合だけ有効になります。
接続情報やsecretはリポジトリに置かず、すべて環境変数から読み込みます。

| 環境変数 | 用途 | 必須 |
| --- | --- | --- |
| `DB_URL` | MySQL接続URL | 必須 |
| `DB_USERNAME` | DBユーザー | 必須 |
| `DB_PASSWORD` | DBパスワード | 必須 |
| `JWT_SECRET` | JWT署名用secret（32文字以上） | 必須 |
| `APP_CORS_ALLOWED_ORIGINS` | CORS許可Origin | 必須 |
| `JWT_EXPIRES_IN_SECONDS` | JWT有効期限（秒。既定値 `3600`） | 任意 |
| `JWT_ISSUER` | JWT issuer | 任意 |
| `SERVER_ADDRESS` | bind address（既定値 `127.0.0.1`） | 任意 |
| `SERVER_PORT` | 起動ポート（既定値 `8091`） | 任意 |
| `SPRINGDOC_ENABLED` | Swagger UI / OpenAPI JSONの公開可否（既定値 `false`） | 任意 |

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=...
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=...
APP_CORS_ALLOWED_ORIGINS=...
```

EC2上での配置、systemdのユニット定義、Nginxの設定などの運用手順は、公開リポジトリ外で管理しています。

## API

| Method | Endpoint | 認証 | 用途 |
| --- | --- | --- | --- |
| POST | `/api/users` | 不要 | ユーザー登録 |
| POST | `/api/auth/login` | 不要 | ログイン・JWT発行 |
| POST | `/api/auth/logout` | 必要 | 発行済みJWTの失効 |
| GET | `/api/auth/me` | 必要 | ログイン中ユーザー取得 |
| POST | `/api/me/scores` | 必要 | ログインユーザーのスコア保存 |
| GET | `/api/me/scores` | 必要 | ログインユーザーのスコア一覧取得 |
| GET | `/api/rankings` | 不要 | ランキング取得 |

`GET /api/rankings` は `mode`、`gameRule`、`timeLimitSeconds`、`limit` で絞り込め、スコア降順、クリアタイム昇順、作成日時降順で返します。

リクエスト・レスポンスの詳細はSwagger UIを参照してください。

## Swagger UI

ローカル起動中は以下でAPI仕様を確認できます。

```text
http://localhost:8091/swagger-ui/index.html
http://localhost:8091/v3/api-docs
```

認証が必要なAPIを試す場合は、`POST /api/auth/login` のレスポンスの `accessToken` をコピーし、画面右上の `Authorize` から `bearerAuth` へ貼り付けます。
`Bearer ` はSwagger UI側が付けるため、token文字列だけを入力します。

Swagger関連のパスは `SecurityConfig` で認証不要にしていますが、prod profileでは既定で無効です。

## テスト

```bash
./mvnw test
```

MockMvcによるControllerテストを中心に、8ファイル / 33テストで、ユーザー登録、ログイン、JWT検証、認証エラー時のレスポンス形式、スコア保存・取得、ランキングの絞り込みを検証しています。
テストDBはMySQLではなくH2を使い、テーブルは本番と同じFlyway migrationから作成したうえで、JPAがEntityとの差分をvalidateします。

## フロントエンド連携

フロントエンドはプレイ結果を常にlocalStorageへ保存し、バックエンドAPI有効かつログイン済みの場合だけ `POST /api/me/scores` にも保存します。
API通信に失敗してもlocalStorage側の結果と画面表示は維持され、API無効時はフロントエンド単体で動作します。

認証はJWT Bearer方式のみで、Cookieは送受信しません。
フロントエンドはログイン時の `accessToken` を `sessionStorage` に保持し、`Authorization` ヘッダーで送信します。

## 実装上の工夫

- スコアの保存・取得は `/api/me/scores` に一本化し、「認証中ユーザー自身のリソース」であることをURLから読み取れるようにしています。全ユーザーを対象とした表示は `/api/rankings` へ分け、エンドポイントごとの責務を1つにそろえています。
- 認証をSpring Security OAuth2 Resource Serverへ寄せ、独自の認証フィルターを実装せずに署名・有効期限・issuerを検証しています。
- JWTのsubjectには内部ユーザーIDだけを入れ、メールアドレスなどの個人情報をtokenに含めないようにしています。
- ログアウト時はユーザーごとの `tokenVersion` を進め、JWTのclaimと突き合わせて発行済みtokenを失効させています。ステートレスなまま、ログアウト後のtokenを使えなくするための実装です。
- HTTPセッションを作らない `STATELESS` 設定にし、Cookieを使わないためCORSの `allowCredentials` も無効にしています。
- JWT secretが32文字未満の場合は起動時に例外にして、ローカルでも署名鍵の最低長をそろえています。
- validationエラー、リクエスト形式エラー、認証エラーをすべて `fieldErrors` 形式へそろえ、フロントエンド側のエラー表示を共通化しています。
- MySQL固有の補正migrationを `db/vendor/{vendor}` へ分離し、H2を使うテストに影響させずに本番と同じmigrationでテーブルを作成しています。
- prod profileでは `server.address` を `127.0.0.1` に固定し、Swagger UIも既定で無効にして、公開経路をNginx経由のAPIだけに絞っています。
- MySQLコンテナのポート公開を `127.0.0.1:3306` に限定しています。DockerのポートフォワードはUFWより先に処理されるため、ファイアウォールではなくbind先で外部公開を防いでいます。

## 今後の改善

- Spring Bootアプリ本体のコンテナ化
- 短命access token + refresh tokenの要否検討

## 実装方針

詳細なコーディング規約と運用資料は、公開リポジトリ外で管理しています。
