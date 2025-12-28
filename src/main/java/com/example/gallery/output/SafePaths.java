package com.example.gallery.output;

import java.nio.file.Path;

/**
 * パス操作における事故（入力/出力の巻き込み削除、包含関係の混在など）を防ぐための検証ユーティリティ。
 *
 * <ul>
 *   <li>inputDir と outputDir の包含関係・同一パスの禁止
 *   <li>削除対象が許可ルート配下であることの確認
 * </ul>
 */
public final class SafePaths {

  private SafePaths() {}

  /**
   * inputDir と outputDir が安全な関係であることを検証する。
   *
   * <p>禁止条件：
   *
   * <ul>
   *   <li>inputDir と outputDir が同一
   *   <li>outputDir が inputDir 配下（出力が入力に混ざる）
   *   <li>inputDir が outputDir 配下（--clean で入力を巻き込む可能性）
   * </ul>
   *
   * @param inputDir 入力ディレクトリ（絶対パス・正規化済みを推奨）
   * @param outputDir 出力ディレクトリ（絶対パス・正規化済みを推奨）
   * @throws OutputPreparationException 危険/不正な場合
   */
  public static void validateNoContainment(Path inputDir, Path outputDir) {
    // 同一パスは禁止
    if (inputDir.equals(outputDir)) {
      throw new OutputPreparationException("inputDir と outputDir が同一です: " + outputDir);
    }

    // output が input 配下は禁止（出力が入力に混ざる）
    if (outputDir.startsWith(inputDir)) {
      throw new OutputPreparationException(
          "outputDir が inputDir 配下です（危険）: outputDir=" + outputDir + ", inputDir=" + inputDir);
    }

    // input が output 配下も禁止（clean が input を巻き込む可能性）
    if (inputDir.startsWith(outputDir)) {
      throw new OutputPreparationException(
          "inputDir が outputDir 配下です（危険）: inputDir=" + inputDir + ", outputDir=" + outputDir);
    }

    // ルート直下の削除などを避けるための最低限ガード（nameCount=0 は "C:\" や "/" 等になりやすい）
    if (outputDir.getNameCount() == 0) {
      throw new OutputPreparationException("outputDir が不正です: " + outputDir);
    }
  }

  /**
   * 削除対象ディレクトリが、許可ルート配下であることを検証する。
   *
   * @param directoryToDelete 削除対象（絶対パス・正規化済みを推奨）
   * @param allowedRoot 削除を許可する基準ディレクトリ（絶対パス・正規化済みを推奨）
   * @throws OutputPreparationException 許可ルート外の場合
   */
  public static void validateDeleteTargetIsUnder(Path directoryToDelete, Path allowedRoot) {
    Path absDir = directoryToDelete.toAbsolutePath().normalize();
    Path absRoot = allowedRoot.toAbsolutePath().normalize();
    if (!absDir.startsWith(absRoot)) {
      throw new OutputPreparationException(
          "--clean で削除しようとしたディレクトリが許可ルート配下ではありません: dir=" + absDir + ", root=" + absRoot);
    }
  }

  /**
   * ショートカット用：削除対象ディレクトリがカレントディレクトリ（プロジェクト）配下であることを検証する。
   *
   * <p>内部的には {@link #validateDeleteTargetIsUnder(Path, Path)} に対して allowedRoot
   * としてカレントディレクトリを渡すのと同等。 CLI実行時に「プロジェクト外の削除」を禁止する用途で使うことを想定する。
   *
   * @param directoryToDelete 削除対象ディレクトリ（絶対パス・正規化済みを推奨）
   * @throws OutputPreparationException カレントディレクトリ配下ではない場合
   */
  public static void validateDeleteTargetIsUnderCwd(Path directoryToDelete) {
    Path cwd = Path.of(".").toAbsolutePath().normalize();
    validateDeleteTargetIsUnder(directoryToDelete, cwd);
  }
}
