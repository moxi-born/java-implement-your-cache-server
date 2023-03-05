package com.moxib.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.moxib.client.validator.Regex;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;

import java.nio.charset.StandardCharsets;

public class CacheClient {

  @Parameter(names = "-h", description = "cache server host", required = true , order = 0)
  private String host;

  @Parameter(names = "-p",  description = "cache server port", required = true, order = 1)
  private int port;

  @Regex("S|G|D")
  @Parameter(names = "-c", description = "command G for get,S for set,D for delete", required = true, order = 2)
  private String command;

  @Parameter(names = "-k", description = "key", required = true, order = 3)
  private String key;

  @Parameter(names = "-v", description = "value", order = 4)
  private String value;

  @Parameter(names = "--help", description = "help", help = true)
  private boolean help;

  public static void main(String[] args) {
    CacheClient client = new CacheClient();
    JCommander jCommander = JCommander.newBuilder().addObject(client).build();
    try {
      jCommander.parse(args);
      client.run(jCommander);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      jCommander.usage();
      System.exit(1);
    }
  }

  private void run(JCommander jCommander) {
    if (help) {
      jCommander.usage();
      System.exit(0);
    } else {
      Vertx vertx = Vertx.vertx();
      NetClientOptions options = new NetClientOptions().setConnectTimeout(1000);
      NetClient netClient = Vertx.vertx().createNetClient(options);
      netClient.connect(port, host)
        .onSuccess(socket -> {
          Buffer buffer = Buffer.buffer(command);
          switch (command) {
            case "S":
              buffer.appendInt(key.length()).appendString(" ")
                .appendInt(value.length()).appendString(" ")
                .appendString(key).appendString(value);
              break;
            case "G":
            case "D":
              buffer.appendInt(key.length()).appendString(" ").appendString(key);
              break;
          }
          socket.write(buffer);
          socket.handler(res -> {
            jCommander.getConsole().println(res.toString(StandardCharsets.UTF_8));
            vertx.close();
            System.exit(0);
          });
        })
        .onFailure(err -> {
          vertx.close();
          System.exit(1);
        });
    }
  }
}
