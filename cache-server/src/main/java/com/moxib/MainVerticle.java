package com.moxib;

import com.moxib.cache.Cache;
import com.moxib.server.HttpCacheVerticle;
import com.moxib.server.TcpCacheVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) {
    // 获取config.json的配置
    vertx.fileSystem().readFile("config.json").map(Buffer::toJsonObject).onSuccess(config -> {
      Cache cache = null;
      try {
        cache = Cache.createCache(config);
      } catch (Exception e) {
        startPromise.fail(e);
      }
      vertx.deployVerticle(new TcpCacheVerticle(cache))
        .map(vertx.deployVerticle(new HttpCacheVerticle(cache)))
        .onSuccess(success -> {
          logger.info("Cache server started");
          startPromise.complete();
        })
        .onFailure(err -> {
          logger.error("Cache Server started failed", err);
          startPromise.fail(err);
        });
    }).onFailure(err -> {
      logger.error("get config error");
    });
  }
}
