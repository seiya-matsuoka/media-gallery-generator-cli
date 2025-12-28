package com.example.gallery.cli;

import com.example.gallery.config.AppConfig;
import com.example.gallery.config.ConfigLoader;
import com.example.gallery.config.ConfigValidationException;
import com.example.gallery.domain.MediaItem;
import com.example.gallery.output.AssetCopier;
import com.example.gallery.output.AssetCopyException;
import com.example.gallery.output.OutputPaths;
import com.example.gallery.output.OutputPreparationException;
import com.example.gallery.output.OutputPreparer;
import com.example.gallery.render.HtmlGalleryRenderer;
import com.example.gallery.render.HtmlTemplateLoader;
import com.example.gallery.render.HtmlWriteException;
import com.example.gallery.scan.MediaScanException;
import com.example.gallery.scan.MediaScanner;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * {@code gallery build} サブコマンド。
 *
 * <p>設定ファイルを読み込み、入力フォルダの走査をし、dist/index.html と dist/assets を生成する。
 */
@Command(name = "build", description = "dist/index.html と dist/assets を生成する。")
public class BuildCommand implements Callable<Integer> {

  @Option(
      names = "--config",
      description = "設定ファイルのパス",
      defaultValue = "./work/gallery.config.json")
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

      // 出力先準備（--clean対応 + パス安全チェック + dist/assets作成）
      OutputPaths out = OutputPreparer.prepare(cfg.inputDir(), cfg.outputDir(), clean);
      System.out.println();
      System.out.println("build: 出力先の準備が完了しました");
      System.out.printf("  dist: %s%n", out.outputDir());
      System.out.printf("  assets: %s%n", out.assetsDir());
      System.out.printf("  index: %s%n", out.indexHtmlPath());

      List<MediaItem> items =
          MediaScanner.scan(cfg.inputDir(), cfg.includeExtensions(), cfg.sort());

      System.out.println();
      System.out.printf("build: メディア走査が完了しました（件数: %d）%n", items.size());
      int preview = Math.min(items.size(), 5);
      for (int i = 0; i < preview; i++) {
        MediaItem item = items.get(i);
        System.out.printf(
            "  - [%s] %s (size=%d bytes, modified=%s)%n",
            item.type(),
            item.relativePath().toString().replace('\\', '/'),
            item.sizeBytes(),
            item.lastModifiedAt());
      }

      // assets へコピー（相対構造維持）
      int copied = AssetCopier.copyAll(items, out.assetsDir());

      System.out.println();
      System.out.printf("build: assets へのコピーが完了しました（件数: %d）%n", copied);
      System.out.printf("  assets: %s%n", out.assetsDir());

      // index.html 生成（テンプレ読込 → レンダ → 書き込み）
      try {
        String template = HtmlTemplateLoader.loadUtf8("/templates/index.html");
        String html = HtmlGalleryRenderer.render(template, "Media Gallery", items);
        Files.writeString(out.indexHtmlPath(), html, StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new HtmlWriteException("index.html の生成に失敗しました: " + out.indexHtmlPath(), e);
      }

      System.out.println();
      System.out.println("build: index.html の生成が完了しました");
      System.out.printf("  index: %s%n", out.indexHtmlPath());

      return 0;
    } catch (OutputPreparationException e) {
      System.err.println("build: 出力先の準備に失敗しました");
      System.err.println("  " + e.getMessage());
      return 1;
    } catch (ConfigValidationException e) {
      System.err.println("build: 設定が不正です");
      System.err.println("  " + e.getMessage());
      return 1;
    } catch (MediaScanException e) {
      System.err.println("build: 入力フォルダの走査に失敗しました");
      System.err.println("  " + e.getMessage());
      return 1;
    } catch (AssetCopyException e) {
      System.err.println("build: assets へのコピーに失敗しました");
      System.err.println("  " + e.getMessage());
      return 1;
    } catch (HtmlWriteException e) {
      System.err.println("build: HTML生成に失敗しました");
      System.err.println("  " + e.getMessage());
      return 1;
    } catch (IOException e) {
      System.err.println("build: 入出力エラーが発生しました");
      System.err.println("  " + e.getMessage());
      return 1;
    }
  }
}
