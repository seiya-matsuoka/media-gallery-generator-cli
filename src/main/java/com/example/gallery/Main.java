package com.example.gallery;

import com.example.gallery.cli.GalleryCommand;
import picocli.CommandLine;

/** アプリのエントリポイント（CLI起動のみを担当）。 */
public class Main {
  public static void main(String[] args) {
    int exitCode = new CommandLine(new GalleryCommand()).execute(args);
    System.exit(exitCode);
  }
}
