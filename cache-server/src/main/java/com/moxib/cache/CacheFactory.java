package com.moxib.cache;

public class CacheFactory {
  public static Cache newCache(Cache.CacheType cacheType) throws Exception {
    if(cacheType.equals(Cache.CacheType.MEMORY)) {
      return new InMemoryCache();
    } else {
      throw new Exception("invalid cache type");
    }
  }
}
