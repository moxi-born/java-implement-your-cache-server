package com.moxib.cache;

import io.vertx.core.json.JsonObject;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RocksDbCache implements Cache {
  private static final Logger logger = LoggerFactory.getLogger(RocksDbCache.class);

  private Stat stat;
  private RocksDB db;

  public RocksDbCache() {
    new RocksDbCache(new JsonObject());
  }

  public RocksDbCache(JsonObject rocksDbConfig) {
    final Options options = new Options();
    options.setCreateIfMissing(true);
    File dbDir = new File(rocksDbConfig.getString("dbPath", "/tmp/rocks-db"),
      rocksDbConfig.getString("dbName", "myCache"));
    try {
      Files.createDirectories(dbDir.getParentFile().toPath());
      Files.createDirectories(dbDir.getAbsoluteFile().toPath());
      db = RocksDB.open(options, dbDir.getAbsolutePath());
    } catch(IOException | RocksDBException ex) {
      logger.error("Error initializing RocksDB, check configurations and permissions, exception: {}, message: {}, stackTrace: {}",
        ex.getCause(), ex.getMessage(), ex.getStackTrace());
    }
    stat = new Stat(0, 0, 0);
    logger.info("RocksDB initialized and ready to use");
  }

  @Override
  public byte[] get(byte[] key) {
    try {
      return db.get(key);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void set(byte[] key, byte[] value) {
    try {
      db.put(key, value);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
    stat.add(key, value);
  }

  @Override
  public void del(byte[] key) {
    byte[] value = get(key);
    if(null != value) {
      stat.del(key, value);
    }
    try {
      db.delete(key);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Stat getStat() {
    return this.stat;
  }
}
