package me.horlick.db;

public interface Cursor extends AutoCloseable {

  /**
   * Move to the next row in the dataset.
   *
   * @return Either the data contained in the next row, or null if no more rows exist.
   */
  Row next();
}
