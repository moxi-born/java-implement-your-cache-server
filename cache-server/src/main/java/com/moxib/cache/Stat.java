package com.moxib.cache;

public class Stat {
  private int count;
  private int keySize;
  private int valueSize;

  public Stat(int count, int keySize, int valueSize) {
    this.count = count;
    this.keySize = keySize;
    this.valueSize = valueSize;
  }

  @Override
  public String toString() {
    return "Stat{" +
      "count=" + count +
      ", keySize=" + keySize +
      ", valueSize=" + valueSize +
      '}';
  }

  public synchronized void add(byte[] key, byte[] value) {
    count ++;
    keySize += key.length;
    valueSize += value.length;
  }


  public synchronized void del(byte[] key, byte[] value) {
    count --;
    keySize -= key.length;
    valueSize -= value.length;
  }
}
