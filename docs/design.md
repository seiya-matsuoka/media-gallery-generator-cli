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
