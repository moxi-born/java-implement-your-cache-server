package com.moxib.cache;

import io.vertx.core.json.JsonObject;

public class CacheFactory {
  public static Cache newCache(JsonObject cacheConfig) throws Exception {
    if(cacheConfig.getString("type").equals("memory")) {
      return new InMemoryCache();
    } else if(cacheConfig.getString("type").equals("rocksDb")) {
      return new RocksDbCache(cacheConfig);
    } else {
      throw new Exception("invalid cache type");
    }
  }
}
