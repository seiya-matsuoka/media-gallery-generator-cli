package com.example.gallery.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * {@code gallery build} サブコマンド。
 *
 * <p>設定ファイルを読み込み、dist/index.html と dist/assets を生成する。
 */
@Command(name = "build", description = "dist/index.html と dist/assets を生成する。")
public class BuildCommand implements Runnable {

  @Option(
      names = "--config",
      description = "設定ファイルのパス",
      defaultValue = "./config/gallery.config.json")
  private String config;

  @Option(names = "--clean", description = "ビルド前に dist を削除してから生成する")
  private boolean clean;

  @Override
  public void run() {
    System.out.printf("build: 未実装（config=%s, clean=%s）。%n", config, clean);
  }
}
