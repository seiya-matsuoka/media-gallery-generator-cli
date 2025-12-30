# 使い方（usage）

このドキュメントは、**Media Gallery Generator CLI** を動かすための手順と、コマンド/設定/出力仕様をまとめたもの。

---

## 0. このツールでできること

- `work/media` 配下の画像/動画を再帰走査して一覧化
- `work/dist/index.html` と `work/dist/assets/` を生成
- タイトルや拡張子フィルタ、ソート順を設定ファイルで変更可能

---

## 1. 前提

- **JDK 21**
- **Maven**
- （任意）VS Code（デバッグ/タスク実行を使う場合）

---

## 2. work ディレクトリとファイルの位置づけ

`work/` は「実行時に使う作業領域」。

- `work/gallery.config.example.json`  
  コミット対象のサンプル設定ファイル（例）
- `work/gallery.config.json`  
  **生成物（ローカル用）**。`gallery init` で作成し、必要に応じて編集して使う
- `work/media/`  
  入力メディア置き場（画像/動画）
- `work/dist/`  
  出力先（`build` で生成。`--clean` で削除 → 再生成される）

> 相対パス（例: `"./media"`）は「設定ファイルのある場所（`work/`）」を基準に解決する。  
> そのため、`gallery.config.json` の `inputDir: "./media"` は `work/media` を指す。

---

## 3. クイックスタート

プロジェクト直下で実行する。

### 3.1 設定ファイルを生成（init）

```powershell
.\gallery init
```

- 既に `work/gallery.config.json` がある場合は失敗する

- 上書きしたい場合：

```powershell
.\gallery init --force
```

### 3.2 メディアを置く

例：`work/media` 配下に jpg/png/mp4 を配置する。

### 3.3 生成（build）

```powershell
.\gallery build --clean
```

成功すると `work/dist/index.html` が生成される。

---

## 4. 実行方法

### 4.1 ラッパー経由（gallery.cmd）

Windows では `.cmd` を同梱しているため、以下で実行できる。

```powershell
.\gallery --help
.\gallery init
.\gallery build --clean
```

> ※ `gallery.cmd` は内部でプロジェクト直下へ移動し、`mvn -q exec:java` に引数を渡して起動する。

---

## 5. コマンド仕様

CLI コマンドは picocli で実装されている。

```powershell
.\gallery --help
```

### 5.1 `init`（設定ファイル生成）

```powershell
.\gallery init
```

オプション：

- `--path <path>`  
  出力先（省略時：`./work/gallery.config.json`）
- `--force`  
  既存ファイルがあっても上書きする

例：

```powershell
.\gallery init --path ./work/gallery.config.json
.\gallery init --force
```

### 5.2 `build`（HTML 生成 + assets コピー）

```powershell
.\gallery build
```

オプション：

- `--config <path>`  
  設定ファイル（省略時：`./work/gallery.config.json`）
- `--clean`  
  ビルド前に `dist` を削除してから生成する

例：

```powershell
.\gallery build --clean
.\gallery build --config ./work/gallery.config.json
```

---

## 6. 設定ファイル仕様（gallery.config.json）

`gallery init` で生成されるデフォルトは次の形（例）。

```json
{
  "title": "Media Gallery",
  "inputDir": "./media",
  "outputDir": "./dist",
  "includeExtensions": ["jpg", "jpeg", "png", "gif", "webp", "mp4"],
  "sort": "modified_desc"
}
```

### 6.1 各キーの意味

- `title`（string）  
  生成する HTML のタイトル/見出し
- `inputDir`（string）  
  入力ディレクトリ（設定ファイル基準の相対パス OK）
- `outputDir`（string）  
  出力ディレクトリ（設定ファイル基準の相対パス OK）
- `includeExtensions`（string[]）  
  対象拡張子。`.JPG` のような表記も内部で正規化される（`.`除去・小文字化）。
- `sort`（string）  
  ソート指定（例：`modified_desc`）

### 6.2 メディア種別の扱い

- `mp4` → 動画として扱い、HTML では `<video>` で出力
- それ以外 → 画像として扱い、HTML では `<img>` で出力

---

## 7. 出力仕様

`outputDir`（デフォルト：`work/dist`）配下に生成される。

- `dist/index.html`  
  ギャラリー本体（テンプレに `{{TITLE}}`, `{{GENERATED_AT}}`, `{{ITEMS}}` を差し込み）
- `dist/assets/`  
  入力ファイルの実体コピー先（相対構造維持）

### 7.1 `--clean` について

`--clean` を付けると、ビルド前に `outputDir` を削除して作り直す。  
事故防止のため、安全条件を満たさないパスは削除しない設計（詳細は `docs/design.md`）。

---

## 8. 成功/失敗の見分け方（ログ）

- `init` 成功例：`init: 設定ファイルを生成しました: ...`
- `build` 成功時は概ね以下が出ます：
  - `build: 設定ファイルの読み込みに成功しました`
  - `build: メディア走査が完了しました...`
  - `build: assets へのコピーが完了しました...`
  - `build: index.html の生成が完了しました`

失敗時は `build:` / `init:` のエラーメッセージが `stderr` に出力され、終了コード `1` になる。

---

## 9. VS Code での実行/デバッグ

### 9.1 Run and Debug（launch.json）

`Run and Debug` から、以下の構成を実行できる。

- `Gallery: help`
- `Gallery: init`
- `Gallery: build (clean)`（`--clean --config ./work/gallery.config.json`）

ブレークポイントを貼って動かしたい場合はここから起動する。

### 9.2 Tasks（tasks.json）

`Terminal` → `Run Task...` からタスクを実行できる。

- `mvn: test`
- `mvn: package`
- `gallery: help`
- `gallery: init`
- `gallery: build (--clean)`

---

## 10. 動作確認

### 10.1 ラッパー（gallery.cmd）

```powershell
.\gallery --help
.\gallery init
.\gallery build --clean
```

---

## 11. 注意点

- `--clean` は出力先を削除します。`outputDir` の設定を変更する場合は特に注意してください。
