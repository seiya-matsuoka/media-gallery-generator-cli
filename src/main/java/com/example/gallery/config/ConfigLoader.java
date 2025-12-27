package com.example.gallery.config;

import com.example.gallery.domain.SortMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** JSON設定ファイル（gallery.config.json）を読み込み、解決済みの設定を返す。 */
public final class ConfigLoader {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private ConfigLoader() {}

  /**
   * 設定ファイルを読み込む。
   *
   * @param configPath 設定ファイルのパス
   * @return 解決済みの設定
   * @throws IOException 入出力エラー
   * @throws ConfigValidationException 設定内容が不正な場合
   */
  public static AppConfig load(Path configPath) throws IOException {
    Objects.requireNonNull(configPath, "configPath");

    if (!Files.exists(configPath)) {
      throw new ConfigValidationException(
          "設定ファイルが見つかりません: " + configPath.toAbsolutePath().normalize());
    }
    if (!Files.isRegularFile(configPath)) {
      throw new ConfigValidationException(
          "設定ファイルが通常ファイルではありません: " + configPath.toAbsolutePath().normalize());
    }

    RawConfig raw;
    try {
      raw = MAPPER.readValue(configPath.toFile(), RawConfig.class);
    } catch (IOException e) {
      throw new ConfigValidationException("設定ファイルの読み込みに失敗しました（JSON形式を確認してください）: " + e.getMessage());
    }

    String inputDirStr = required(raw.inputDir, "inputDir");
    String outputDirStr = required(raw.outputDir, "outputDir");

    Path inputDir = ConfigPaths.resolveAgainstConfigDir(configPath, inputDirStr);
    Path outputDir = ConfigPaths.resolveAgainstConfigDir(configPath, outputDirStr);

    List<String> extensions = normalizeExtensions(raw.includeExtensions);
    if (extensions.isEmpty()) {
      throw new ConfigValidationException("includeExtensions が空です（少なくとも1つ指定してください）");
    }

    SortMode sort = SortMode.MODIFIED_DESC;
    if (raw.sort != null && !raw.sort.isBlank()) {
      try {
        sort = SortMode.from(raw.sort);
      } catch (IllegalArgumentException e) {
        throw new ConfigValidationException(
            "sort が不正です: " + raw.sort + "（例: " + SortMode.MODIFIED_DESC.id() + "）");
      }
    }

    return new AppConfig(inputDir, outputDir, extensions, sort);
  }

  private static String required(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new ConfigValidationException(fieldName + " が未指定です");
    }
    return value;
  }

  private static List<String> normalizeExtensions(List<String> raw) {
    if (raw == null) {
      throw new ConfigValidationException("includeExtensions が未指定です");
    }
    return raw.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .map(s -> s.startsWith(".") ? s.substring(1) : s)
        .map(s -> s.toLowerCase(Locale.ROOT))
        .filter(s -> !s.isBlank())
        .distinct()
        .toList();
  }

  /** JSONを受けるための中間モデル（そのままの値を受け取る） */
  private static class RawConfig {
    public String inputDir;
    public String outputDir;
    public List<String> includeExtensions;
    public String sort;

    /** Jackson がリフレクションで使用するデフォルトコンストラクタ。 */
    @SuppressWarnings("unused")
    public RawConfig() {}
  }
}
