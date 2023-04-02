package com.moxib.server;

import com.moxib.cache.Cache;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class HttpEndPoint {
  private static final Logger logger = LoggerFactory.getLogger(HttpEndPoint.class);

  private final Cache cache;

  private final Vertx vertx;

  private final JsonObject endPointConfig;

  private HttpServer httpServer;

  public HttpEndPoint(Cache cache,  Vertx vertx, JsonObject endPointConfig) {
    this.cache = cache;
    this.vertx = vertx;
    this.endPointConfig = endPointConfig;
  }

  public void initHttpEndPoint() {
    Router router = Router.router(vertx);
    String prefix = "/cache";

    router.post(prefix + "/:key/:value").handler(this::setCache);
    router.get(prefix + "/:key").handler(this::getCache);
    router.delete(prefix + "/:key").handler(this::delCache);
    router.get(prefix + "/stat/").handler(this::getStat);

    httpServer = vertx.createHttpServer();
    httpServer
      .requestHandler(router)
      .listen(endPointConfig.getInteger("port", 8080))
      .onSuccess(success -> logger.info("http cache server start success"))
      .onFailure(err -> logger.error("http cache server start failed", err));
  }

  private void setCache(RoutingContext context) {
    String key = context.pathParam("key");
    String value = context.pathParam("value");
    cache.set(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
    context.response().setStatusCode(200).end("success");
  }

  private void getCache(RoutingContext context) {
    String key = context.pathParam("key");
    byte[] value = cache.get(key.getBytes(StandardCharsets.UTF_8));
    if(null != value) {
      context.response().setStatusCode(200).end(new String(value, StandardCharsets.UTF_8));
    } else {
      context.response().setStatusCode(500).end("");
    }
  }

  private void delCache(RoutingContext context) {
    String key = context.pathParam("key");
    cache.del(key.getBytes(StandardCharsets.UTF_8));
    context.response().setStatusCode(500).end("success");
  }

  private void getStat(RoutingContext context) {
    context.response().setStatusCode(200).end(JsonObject.mapFrom(cache.getStat()).toString());
  }

  public void closeHttpEndPoint() {
    if (null != httpServer) {
      httpServer.close()
        .onSuccess(success -> logger.info("http endpoint close successful"))
        .onFailure(err -> logger.error("http endpoint close failed"));
    }
  }
}
