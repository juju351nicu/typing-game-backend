# Phase10 EC2デプロイ前チェックリスト

## 目的

Phase10では、typing-game-backendをEC2上で動かし、GitHub Pagesで公開済みのtypingGameからバックエンドAPIへ接続できる状態を学習します。

最初のゴールは「本番運用を完成させること」ではなく、以下を小さく確認することです。

- EC2上でSpring Bootアプリを起動できる。
- `prod` profileを環境変数で起動できる。
- Nginx経由でAPIへアクセスできる。
- HTTPS化の手順を理解できる。
- GitHub PagesのFEからJWT Bearer認証で疎通確認できる。

## Phase9完了状態

2026-07-18時点で、Phase10へ進む前の準備は以下まで完了しています。

- `application-prod.yml` を追加済み。
- DB接続情報は `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` で外出しする方針。
- JWT設定は `JWT_SECRET` / `JWT_EXPIRES_IN_SECONDS` / `JWT_ISSUER` で外出しする方針。
- `prod` profileでは `JWT_SECRET` にデフォルト値を置かない。
- CORS許可Originは `APP_CORS_ALLOWED_ORIGINS` で外出しする方針。
- `prod` profileではSwagger UI / OpenAPI JSONをデフォルト無効にする。
- JWT Bearer認証を主方式にする。
- セッションCookie方式は移行期間とローカル学習用として残す。
- GitHub Pages公開版のFEはAPI無効モードで公開済み。
- GitHub Pages公開URLで、ゲームプレイ、localStorage保存、ランキング表示を確認済み。

## Phase10で最初に決めること

EC2を触る前に、以下を決めます。

```text
1. DBはEC2内MySQLから始めるか、RDSを使うか
2. ドメインなしでEC2のPublic DNS / Elastic IPから始めるか
3. HTTPS化をどの段階で入れるか
4. Swagger UIを一時的に有効にするか
5. デプロイは手動から始めるか、GitHub Actions連携まで進めるか
```

学習の順番としては、最初は以下が扱いやすいです。

```text
EC2 1台
Spring Boot
MySQL
Nginx
手動デプロイ
あとからHTTPS
あとからRDS / 独自ドメイン / 自動デプロイ
```

## AWS側の事前確認

- AWSアカウントへログインできる。
- 利用リージョンを決める。
- EC2インスタンスタイプを決める。
- キーペアを作成または確認する。
- セキュリティグループを用意する。
- 必要ならElastic IPを使うか決める。

最初のセキュリティグループ例:

```text
SSH 22
-> 自分のIPだけ許可

HTTP 80
-> 0.0.0.0/0

HTTPS 443
-> 0.0.0.0/0

Spring Boot 8091
-> 原則として外部公開しない
-> Nginxからのみ到達させる構成を目指す
```

## EC2内で準備するもの

- Java 21
- Git
- MySQL
- Nginx
- unzip / curl など最低限の確認コマンド

Spring Bootは最初はjarを手動配置して起動確認します。
Docker化やGitHub Actionsからの自動デプロイは、最初の疎通確認が終わってからで良いです。

## DB準備チェック

ローカル開発では `root` 接続でも良いですが、EC2上ではアプリ用ユーザーを作ります。

例:

```sql
CREATE DATABASE typing_game CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'typing_game_app'@'localhost' IDENTIFIED BY '強いパスワード';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP ON typing_game.* TO 'typing_game_app'@'localhost';
FLUSH PRIVILEGES;
```

Flywayをアプリ起動時に流す場合、初回だけ `CREATE` / `ALTER` などの権限が必要です。
将来的にマイグレーション実行手順を分ける場合は、アプリ用ユーザーの権限をさらに絞ります。

## 環境変数チェック

EC2上では、以下を環境変数または環境変数ファイルで設定します。

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://localhost:3306/typing_game?serverTimezone=Asia/Tokyo&useUnicode=true&characterEncoding=UTF-8
DB_USERNAME=typing_game_app
DB_PASSWORD=...
JWT_SECRET=...
JWT_EXPIRES_IN_SECONDS=3600
JWT_ISSUER=typing-game-backend
APP_CORS_ALLOWED_ORIGINS=https://juju351nicu.github.io
SERVER_ADDRESS=127.0.0.1
SERVER_PORT=8091
SPRINGDOC_ENABLED=false
```

注意:

- `JWT_SECRET` はGitHubに載せない。
- `DB_PASSWORD` はGitHubに載せない。
- `APP_CORS_ALLOWED_ORIGINS` はパスなしのOriginにする。
- GitHub PagesのOriginは `https://juju351nicu.github.io`。
- `SERVER_ADDRESS=127.0.0.1` にして、NginxからだけSpring Bootへ流す構成を優先する。

## jar作成と起動確認

ローカルまたはEC2上でjarを作ります。

```bash
./mvnw clean test
./mvnw clean package
```

EC2上で起動確認します。

```bash
java -jar target/typing-game-backend-*.jar
```

確認すること:

- アプリが起動する。
- Flyway migrationが成功する。
- DB接続エラーが出ない。
- `JWT_SECRET` 未設定時にprod起動が失敗する。
- `SPRINGDOC_ENABLED=false` でSwaggerが出ない。

## systemd化チェック

手動起動確認後、`systemd` でサービス化します。

見る観点:

- service userをrootにしない。
- jarの配置先を決める。
- 環境変数ファイルをGit管理外に置く。
- `Restart=always` または `on-failure` を検討する。
- `journalctl` でログを見られる。

確認コマンド例:

```bash
sudo systemctl status typing-game-backend
sudo journalctl -u typing-game-backend -f
```

## Nginxチェック

Nginxでは外部のHTTP/HTTPSを受け、Spring Bootへリバースプロキシします。

例:

```text
client
-> https://api.example.com
-> Nginx :443
-> http://127.0.0.1:8091
-> Spring Boot
```

確認すること:

- `/api/rankings` がNginx経由で返る。
- OPTIONS preflightが通る。
- `Authorization` ヘッダーがSpring Bootへ届く。
- Spring Bootの8091を外部へ直接公開していない。

## HTTPSチェック

GitHub PagesはHTTPSです。
ブラウザからHTTPSページがHTTP APIへアクセスすると、mixed contentで失敗します。

そのため、GitHub PagesからEC2のAPIへ接続する段階では、バックエンドAPIもHTTPSにします。

最初に確認すること:

- 独自ドメインを使うか。
- Let's Encryptを使うか。
- 証明書の自動更新をどうするか。
- HTTPS化後にCORS Originが正しいか。

## FE接続チェック

バックエンドAPI公開後、FE側は以下のようにAPI有効モードでビルドします。

```text
VITE_ENABLE_BACKEND_API=true
VITE_API_BASE_URL=https://api.example.com
```

確認すること:

- GitHub Pagesの公開URLでログイン導線が出る。
- ユーザー登録できる。
- ログインできる。
- access tokenが `sessionStorage` に保存される。
- `Authorization: Bearer ...` で `/api/me/scores` を呼べる。
- 401時に再ログイン案内が出る。
- API失敗時もlocalStorageのプレイ結果が残る。

## セキュリティ確認

- EC2のSSHは自分のIPだけ許可する。
- MySQLを外部公開しない。
- Spring Bootの8091を外部公開しない。
- `JWT_SECRET` をGitHubに載せない。
- `DB_PASSWORD` をGitHubに載せない。
- 本番用DBユーザーにrootを使わない。
- Swagger UIを公開しっぱなしにしない。
- 不要なSecurity Groupルールを残さない。
- 必要になった段階でログローテーションを検討する。

## Phase10ではまだ後回しで良いこと

- RDS化
- 独自ドメイン
- GitHub ActionsからEC2への自動デプロイ
- Docker化
- Blue/Green deployment
- refresh token
- OpenAPI Generator
- 本格的な監視

## 完了条件

Phase10の最初の完了条件は以下です。

- EC2上でSpring Bootが `prod` profileで起動する。
- Nginx経由で公開APIへアクセスできる。
- HTTPSでAPIへアクセスできる。
- GitHub PagesのFEからバックエンドAPIへ接続できる。
- JWT Bearer認証でログインユーザー向けAPIを呼べる。
- localStorage fallbackが壊れていない。
- 起動手順、停止手順、ログ確認手順がdocsに残っている。
