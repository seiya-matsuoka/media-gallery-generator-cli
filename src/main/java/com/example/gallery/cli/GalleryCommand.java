package com.example.gallery.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * ルートコマンド（gallery）。
 *
 * <p>フォルダ内の画像・動画から静的なギャラリー（HTMLファイル）を生成する。
 *
 * <p>サブコマンドとして {@code init} と {@code build} を持つ。
 */
@Command(
    name = "gallery",
    mixinStandardHelpOptions = true,
    description = "フォルダ内の画像・動画から静的なギャラリー（HTMLファイル）を生成する。",
    subcommands = {InitCommand.class, BuildCommand.class})
public class GalleryCommand implements Runnable {

  @Override
  public void run() {
    // サブコマンドなしで実行された場合はヘルプを表示
    CommandLine.usage(this, System.out);
  }
}
