package com.moxib.server;

import com.moxib.cache.Cache;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class TcpServer {
  private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);

  private static final Buffer PONG = Buffer.buffer(new byte[]{(byte) 1});

  private final Vertx vertx;

  private final Cache cache;

  private final JsonObject tcpServerConfig;

  private NetServer netServer;

  public TcpServer(Cache cache, Vertx vertx, JsonObject tcpServerConfig) {
    this.cache = cache;
    this.vertx = vertx;
    this.tcpServerConfig = tcpServerConfig;
  }

  // set操作 S<klen><SP><vlen><SP><key><value>
  // get操作 G<klen><SP><key>
  // del操作 D<klen><SP><key>
  public void initTcpServer() {
    int port = tcpServerConfig.getInteger("port", 3000);
    boolean logActivity = tcpServerConfig.getBoolean("logActivity", false);
    NetServerOptions serverOptions = new NetServerOptions().setLogActivity(logActivity);
    netServer = vertx.createNetServer(serverOptions);
    netServer.connectHandler(socket -> {
      // 固定长度模式，获取操作类型，buffer第一个byte
      RecordParser parser = RecordParser.newFixed(1, socket);
      parser.pause();
      parser.fetch(1);
      parser.handler(buffer -> readOperation(buffer, parser, socket));
    });
    netServer.listen(port)
      .onSuccess(success -> logger.info("cache server startup success at port {}", port))
      .onFailure(err -> logger.error("cache server startup failed", err));
  }

  /**
   * 获取操作类型
   * @param buffer
   * @param parser
   * @param socket
   */
  private void readOperation(Buffer buffer, RecordParser parser, NetSocket socket) {
    String operation = buffer.getString(0, 1);
    logger.debug("operation is {}", operation);
    if (null == operation || "".equals(operation)) {
      socket.write("invalid operation");
      return;
    }
    // 切换为分隔符模式，这里是空格" "
    parser.delimitedMode(Buffer.buffer(" "));
    parser.fetch(1);
    parser.handler(kLenBuffer -> readKeyLen(kLenBuffer, parser, operation, socket));
  }

  /**
   * 读取key长度并根据操作类型进行下一步操作
   * @param buffer
   * @param parser
   * @param operation
   * @param socket
   */
  private void readKeyLen(Buffer buffer, RecordParser parser, String operation, NetSocket socket) {
    int keyLength = buffer.getInt(0);
    logger.debug("key length is {}", keyLength);
    switch (operation) {
      case "S":
        parser.handler(vLenBuffer -> readValueLen(vLenBuffer, parser, keyLength, socket));
        parser.fetch(1);
        break;
      case "G":
        // 切换为固定长度模式获取key
        parser.fixedSizeMode(keyLength);
        parser.fetch(1);
        parser.handler(keyBuffer -> readKeyAndGet(keyBuffer, keyLength, socket));
        break;
      case "D":
        parser.fixedSizeMode(keyLength);
        parser.fetch(1);
        parser.handler(keyBuffer -> readKeyAndDel(keyBuffer, keyLength, socket));
        break;
      default:
        socket.write("invalid operation");
    }
  }

  /**
   * 获得value的长度
   * @param buffer
   * @param parser
   * @param keyLength
   * @param socket
   */
  private void readValueLen(Buffer buffer, RecordParser parser, int keyLength, NetSocket socket) {
    int valueLength = buffer.getInt(0);
    logger.debug("value length is {}", valueLength);
    parser.fixedSizeMode(keyLength + valueLength);
    parser.fetch(1);
    parser.handler(keyBuffer -> readKeyAndValue(keyBuffer, keyLength, valueLength, socket));
  }

  /**
   * 读取key和value并做set
   * @param buffer
   * @param keyLength
   * @param valueLength
   * @param socket
   */
  private void readKeyAndValue(Buffer buffer, int keyLength, int valueLength, NetSocket socket) {
    byte[] key = buffer.getBytes(0, keyLength);
    byte[] value = buffer.getBytes(keyLength, keyLength + valueLength);
    logger.debug("key is {}", new String(key, StandardCharsets.UTF_8));
    logger.debug("value is {}", new String(value, StandardCharsets.UTF_8));
    cache.set(key, value);
    socket.write(PONG);
  }

  /**
   * 读取key并get
   * @param keyBuffer
   * @param keyLength
   * @param socket
   */
  private void readKeyAndGet(Buffer keyBuffer, int keyLength, NetSocket socket) {
    byte[] key = keyBuffer.getBytes(0, keyLength);
    logger.debug("key is {}", new String(key, StandardCharsets.UTF_8));
    byte[] value = cache.get(key);
    if(null != value) {
      socket.write(new String(value, StandardCharsets.UTF_8));
    } else {
      socket.write(PONG);
    }
  }

  /**
   * 读取key并del
   * @param keyBuffer
   * @param keyLength
   * @param socket
   */
  private void readKeyAndDel(Buffer keyBuffer, int keyLength, NetSocket socket) {
    byte[] key = keyBuffer.getBytes(0, keyLength);
    logger.debug("key is {}", new String(key, StandardCharsets.UTF_8));
    cache.del(key);
    socket.write(PONG);
  }

  public void shutDownTcpServer() {
    if (null != netServer) {
      netServer.close()
        .onSuccess(success -> logger.info("tcp server shutdown successful"))
        .onFailure(err -> logger.error("tcp server shutdown failed"));
    }
  }
}
