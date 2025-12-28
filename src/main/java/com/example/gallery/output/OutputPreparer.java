package com.example.gallery.output;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/** 出力先ディレクトリ（dist/assets）の準備を行う。 */
public final class OutputPreparer {

  private OutputPreparer() {}

  /**
   * 出力先を準備する（パス検証、--clean削除、ディレクトリ作成）。
   *
   * <p>事故防止のため、inputDir と outputDir の包含関係を禁止する。
   *
   * @param inputDir 入力ディレクトリ
   * @param outputDir 出力ディレクトリ
   * @param clean true の場合、出力先を削除して作り直す
   * @return 出力先パス群
   * @throws IOException ファイル操作に失敗した場合
   * @throws OutputPreparationException パスが危険/不正な場合
   */
  public static OutputPaths prepare(Path inputDir, Path outputDir, boolean clean)
      throws IOException {
    Path cwd = Path.of(".").toAbsolutePath().normalize();
    return prepare(inputDir, outputDir, clean, cwd);
  }

  /**
   * 出力先を準備する（パス検証、--clean削除、ディレクトリ作成）。
   *
   * <p>事故防止のため、inputDir と outputDir の包含関係を禁止する。
   *
   * <p>allowedDeleteRoot は --clean 時に削除を許可する基準ディレクトリで、 outputDir がこの配下にある場合のみ削除を許可する（誤削除防止）。
   * 通常のCLI実行ではプロジェクト配下のみ削除可能にするため、カレントディレクトリを渡す。テストでは一時ディレクトリを渡して削除処理を検証できる。
   *
   * @param inputDir 入力ディレクトリ
   * @param outputDir 出力ディレクトリ
   * @param clean true の場合、出力先を削除して作り直す
   * @param allowedDeleteRoot --clean 時の削除を許可する基準ディレクトリ
   * @return 出力先パス群
   * @throws IOException ファイル操作に失敗した場合
   * @throws OutputPreparationException パスが危険/不正な場合
   */
  public static OutputPaths prepare(
      Path inputDir, Path outputDir, boolean clean, Path allowedDeleteRoot) throws IOException {

    Path absInput = inputDir.toAbsolutePath().normalize();
    Path absOutput = outputDir.toAbsolutePath().normalize();

    // SafePaths にて安全チェックを行う
    SafePaths.validateNoContainment(absInput, absOutput);

    if (clean) {
      safeDeleteDirectory(absOutput, allowedDeleteRoot);
    }

    Files.createDirectories(absOutput);
    Path assetsDir = absOutput.resolve("assets");
    Files.createDirectories(assetsDir);

    Path indexHtml = absOutput.resolve("index.html");
    return new OutputPaths(absOutput, assetsDir, indexHtml);
  }

  /**
   * --clean 時に outputDir を安全に削除する。
   *
   * <p>削除対象が許可ルート配下でない場合は削除しない（事故防止）。
   */
  private static void safeDeleteDirectory(Path absOutput, Path allowedDeleteRoot)
      throws IOException {
    if (!Files.exists(absOutput)) {
      return;
    }

    // SafePaths にて削除安全チェックを行う
    SafePaths.validateDeleteTargetIsUnder(absOutput, allowedDeleteRoot);

    Files.walkFileTree(
        absOutput,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.deleteIfExists(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.deleteIfExists(dir);
            return FileVisitResult.CONTINUE;
          }
        });
  }
}
