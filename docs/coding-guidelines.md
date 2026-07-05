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

未ログインユーザーのプレイ結果は、引き続きフロントエンドのlocalStorageへ保存します。API保存に失敗した場合も、localStorage側の保存結果は消さない方針です。

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
