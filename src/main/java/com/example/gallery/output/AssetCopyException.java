package com.example.gallery.output;

/** assets へのコピー処理に失敗した場合の例外。 */
public class AssetCopyException extends RuntimeException {
  public AssetCopyException(String message) {
    super(message);
  }
}
