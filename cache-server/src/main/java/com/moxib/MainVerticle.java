package com.moxib;

import com.moxib.cache.Cache;
import com.moxib.cache.InMemoryCache;
import com.moxib.server.HttpCacheVerticle;
import com.moxib.server.TcpCacheVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) {
    Cache cache = new InMemoryCache();
    vertx.deployVerticle(new TcpCacheVerticle(cache))
      .map(vertx.deployVerticle(new HttpCacheVerticle(cache)))
      .onSuccess(success -> logger.info("Cache server started"))
      .onFailure(err -> logger.error("Cache Server started failed", err));
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }
}
