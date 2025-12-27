package com.example.gallery.output;

import java.nio.file.Path;

/**
 * パス操作における事故（入力/出力の巻き込み削除、包含関係の混在など）を防ぐための検証ユーティリティ。
 *
 * <ul>
 *   <li>inputDir と outputDir の包含関係・同一パスの禁止
 *   <li>削除対象がプロジェクト（カレント）配下であることの確認
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
   * --clean 等で削除するディレクトリが、カレントディレクトリ配下であることを検証する。
   *
   * <p>誤ったパス指定でプロジェクト外を削除しないための安全ガード。
   *
   * @param directoryToDelete 削除対象（絶対パス・正規化済みを推奨）
   * @throws OutputPreparationException プロジェクト外の場合
   */
  public static void validateDeleteTargetIsUnderCwd(Path directoryToDelete) {
    Path cwd = Path.of(".").toAbsolutePath().normalize();
    if (!directoryToDelete.startsWith(cwd)) {
      throw new OutputPreparationException(
          "--clean で削除しようとしたディレクトリがプロジェクト配下ではありません: " + directoryToDelete);
    }
  }
}
