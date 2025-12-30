# 設計（design）

このドキュメントは、**Media Gallery Generator CLI** の設計（クラス責務・処理フロー）をまとめたもの。

---

## 1. 全体像

本ツールは、設定ファイルを読み込み、入力ディレクトリを走査してメディア（画像/動画）を収集し、`dist/` 配下に `index.html` と `assets/`（実体コピー）を生成する。

### 1.1 ビルド処理の流れ

1. 設定ファイル（JSON）を読み込んで `AppConfig` を作る
2. 出力先を準備（パス安全チェック、`--clean` 対応、`dist/assets` 作成）
3. 入力ディレクトリを走査して `List<MediaItem>` を作成
4. `dist/assets` に実体コピー
5. HTML テンプレを読み込み、タイトル・一覧を差し込んで `index.html` を出力

---

## 2. 設計方針

### 2.1 CLI の責務はオーケストレーション

`BuildCommand` は、各コンポーネント（config/scan/output/render）を順に呼び出して、ユーザー向けログを出す役割に寄せる。

### 2.2 危ないファイル操作を避ける

- 出力削除（`--clean`）は、**allowed root（許可削除ルート）配下か**などの条件で抑止する想定（`SafePaths`）
- `assets` へのコピーは、`relativePath` を正規化しつつ **パストラバーサル（`..` などで外に出る）を拒否**する。

### 2.3 相対パスは設定ファイルの場所を基準に解決

設定の `inputDir/outputDir` は、設定ファイルのディレクトリを基準に解決済み `Path` として保持する。

---

## 3. パッケージ構成と責務

### `com.example.gallery`

- `Main`: エントリポイント。picocli を起動して終了コードを返す。

### `com.example.gallery.cli`

- `BuildCommand`: `gallery build`。設定ロード → 出力準備 → 走査 → コピー → HTML 生成を実行。

### `com.example.gallery.config`

- `ConfigLoader`: JSON 設定ファイルを読み込み、バリデーションして `AppConfig` を返す。
- `AppConfig`: 解決済み設定（`title/inputDir/outputDir/extensions/sort`）

### `com.example.gallery.domain`

- `MediaItem`: 走査で見つかったメディア 1 件分（source/relative/type/size/mtime）。
- `SortMode`: ソート指定（例: `modified_desc`）を解決する。

### `com.example.gallery.scan`

- `MediaScanner`: walk → フィルタ → メタ情報 → ソートで `List<MediaItem>` を返す。
- `MediaFilter`: 拡張子で対象判定（正規化含む）。
- `MediaMetadataReader`: size/mtime の取得を集約。

### `com.example.gallery.output`

- `OutputPreparer`: 出力先準備（パス検証、`--clean`、ディレクトリ作成）。
- `SafePaths`: input/output の包含関係や削除対象の安全性を検証する。
- `OutputPaths`: 生成物の出力先パス群（`dist`, `assets`, `index.html`）。
- `AssetCopier`: `dist/assets` へのコピー（相対構造維持＋安全チェック）。

### `com.example.gallery.render`

- `HtmlTemplateLoader`: クラスパス上のテンプレを UTF-8 で読みこむ。
- `HtmlGalleryRenderer`: HTML テンプレに `{{TITLE}}/{{GENERATED_AT}}/{{ITEMS}}` を差し込み、HTML 文字列を返す。

---

## 4. 詳細フロー

### 4.1 設定ロード（ConfigLoader）

- 設定ファイルの存在・種別チェック後、Jackson `ObjectMapper` で `RawConfig` を読み込む。
- `title` が未指定/空なら `"Media Gallery"` が入る。
- `inputDir/outputDir` は **設定ファイルの場所基準**で `Path` に解決する。
- `includeExtensions` は trim / `.`除去 / 小文字化 / distinct で正規化し、空ならエラーにする。
- `sort` は `SortMode.from` で解決し、不正なら`ConfigValidationException` にする。

### 4.2 出力準備（OutputPreparer / SafePaths）

`BuildCommand` は `OutputPreparer.prepare(input, output, clean)` を呼ぶ。  
`OutputPreparer` は以下を担当する：

- `SafePaths.validateInputAndOutputPaths(...)` による安全チェック（包含関係禁止など）
- `--clean` のとき、削除対象が安全な範囲にあることを検証してから削除する設計（SafePaths）
- `dist/` と `dist/assets/` を作成し、`OutputPaths` を返す。

### 4.3 走査（MediaScanner）

`MediaScanner.scan` は、**walk → フィルタ → メタ情報取得 → ソート**を一括で行う。

- walk: `Files.walk` で再帰走査
- filter: 通常ファイルのみを対象にし、拡張子フィルタ（`MediaFilter`）を適用
- metadata: サイズ/更新日時を取得し `MediaItem` に格納
- sort: `SortMode` に従ってソート

拡張子判定は `MediaFilter.matchExtension` が担い、`.JPG` → `jpg` のように正規化する。

### 4.4 実体コピー（AssetCopier）

`AssetCopier.copyAll(items, assetsDir)` は `assetsDir/relativePath` にコピーする。  
安全対策として、

- `relativePath` が絶対パスなら拒否
- 正規化後に `assetsDir` 外へ出る（`..` 等）ケースを拒否
- `.` を含むパス要素も拒否

### 4.5 HTML 生成（HtmlTemplateLoader / HtmlGalleryRenderer）

- テンプレ読込は `HtmlTemplateLoader.loadUtf8("/templates/index.html")` 。
- `HtmlGalleryRenderer.render` はテンプレの `{{TITLE}}/{{GENERATED_AT}}/{{ITEMS}}` を置換し、HTML 文字列を返す。
- `items` が 0 件のときは「メディアがありません」を出す。

---

## 5. エラー設計（例外の方針）

基本方針は「責務単位で例外型を分け、CLI 側で握ってメッセージ化」。

- config: `ConfigValidationException`
- scan: `MediaScanException`
- output: `OutputPreparationException`
- render: `HtmlWriteException`

`BuildCommand` 自体は `Callable<Integer>` で、picocli から終了コードとして扱える構造。  
また `Main` は、その終了コードで `System.exit` する。

---
