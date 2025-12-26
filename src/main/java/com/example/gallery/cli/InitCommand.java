package com.example.gallery.cli;

import picocli.CommandLine.Command;

/**
 * {@code gallery init} サブコマンド。
 *
 * <p>デフォルト設定ファイル（gallery.config.json）を生成する。
 */
@Command(name = "init", description = "デフォルトの設定ファイル（gallery.config.json）を生成する。")
public class InitCommand implements Runnable {
  @Override
  public void run() {
    System.out.println("init: 未実装");
  }
}
