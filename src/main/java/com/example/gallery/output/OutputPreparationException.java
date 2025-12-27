package com.example.gallery.output;

/** 出力先の準備（dist作成/削除、パス検証など）に失敗した場合の例外。 */
public class OutputPreparationException extends RuntimeException {
  public OutputPreparationException(String message) {
    super(message);
  }
}
