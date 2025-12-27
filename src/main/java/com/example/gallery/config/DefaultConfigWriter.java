package com.example.gallery.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** デフォルト設定ファイル（gallery.config.json）の出力を担当する。 */
public final class DefaultConfigWriter {

  private static final String RESOURCE_PATH = "/default-config/gallery.config.json";

  private DefaultConfigWriter() {}

  /**
   * デフォルト設定ファイルを指定パスへ出力する。
   *
   * @param outputPath 出力先
   * @param overwrite true の場合、既存ファイルを上書きする
   * @throws IOException 入出力エラー
   */
  public static void write(Path outputPath, boolean overwrite) throws IOException {
    if (Files.exists(outputPath) && !overwrite) {
      throw new FileAlreadyExistsException(outputPath.toString());
    }

    Path parent = outputPath.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }

    try (InputStream in = DefaultConfigWriter.class.getResourceAsStream(RESOURCE_PATH)) {
      if (in == null) {
        throw new IOException("デフォルト設定が見つかりません: " + RESOURCE_PATH);
      }
      Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
