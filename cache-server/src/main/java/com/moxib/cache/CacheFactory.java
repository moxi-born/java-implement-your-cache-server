package com.moxib.cache;

import io.vertx.core.json.JsonObject;

public class CacheFactory {
  public static Cache newCache(JsonObject config) throws Exception {
    JsonObject cacheConfig = config.getJsonObject("cache");
    if(cacheConfig.getString("type").equals("memory")) {
      return new InMemoryCache();
    } else if(cacheConfig.getString("type").equals("rocksDb")) {
      return new RocksDbCache(config);
    } else {
      throw new Exception("invalid cache type");
    }
  }
}
