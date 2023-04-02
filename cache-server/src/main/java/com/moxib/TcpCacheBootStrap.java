package com.moxib;

import com.moxib.cache.Cache;
import com.moxib.cluster.CacheCluster;
import com.moxib.server.HttpEndPoint;
import com.moxib.server.TcpServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class TcpCacheBootStrap extends AbstractVerticle {
  private TcpServer tcpServer;
  private HttpEndPoint httpEndPoint;
  private CacheCluster cacheCluster;

  @Override
  public void start() throws Exception {
    JsonObject cacheConfig = config();

    Cache cache = Cache.createCache(cacheConfig.getJsonObject("cache"));
    tcpServer = new TcpServer(cache, vertx, cacheConfig.getJsonObject("server"));
    httpEndPoint = new HttpEndPoint(cache, vertx, cacheConfig.getJsonObject("endpoint"));
    cacheCluster = new CacheCluster(cacheConfig.getJsonObject("cluster"));
    tcpServer.initTcpServer();
    httpEndPoint.initHttpEndPoint();
    cacheCluster.initCluster();
  }

  @Override
  public void stop() {
    tcpServer.shutDownTcpServer();
    httpEndPoint.closeHttpEndPoint();
    cacheCluster.closeCluster();
  }
}
