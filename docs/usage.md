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
