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
    Path absInput = inputDir.toAbsolutePath().normalize();
    Path absOutput = outputDir.toAbsolutePath().normalize();

    // SafePaths にて安全チェックを行う
    SafePaths.validateNoContainment(absInput, absOutput);

    if (clean) {
      safeDeleteDirectory(absOutput);
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
   * <p>削除対象がプロジェクト（カレント）配下でない場合は削除しない（事故防止）。
   */
  private static void safeDeleteDirectory(Path absOutput) throws IOException {
    if (!Files.exists(absOutput)) {
      return;
    }

    // SafePaths にて削除安全チェックを行う
    SafePaths.validateDeleteTargetIsUnderCwd(absOutput);

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
