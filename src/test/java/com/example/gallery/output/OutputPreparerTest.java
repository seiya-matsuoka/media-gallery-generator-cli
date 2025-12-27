package com.example.gallery.output;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OutputPreparerTest {

  @TempDir Path tempDir;

  // --clean=true のとき、outputDir が削除されてから作り直され、assets/ が作成されることを確認する
  @Test
  void cleansAndRecreatesOutputDirectories() throws Exception {
    Path input = tempDir.resolve("media");
    Path output = tempDir.resolve("dist");

    Files.createDirectories(input);
    Files.createDirectories(output.resolve("assets"));
    Files.writeString(output.resolve("old.txt"), "old");

    // clean=true を検証
    OutputPaths paths = OutputPreparer.prepare(input, output, true);

    assertTrue(Files.isDirectory(paths.outputDir()));
    assertTrue(Files.isDirectory(paths.assetsDir()));
    assertTrue(Files.notExists(output.resolve("old.txt")));
  }

  // outputDir が inputDir 配下の場合は拒否されることを確認する
  @Test
  void rejectsOutputUnderInput() {
    Path input = tempDir.resolve("work");
    Path output = input.resolve("dist");
    assertThrows(
        OutputPreparationException.class, () -> OutputPreparer.prepare(input, output, false));
  }

  // inputDir が outputDir 配下の場合も拒否されることを確認する
  @Test
  void rejectsInputUnderOutput() {
    Path output = tempDir.resolve("dist");
    Path input = output.resolve("media");
    assertThrows(
        OutputPreparationException.class, () -> OutputPreparer.prepare(input, output, false));
  }
}
