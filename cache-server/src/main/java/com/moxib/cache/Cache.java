package com.moxib.cache;

public interface Cache {
  /**
   * 根据key获取
   * @param key
   * @return value
   */
  byte[] get(byte[] key);
  /**
   * 根据key、value设置
   * @param key
   * @return value
   */
  void set(byte[] key, byte[] value);
  /**
   * 根据key删除
   * @param key
   * @return value
   */
  void del(byte[] key);
  /**
   * 获取状态
   * @return stat
   */
  Stat getStat();

  enum CacheType {
    MEMORY,
    ROCKSDB;
  }
}
