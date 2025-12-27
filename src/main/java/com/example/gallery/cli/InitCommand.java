package com.example.gallery.cli;

import com.example.gallery.config.DefaultConfigWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * {@code gallery init} サブコマンド。
 *
 * <p>デフォルト設定ファイル（gallery.config.json）を生成する。
 */
@Command(name = "init", description = "デフォルトの設定ファイル（gallery.config.json）を生成する。")
public class InitCommand implements Callable<Integer> {

  @Option(
      names = "--path",
      description = "出力先のパス（省略時: ${DEFAULT-VALUE}）",
      defaultValue = "./config/gallery.config.json")
  private Path path;

  @Option(names = "--force", description = "既存ファイルがあっても上書きする")
  private boolean force;

  @Override
  public Integer call() {
    Path out = path.toAbsolutePath().normalize();

    try {
      DefaultConfigWriter.write(path, force);
      System.out.printf("init: 設定ファイルを生成しました: %s%n", out);
      return 0;
    } catch (FileAlreadyExistsException e) {
      System.err.printf("init: 既にファイルが存在します: %s%n", out);
      System.err.println("init: 上書きする場合は --force を付けてください。");
      return 1;
    } catch (IOException e) {
      System.err.printf("init: 設定ファイルの生成に失敗しました: %s%n", e.getMessage());
      return 1;
    }
  }
}
