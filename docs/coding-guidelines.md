# Coding Guidelines

このドキュメントは、typing-game-backend の実装で守る基本方針をまとめたものです。

後から Claude Code や Codex が作業を引き継ぐ場合も、この方針に合わせて実装してください。

## Java

- DTO は現場の書き方に合わせて、原則として `class` + private field + getter/setter で定義する。
- DTO の JSON 項目名は、必要に応じて `@JsonProperty` で明示する。
- Entity と request / response DTO は分ける。
- Controller はリクエスト受け取りとレスポンス返却を主な責務にする。
- Service は業務処理と Entity / DTO の変換を主な責務にする。
- Repository はDBアクセスの入口に絞る。

## Package Naming

### dto と model の使い分け

このプロジェクトでは、APIのリクエスト / レスポンス専用クラスを `jp.clip.typinggame.dto` に置きます。

```java
package jp.clip.typinggame.dto;
```

`dto` は Data Transfer Object の略で、主にControllerの入口・出口で使うデータの形を表します。

例:

```text
LoginRequest
RegisterUserRequest
SaveScoreRequest
ScoreResponse
UserResponse
```

これらはAPI通信のための入れ物であり、DBテーブルそのものや業務概念そのものではないため、`dto` に置きます。

`model` は現場によって意味が広く、以下のような使われ方があります。

```text
model = 画面 / API / 業務 / DB など、何らかのデータ構造全般
```

現場によっては、request / response / entity / domain をまとめて `model` 配下に置くこともあります。
そのため、既存プロジェクトで `model` パッケージを見かけた場合は、その現場で `model` が何を指しているかを先に確認してください。

このプロジェクトでの判断基準:

- APIのリクエスト / レスポンス専用クラスは `dto` に置く。
- DBテーブルに対応するクラスは `entity` に置く。
- 業務ルールを持つ中心概念が必要になった場合は、`domain` または `model` の導入を検討する。
- 現時点では `model` パッケージは作らず、`dto` / `entity` / `repository` / `service` / `controller` の分け方を維持する。

## Lombok

- DTO / Entity では、現場の書き方に合わせて Lombok の `@Getter` / `@Setter` を使ってよい。
- `@Getter` / `@Setter` を使う場合も、フィールドの意味が分かるようにJavadocを書く。
- `@Builder` は原則として使わない。
- 単純なDTOやEntity生成では、`new Xxx()` と setter を使う。
- コンストラクタ注入が必要な Service / Controller では、`@RequiredArgsConstructor` を使ってよい。

例:

```java
/**
 * スコア保存APIのリクエストDTOです。
 */
@Getter
@Setter
public class SaveScoreRequest {

    /** ゲームスコアです。 */
    @JsonProperty("score")
    private Integer score;
}
```

## Dependencies

### Spring Security

このプロジェクトでは、登録ユーザーのログイン機能に Spring Security を使います。

- パスワードは平文保存しない。
- パスワード保存時は `BCryptPasswordEncoder` で暗号化する。
- 認証ユーザーの取得は `UserDetailsService` を通す。
- まずはセッションCookie方式で開始し、JWTは必要になった段階で検討する。
- FEからログイン後APIを呼ぶ場合は、Cookie送受信のため `credentials: "include"` を付ける。
- todo-backend のSecurity設定を参考にするが、`NoOpPasswordEncoder` は使わない。

### Swagger / OpenAPI

このプロジェクトでは、API仕様確認と学習のため Swagger UI を使います。

- Spring Boot 3系では `springdoc-openapi-starter-webmvc-ui` を使う。
- Swagger UI は `/swagger-ui.html` で確認する。
- OpenAPI JSON は `/v3/api-docs` で確認する。
- Spring Securityを使う場合、Swagger関連パスは `SecurityConfig` で認証不要にする。
- Request DTO の各フィールドには、必要に応じて `@Schema(description = "...", example = "...")` を付ける。
- Swagger UI のRequest bodyでそのまま試せるように、`example` は正常系かつvalidationを通る値にする。
- Javadocはコードを読む人向け、`@Schema` はSwagger UIを見る人向けとして使い分ける。

### 例外レスポンス

APIエラーはFEで扱いやすいように、共通して `fieldErrors` 形式で返します。

```json
{
  "fieldErrors": [
    {
      "errorCode": "NotEmpty",
      "field": "loginEmail",
      "message": "メールアドレスを入力してください。"
    }
  ]
}
```

- validationエラーは `ApiExceptionHandler` で変換する。
- ログイン失敗、重複登録、未ログインなども `fieldErrors` 形式に寄せる。
- Ghost-PDF5 の例外レスポンスを参考にするが、`@Data` ではなく `@Getter` / `@Setter` を使う。
- `System.out.println` でエラー内容を出力しない。

## Service

- 現時点では `ScoreService`、`UserService`、`AuthService` のように実装クラスを直接使う。
- APIや業務処理が増えて、差し替えやテストダブルが必要になった段階で `Service` / `ServiceImpl` 化を検討する。
- `ServiceImpl` 化する場合は、一部だけでなく対象サービスの命名を揃える。

## Frontend / Ghost-PDF5 との対応

Ghost-PDF5 の静的JavaScriptは、typingGameのTypeScript実装へそのままコピーせず、責務ごとに対応するファイルへ考え方を寄せます。

- Ghost-PDF5 `const.js`
  - 定数を集約する考え方を参考にする。
  - typingGameでは `src/constants/const.ts` に置く。
- Ghost-PDF5 `rest.js`
  - fetchの共通処理、HTTPメソッド、JSON送受信の考え方を参考にする。
  - typingGameでは `src/utils/fetchClient.ts` に置く。
  - typingGameはセッションCookie認証のため、API呼び出しでは `credentials: "include"` を使う。
- Ghost-PDF5 `util.js`
  - 空判定、localStorage、文字列補助などの考え方を参考にする。
  - typingGameでは `src/utils/gameUtils.ts` など、用途が分かる名前へ分ける。
- Ghost-PDF5 `main.js`
  - 画面状態、エラー表示、REST呼び出しの流れは参考にする。
  - typingGameはVue 3 / Composition API / Pinia構成なので、Options APIの大きな1ファイル構成はそのまま移植しない。

新しく `rest.ts`、`util.ts`、`const.ts` を重複して増やす前に、既存の `fetchClient.ts`、`gameUtils.ts`、`constants/const.ts` に追加できるか確認してください。

### typingGame から Ghost-PDF5 へ流用する時の方針

将来的に Ghost-PDF5 を整理する場合は、Ghost側の `const.js`、`rest.js`、`util.js` をそのまま育てるより、typingGame側で整理した責務分けを基準にします。

- `fetchClient.ts`
  - fetch共通処理、JSON送受信、HTTPエラー、Cookie送受信などを扱う。
  - Ghost-PDF5へ移す場合も、まずは `rest.js` の責務をこの形へ寄せる。
- `gameUtils.ts`
  - 空判定、localStorage、ブラウザ判定など、画面やドメインから独立した補助処理を扱う。
  - Ghost-PDF5へ移す場合は、PDF固有処理と汎用Utilを混ぜすぎない。
- `const.ts`
  - URL、画面選択肢、定数値などを集約する。
  - Ghost-PDF5へ移す場合も、文字列リテラルやAPIパスを画面内に散らさない。

Ghost-PDF5 がJavaScriptのままの場合、TypeScriptの型そのものは流用できません。ただし、関数名、責務分離、エラーハンドリング、localStorageの扱いは流用できます。

todo / Ghost / typingGame を揃える時は、先に typingGame で小さく実装してから、うまくいった責務分けを他プロジェクトへ戻す方針にします。

## Database Migration

現場でFlywayを使っているため、このプロジェクトでもDB変更はFlywayで管理します。

現在の方針:

- Flyway導入後は、DB変更を `schema.sql` の直接編集ではなく `V1__create_initial_schema.sql`、`V2__add_xxx.sql` のようなmigrationで管理する。
- DB種類ごとに必要な補正SQLは `db/vendor/{vendor}` に置く。
- MySQLだけで必要な既存ローカルDB補正は `db/vendor/mysql` に置き、H2テストへ無理に適用しない。
- JPAの `ddl-auto` は `validate` にして、EntityとDB定義のズレを検知する。
- Spring Bootの `spring.sql.init.mode` は `never` にして、`schema.sql` / `data.sql` の自動実行とFlywayを混在させない。

導入時の想定:

```text
src/main/resources/db/migration/V1__create_initial_schema.sql
```

- 既存ローカルDBを使い続ける場合は、`baseline-on-migrate` を使うか検討する。
- 学習用でデータを消してよい場合は、開発DBを作り直して `V1__create_initial_schema.sql` から作成してもよい。
- テスト用H2もmigrationで作れるようにして、ローカルMySQLとテストDBの差を減らす。

既存ローカルDBへの注意:

- 既存DBに `flyway_schema_history` がない場合は、`baseline-on-migrate` により基準化する。
- 既存DBのテーブル定義が古い場合、Flywayの基準化だけでは不足カラムは追加されない。
- 学習用DBでデータを残す必要がない場合は、DBを作り直してmigrationで再作成する方が分かりやすい。
- データを残す必要がある場合は、不足カラムを手動で追加するか、専用migrationを作る。

## Score API の使い分け

- `POST /api/scores`
  - 公開スコア、ランキング、ゲストも含む全体向けの保存APIとして残す。
- `GET /api/scores`
  - 公開スコア一覧またはランキング前段の取得APIとして残す。
- `POST /api/me/scores`
  - ログインユーザー自身に紐づくスコア保存APIとして使う。
  - FEではログイン済みの場合だけこのAPIを呼び出す。
- `GET /api/me/scores`
  - ログインユーザー自身の保存済みスコア一覧APIとして使う。
- `GET /api/rankings`
  - 公開ランキング取得APIとして使う。
  - 未ログインでも取得できる。
  - クエリパラメータで `mode`、`gameRule`、`timeLimitSeconds`、`limit` を受け取る。
  - 並び順はスコア降順、クリアタイム昇順、作成日時降順にする。
  - 現時点では専用DTOを増やさず、既存の `ScoreResponse` を返す。

未ログインユーザーのプレイ結果は、引き続きフロントエンドのlocalStorageへ保存します。API保存に失敗した場合も、localStorage側の保存結果は消さない方針です。

## mode / gameRule の enum 化方針

`mode` と `gameRule` は、将来的には enum 化を検討します。
ただし、Phase6 のバックエンドAPI接続中は、既存のフロントエンド、DB、Swagger確認を安定させるため、すぐには変更しません。

現在の外向きAPI値:

```json
{
  "mode": 2,
  "gameRule": "timeAttack"
}
```

現時点の判断:

- `gameRule` は `normal` / `timeAttack` のように候補値が少ないため、enum 化しやすい。
- `mode` は `0` / `1` / `2` の数値でFE、API、DBがつながっているため、enum 化時の影響範囲が少し広い。
- Phase6 の一区切りまでは、APIレスポンスの形を変えない。
- enum 化する場合も、JSON値は既存互換を優先し、`timeAttack` や `2` のような外向きの値を安易に変えない。

将来の候補:

```java
public enum GameRule {
    NORMAL("normal"),
    TIME_ATTACK("timeAttack")
}
```

```java
public enum GameMode {
    EASY(0),
    NORMAL(1),
    HARD(2)
}
```

実装する場合は、以下を合わせて確認します。

- Jackson の `@JsonValue` / `@JsonCreator` で既存JSON値を維持できるか。
- JPA の `@Converter` または明示的な変換でDB値を維持できるか。
- `SaveScoreRequest`、`ScoreResponse`、`RankingController` のSwagger表示が分かりやすいか。
- 既存のFE型、localStorageデータ、API変換処理と矛盾しないか。
- 正常系、validationエラー、不正値のテストを追加できるか。

タイミングとしては、「自分の記録 / 全体ランキング」の切り替えとFEテストまで終わり、Phase6 の一通りの実装が一区切りしてから行います。

### Apache Commons

このプロジェクトでは、現場でよく使われる補助ライブラリとして以下を入れています。

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.17.0</version>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-collections4</artifactId>
    <version>4.5.0</version>
</dependency>
```

- `commons-lang3`
  - 文字列、数値、Object、日付などの汎用ユーティリティを使うためのライブラリ。
  - 例: `StringUtils.isBlank(...)`、`ObjectUtils.isEmpty(...)`
- `commons-collections4`
  - Collection / List / Map などの判定や操作を補助するライブラリ。
  - 例: `CollectionUtils.isEmpty(...)`、`MapUtils.isEmpty(...)`

使い分け:

- Java標準APIだけで簡潔に書ける場合は、標準APIを優先する。
- nullチェックや空判定が増えて読みづらくなる場合は、Apache Commonsを使ってよい。
- 使っていないimportや依存は増やさない。
- 新しい外部ライブラリを追加する前に、Java標準API、Spring、Apache Commonsで足りるか確認する。

## Naming

### DTO / Entity 変換

DTO と Entity の変換メソッドは、変換先が分かる名前にする。

```java
private Score toScoreEntity(SaveScoreRequest request)
private ScoreResponse toResponse(Score score)
```

- `toScoreEntity`
  - DTO から `Score` Entity へ変換することが主目的の場合に使う。
  - `to〇〇` は、入力値を別の表現へ変換する意味を持たせる。
- `toResponse`
  - Entity から API レスポンスDTOへ変換する場合に使う。
  - Controllerへ返す値であることが文脈から分かる場合は、`toScoreResponse` ではなく `toResponse` でもよい。

### build / create / new の使い分け

- `buildScore`
  - 複数の値、条件、補正処理を組み合わせて `Score` を組み立てる場合に使う。
  - 単純な DTO -> Entity 変換だけなら、`toScoreEntity` を優先する。
- `createScore`
  - 新規作成そのものに意味がある場合に使う。
  - 例: 初期値を入れる、作成イベントを伴う、採番や初期状態を明示する。
- `newScore`
  - 単純生成のニュアンスが強く、メソッド名としては意味が弱くなりやすい。
  - 原則として、より具体的な `to〇〇` / `build〇〇` / `create〇〇` を優先する。

## Javadoc

- public class、public method、主要な private method には Javadoc を付ける。
- Javadoc は日本語で書く。
- `@since` / `@author` は付けない。
- DTO の Javadoc には、そのDTOの用途と、request / response のどちらかを明記する。
- Entity のフィールドには、DB項目として何を表す値かを書く。
- `@Getter` / `@Setter` でアクセサを自動生成する場合も、フィールドJavadocは省略しない。
- 変換メソッドの Javadoc には、変換元と変換先を明記する。
- recordを使わない理由や現場の書き方に合わせた判断など、後続作業者が迷いやすい背景はJavadocかdocsに残す。

## Records

Java 16以降では、DTOを `record` で書くこともできます。

```java
public record SaveScoreRequest(String time, Integer score) {
}
```

recordの特徴:

- コンストラクタ、アクセサ、`equals`、`hashCode`、`toString` が自動生成される。
- アクセサ名は `getTime()` ではなく `time()` になる。
- setterは持たない。
- 値を受け渡すだけのDTOを短く書ける。

このプロジェクトでの方針:

- 現時点では、現場の書き方に合わせて通常の `class` DTOを使う。
- recordを試す場合は、既存DTOの書き方と混在させすぎない。
- recordへ変更する場合は、Service側の `getXxx()` 呼び出しも `xxx()` へ変わる点に注意する。

## Tests

- JUnit は JUnit 5（Jupiter）を使う。
- テストクラスとテストメソッドには、必要に応じて `@DisplayName` を付ける。
- `@DisplayName` は、テストレポート上で仕様が分かる日本語名にする。
- Controller のAPI仕様は `MockMvc` で確認する。
- 保存APIは正常系と validation エラーを確認する。
- DB接続が必要なテストは、ローカルMySQLではなくテスト用H2を使う。
- バグ修正時は、可能なら再発防止のテストを追加する。
- テストのJavadocには、「どのAPI仕様を確認しているか」を簡潔に書く。

### Spring Boot Test Context

`@SpringBootTest` はSpring Bootのアプリケーションコンテキストを起動するため、Controller層やSecurity込みのAPIテストには分かりやすい一方、テスト数が増えると重くなりやすいです。

現時点の判断:

- 現在のテスト数では、メモリ圧迫やOOMは起きていない。
- `./mvnw test` が安定して成功しているため、今すぐ分割や最適化は不要。
- ただし `@SpringBootTest` のControllerテストが増えるほど、起動時間とメモリ使用量は増えやすい。

今後重くなった場合の対応:

- ServiceなどSpring全体が不要な処理は、単体テストへ切り出す。
- Controllerだけを薄く確認したい場合は、必要に応じて `@WebMvcTest` を検討する。
- Security、Session、Repository、DBまで含めたいAPI仕様テストは `@SpringBootTest` + `MockMvc` を使う。

### JUnit 5 の `@Tag`

`@Tag` は、テストを種類ごとに分けて実行したくなった段階で使います。

例:

```java
@Tag("integration")
@SpringBootTest
class MyScoreControllerTest {
}
```

現時点の判断:

- まだテスト数が少ないため、`@Tag` は導入しなくてよい。
- `unit`、`integration`、`slow` のように分けたくなった時に導入する。
- 導入する場合は、MavenやCI側の実行コマンドも合わせて整理する。
