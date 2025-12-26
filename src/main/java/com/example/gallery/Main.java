package com.example.gallery;

import com.example.gallery.cli.BuildCommand;
import com.example.gallery.cli.InitCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * ルートコマンド（gallery）。
 *
 * <p>フォルダ内の画像・動画から静的なギャラリー（HTMLファイル）を生成するCLIのエントリポイント。
 *
 * <p>サブコマンドとして {@code init} と {@code build} を持つ。
 */
@Command(
    name = "gallery",
    mixinStandardHelpOptions = true,
    description = "フォルダ内の画像・動画から静的なギャラリー（HTMLファイル）を生成する。",
    subcommands = {InitCommand.class, BuildCommand.class})
public class Main implements Runnable {

  @Override
  public void run() {
    CommandLine.usage(this, System.out);
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }
}
