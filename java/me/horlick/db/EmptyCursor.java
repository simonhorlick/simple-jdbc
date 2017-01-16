package me.horlick.db;

public class EmptyCursor implements Cursor {

  @Override
  public Row next() {
    return null;
  }

  @Override
  public void close() throws Exception {}
}
