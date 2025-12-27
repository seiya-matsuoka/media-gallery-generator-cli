package com.example.gallery.cli;

import com.example.gallery.config.AppConfig;
import com.example.gallery.config.ConfigLoader;
import com.example.gallery.config.ConfigValidationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * {@code gallery build} サブコマンド。
 *
 * <p>設定ファイルを読み込み、dist/index.html と dist/assets を生成する。
 */
@Command(name = "build", description = "dist/index.html と dist/assets を生成する。")
public class BuildCommand implements Callable<Integer> {

  @Option(
      names = "--config",
      description = "設定ファイルのパス",
      defaultValue = "./config/gallery.config.json")
  private Path config;

  @Option(names = "--clean", description = "ビルド前に dist を削除してから生成する")
  private boolean clean;

  @Override
  public Integer call() {
    try {
      AppConfig cfg = ConfigLoader.load(config);
      System.out.println("build: 設定ファイルの読み込みに成功しました");
      System.out.printf("  config: %s%n", config.toAbsolutePath().normalize());
      System.out.printf("  inputDir: %s%n", cfg.inputDir());
      System.out.printf("  outputDir: %s%n", cfg.outputDir());
      System.out.printf("  sort: %s%n", cfg.sort().id());
      System.out.printf("  extensions: %s%n", cfg.includeExtensions());
      System.out.printf("  clean: %s%n", clean);
      return 0;
    } catch (ConfigValidationException e) {
      System.err.println("build: 設定が不正です");
      System.err.println("  " + e.getMessage());
      return 1;
    } catch (IOException e) {
      System.err.println("build: 設定ファイルの読み込みに失敗しました");
      System.err.println("  " + e.getMessage());
      return 1;
    }
  }
}
