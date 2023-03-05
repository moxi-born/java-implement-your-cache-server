package com.moxib.cache;

import com.moxib.ByteUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class InMemoryCache implements Cache {
  private Map<byte[], byte[]> cache;
  private Stat stat;

  public InMemoryCache() {
    cache = new ConcurrentSkipListMap<>(ByteUtils.getDefaultByteArrayComparator());
    stat = new Stat(0, 0, 0);
  }


  @Override
  public byte[] get(byte[] key) {
    return cache.get(key);
  }

  @Override
  public void set(byte[] key, byte[] value) {
    cache.put(key, value);
    stat.add(key, value);
  }

  @Override
  public void del(byte[] key) {
    byte[] value = get(key);
    if(null != value) {
      stat.del(key, value);
    }
    cache.remove(key);
  }

  @Override
  public Stat getStat() {
    return this.stat;
  }
}
