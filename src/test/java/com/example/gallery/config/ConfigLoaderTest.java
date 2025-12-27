package com.example.gallery.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.gallery.domain.SortMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigLoaderTest {

  @TempDir Path tempDir;

  // configファイルのあるディレクトリを基準に、input/outputの相対パスが解決されることを確認する
  @Test
  void resolvesRelativePathsAgainstConfigDirectory() throws Exception {
    Path configDir = tempDir.resolve("config");
    Files.createDirectories(configDir);

    Path configFile = configDir.resolve("gallery.config.json");
    Files.writeString(
        configFile,
        """
        {
          "inputDir": "../media",
          "outputDir": "../dist",
          "includeExtensions": ["jpg", "mp4"],
          "sort": "modified_desc"
        }
        """);

    AppConfig cfg = ConfigLoader.load(configFile);

    // ../media, ../dist が configDir（tempDir/config）基準で tempDir 配下に解決される想定
    assertEquals(tempDir.resolve("media").normalize(), cfg.inputDir());
    assertEquals(tempDir.resolve("dist").normalize(), cfg.outputDir());
    assertEquals(SortMode.MODIFIED_DESC, cfg.sort());
  }

  // includeExtensions が「先頭ドット除去」「小文字化」「重複排除」されることを確認する
  @Test
  void normalizesExtensionsToLowercaseWithoutDot() throws Exception {
    Path configFile = tempDir.resolve("gallery.config.json");
    Files.writeString(
        configFile,
        """
        {
          "inputDir": "./media",
          "outputDir": "./dist",
          "includeExtensions": [".JPG", " jpg ", ".Mp4", "mp4"],
          "sort": "modified_desc"
        }
        """);

    AppConfig cfg = ConfigLoader.load(configFile);

    // ".JPG" / " jpg " / ".Mp4" / "mp4" → ["jpg", "mp4"] に正規化される想定
    assertEquals(List.of("jpg", "mp4"), cfg.includeExtensions());
  }

  // 指定した設定ファイルが存在しない場合に、ConfigValidationException になることを確認する
  @Test
  void rejectsMissingConfigFile() {
    Path missing = tempDir.resolve("missing.json");
    assertThrows(ConfigValidationException.class, () -> ConfigLoader.load(missing));
  }
}
