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
