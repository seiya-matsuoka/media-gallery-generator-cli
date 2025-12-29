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
