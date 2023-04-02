package com.moxib.cluster;

import com.moxib.exception.MemberNotFoundException;
import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterImpl;
import io.scalecube.cluster.ClusterMessageHandler;
import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.cluster.transport.api.Message;
import io.scalecube.net.Address;
import io.scalecube.transport.netty.tcp.TcpTransportFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CacheCluster {
  private static final Logger logger = LoggerFactory.getLogger(CacheCluster.class);
  private Mono<Cluster> cluster;
  private final JsonObject clusterConfig;

  public CacheCluster(JsonObject clusterConfig) {
    this.clusterConfig = clusterConfig;
  }

  public void initCluster() throws MemberNotFoundException {
    JsonArray memberInfo = clusterConfig.getJsonArray("member");
    if(null == memberInfo) {
      throw new MemberNotFoundException();
    }
    String host = clusterConfig.getString("host", "127.0.0.1");
    int port = clusterConfig.getInteger("port",13000);

    List<Address> addressList = memberInfo.stream().map(member -> Address.from((String) member)).collect(Collectors.toList());
    cluster = new ClusterImpl()
      .config(opts -> opts.memberAlias(String.format("CacheCluster-%s-%d", host, port)))
      .config(opts -> opts.transport(transportConfig -> transportConfig.port(port)))
      .membership(opts -> opts.seedMembers(addressList))
      .transportFactory(TcpTransportFactory::new)
      .handler(cluster -> new CacheMessageHandler())
      .start();
    cluster.subscribe(
      success -> logger.info("cache cluster start successful"),
      err -> logger.error("cache cluster start successful", err)
    );
  }

  public void closeCluster() {
    Objects.requireNonNull(cluster.block()).shutdown();
  }

  private static class CacheMessageHandler implements ClusterMessageHandler {
    @Override
    public void onMessage(Message message) {

    }

    @Override
    public void onGossip(Message gossip) {

    }

    @Override
    public void onMembershipEvent(MembershipEvent event) {

    }
  }
}
