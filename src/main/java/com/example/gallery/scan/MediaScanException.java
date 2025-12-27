package com.example.gallery.scan;

/** メディア走査に必要な前提（入力ディレクトリなど）が満たされない場合の例外。 */
public class MediaScanException extends RuntimeException {
  public MediaScanException(String message) {
    super(message);
  }
}
