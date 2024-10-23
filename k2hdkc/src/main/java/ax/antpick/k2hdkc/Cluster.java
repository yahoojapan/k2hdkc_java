/*
 * The MIT License
 *
 * Copyright 2018 Yahoo Japan Corporation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * AUTHOR:   Hirotaka Wakabayashi
 * CREATE:   Fri, 14 Sep 2018
 * REVISION:
 *
 */
package ax.antpick.k2hdkc;

import com.sun.jna.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds configurations of a <a href="https://chmpx.antpick.ax/">chmpx</a> slave server.
 *
 * <p>This class has three main roles.
 *
 * <ol>
 *   <li>This class holds settings to connect a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
 *       server.
 *   <li>This class is responsible to control native libraries log levels.
 *   <li>This class encapsulates the {@link K2hdkcLibrary} C API and {@link Cmd} classes.
 * </ol>
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Suppose you want to add a key of "keystring" with a value of "valuestring" to a <a
 * href="https://k2hdkc.antpick.ax/">k2hdkc</a> cluster. You could write this as:
 *
 * <pre>{@code
 * package com.example;
 *
 * import ax.antpick.k2hdkc.*;
 * import com.sun.jna.*;
 * import com.sun.jna.ptr.*;
 * import java.io.IOException;
 * import java.util.*;
 * import java.util.stream.*;
 *
 * public class App {
 *   public static void main(String[] args) {
 *     try (Cluster cluster = Cluster.of("cluster/slave.yaml")) {
 *       cluster.set("keystring", "valstring");
 *       System.out.println(cluster.get("keystring"));
 *     } catch (IOException ex) {
 *       System.out.println(ex.getMessage());
 *       assert (false);
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>Before running the code above, You should run three processes.
 *
 * <ol>
 *   <li>A <a href="https://chmpx.antpick.ax/">chmpx</a> server process
 *   <li>A <a href="https://chmpx.antpick.ax/">chmpx</a> slave process
 *   <li>A <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> server process
 * </ol>
 *
 * <p>The following commands in this repository will run all processes you need in localhost.
 *
 * <pre>{@code
 * $ cd cluster
 * $ sh start_server.sh
 * }</pre>
 *
 * @author Hirotaka Wakabayashi
 */
public class Cluster implements AutoCloseable {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(Cluster.class);
  /** The default file system that is accessible to the JVM. */
  private static final FileSystem fs = FileSystems.getDefault();
  /** Standard C library */
  private static NativeLibrary NATIVE_LIBC_INSTANCE = null;
  /** Standard C library interface */
  private static CLibrary C_INSTANCE = null;
  /** <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> C library */
  private static NativeLibrary NATIVE_LIBK2HDKC_INSTANCE = null;
  /** <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> C library interface */
  private static K2hdkcLibrary INSTANCE = null;
  /** <a href="https://chmpx.antpick.ax/">chmpx</a> C library */
  private static NativeLibrary NATIVE_LIBCHMPX_INSTANCE = null;
  /** <a href="https://chmpx.antpick.ax/">chmpx</a> C library interface */
  private static ChmpxLibrary CHMPX_INSTANCE = null;
  /** <a href="https://k2hash.antpick.ax/">k2hash</a> C library */
  private static NativeLibrary NATIVE_LIBK2HASH_INSTANCE = null;
  /** <a href="https://k2hash.antpick.ax/">k2hash</a> C library interface */
  private static K2hashLibrary K2HASH_INSTANCE = null;

  /* -- public Static members -- */
  /** The default log level */
  public static final Path DEFAULT_NATIVE_LOG_PATH = null;
  /** The default C library log level */
  public static final NativeLogLevel DEFAULT_NATIVE_LOG_LEVEL = NativeLogLevel.SeverityError;
  /** The default stack log level */
  public static final NativeStackLogLevel DEFAULT_NATIVE_STACK_LOG_LEVEL =
      NativeStackLogLevel.StackSilent;
  /**
   * The port a <a href="https://chmpx.antpick.ax/">chmpx</a> slave process listens to control the
   * process
   */
  public static final short DEFAULT_PORT = 8031;
  /**
   * The cuk(Cloud Unique Key) a <a href="https://chmpx.antpick.ax/">chmpx</a> slave process's cuk
   */
  public static final String DEFAULT_CUK = "";
  /**
   * {@code true} if reconnecting with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave process
   * after unexpected closing a connection.
   */
  public static final boolean DEFAULT_REJOIN = true;
  /** Default retry rejoin */
  public static final boolean DEFAULT_RETRY_REJOIN_FOREVER = true;
  /** Default clean up */
  public static final boolean DEFAULT_CLEANUP = true;
  /** Defines levels used to dump a k2hdkc data */
  public enum NativeLogLevel {
    /**
     * the level of the importance of the message is the "silent" level which is the highest level
     */
    SeveritySilent,
    /** the level of the importance of the message is the "error" level */
    SeverityError,
    /** the level of the importance of the message is the "warning" level */
    SeverityWarning,
    /** the level of the importance of the message is the "info" level */
    SeverityInfo,
    /** the level of the importance of the message is the "dump" level which is the lowest level */
    SeverityDump
  }

  /** Defines the level which native library puts logs. */
  public enum NativeStackLogLevel {
    /** no native library loggers are enabled in this level */
    StackSilent,
    /**
     * <a href="https://k2hash.antpick.ax/">k2hash</a>, <a
     * href="https://chmpx.antpick.ax/">chmpx</a>, <a href="https://k2hdkc.antpick.ax/">k2hdkc</a>
     * and communication loggers are enabled in this level
     */
    StackComlog,
    /**
     * <a href="https://k2hash.antpick.ax/">k2hash</a>, <a
     * href="https://chmpx.antpick.ax/">chmpx</a> and <a
     * href="https://k2hdkc.antpick.ax/">k2hdkc</a> library loggers are enabled in this level
     */
    StackK2hdkc,
    /**
     * <a href="https://k2hash.antpick.ax/">k2hash</a> and <a
     * href="https://chmpx.antpick.ax/">chmpx</a> library loggers are enabled in this level
     */
    StackChmpx,
    /** <a href="https://k2hash.antpick.ax/">k2hash</a> library logger is enabled in this level */
    StackK2hash
  }

  /* -- private instance members -- */
  /** The <a href="https://chmpx.antpick.ax/">chmpx</a> slave configuration file path string */
  private final Path path;
  /** The <a href="https://chmpx.antpick.ax/">chmpx</a> slave process control port */
  private short port = DEFAULT_PORT;
  /** The <a href="https://chmpx.antpick.ax/">chmpx</a> slave process cuk string */
  private String cuk = DEFAULT_CUK;
  /** The rejoin cluster automatically */
  private boolean isRejoin = DEFAULT_REJOIN;
  /** The rejoin retry cluster automatically */
  private boolean isRetryRejoinForever = DEFAULT_RETRY_REJOIN_FOREVER;
  /** The clear configuration backup */
  private boolean isCleanup = DEFAULT_CLEANUP;
  /** The log level */
  private NativeLogLevel nativeLogLevel = DEFAULT_NATIVE_LOG_LEVEL;
  /** The stack log level */
  private NativeStackLogLevel nativeStackLogLevel = DEFAULT_NATIVE_STACK_LOG_LEVEL;

  /**
   * Returns full of members as a string.
   *
   * @return full of members as a string in a key=value manner
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getName());
    sb.append("[");
    for (Field field : this.getClass().getDeclaredFields()) {
      sb.append(field.getName());
      sb.append("=");
      try {
        sb.append(field.get(this));
      } catch (IllegalArgumentException | IllegalAccessException ex) {
        sb.append("null");
      }
      sb.append(",");
    }
    sb.append("]");
    return sb.toString();
  }

  /* -- Constructors -- */
  /**
   * Creates a {@code Cluster} instance.
   *
   * @param pathname a <a href="https://chmpx.antpick.ax/">chmpx</a> slave server configuration file
   *     path string
   * @param port a <a href="https://chmpx.antpick.ax/">chmpx</a> control port
   * @param cuk a <a href="https://chmpx.antpick.ax/">chmpx</a> slave cuk(Cloud Unique Key) string
   * @param isRejoin <code>true</code> if try connecting once after failing to connect with a
   *     cluster
   * @param isRetryRejoinForever <code>true</code> if try connecting with a <a
   *     href="https://chmpx.antpick.ax/">chmpx</a> slave server forever after failure of connection
   *     with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave server
   * @param isCleanup <code>true</code> if backup configuration files remove when leaveing from a
   *     cluster
   */
  private Cluster(
      Path pathname,
      short port,
      String cuk,
      boolean isRejoin,
      boolean isRetryRejoinForever,
      boolean isCleanup) {
    assert (pathname != null && !pathname.toString().isEmpty());
    assert (pathname.toFile().exists());
    assert (port >= 0);
    this.path = pathname;
    this.port = port;
    this.cuk = cuk;
    this.isRejoin = isRejoin;
    this.isRetryRejoinForever = isRetryRejoinForever;
    this.isCleanup = isCleanup;
  }

  /* -- public Static methods -- */
  /**
   * Creates a {@code Cluster} instance.
   *
   * @param path a <a href="https://chmpx.antpick.ax/">chmpx</a> slave server configuration file
   *     path string
   * @return a Cluster instance
   * @throws IllegalArgumentException if pathname should not be null
   * @throws IOException if the <a href="https://chmpx.antpick.ax/">chmpx</a> slave configuration
   *     file open failed
   */
  public static synchronized Cluster of(String path) throws IOException {
    return Cluster.of(
        path,
        DEFAULT_PORT,
        DEFAULT_CUK,
        DEFAULT_REJOIN,
        DEFAULT_RETRY_REJOIN_FOREVER,
        DEFAULT_CLEANUP);
  }

  /**
   * Creates a {@code Cluster} instance.
   *
   * @param path a <a href="https://chmpx.antpick.ax/">chmpx</a> slave server configuration file
   *     path string
   * @param port a <a href="https://chmpx.antpick.ax/">chmpx</a> control port
   * @return a Cluster instance
   * @throws IllegalArgumentException if pathname should not be null
   * @throws IOException if the <a href="https://chmpx.antpick.ax/">chmpx</a> slave configuration
   *     file open failed
   */
  public static synchronized Cluster of(String path, short port) throws IOException {
    return Cluster.of(
        path, port, DEFAULT_CUK, DEFAULT_REJOIN, DEFAULT_RETRY_REJOIN_FOREVER, DEFAULT_CLEANUP);
  }

  /**
   * Creates a {@code Cluster} instance.
   *
   * @param path a <a href="https://chmpx.antpick.ax/">chmpx</a> slave server configuration file
   *     path string
   * @param port a <a href="https://chmpx.antpick.ax/">chmpx</a> control port
   * @param cuk a <a href="https://chmpx.antpick.ax/">chmpx</a> slave cuk(Cloud Unique Key) string
   * @return a Cluster instance
   * @throws IllegalArgumentException if pathname should not be null
   * @throws IOException if the <a href="https://chmpx.antpick.ax/">chmpx</a> slave configuration
   *     file open failed
   */
  public static synchronized Cluster of(String path, short port, String cuk) throws IOException {
    return Cluster.of(
        path, port, cuk, DEFAULT_REJOIN, DEFAULT_RETRY_REJOIN_FOREVER, DEFAULT_CLEANUP);
  }

  /**
   * Creates a {@code Cluster} instance.
   *
   * @param pathname a <a href="https://chmpx.antpick.ax/">chmpx</a> slave server configuration file
   *     path string
   * @param port a <a href="https://chmpx.antpick.ax/">chmpx</a> control port
   * @param cuk a <a href="https://chmpx.antpick.ax/">chmpx</a> slave cuk(Cloud Unique Key) string
   * @param isRejoin <code>true</code> if try connecting once after failing to connect with a
   *     cluster
   * @param isRetryRejoinForever <code>true</code> if try connecting with a <a
   *     href="https://chmpx.antpick.ax/">chmpx</a> slave server forever after failure of connection
   *     with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave server
   * @param isCleanup <code>true</code> if backup configuration files remove when leaveing from a
   *     cluster
   * @return a Cluster instance
   * @throws IllegalArgumentException if pathname should not be null
   * @throws IOException if the <a href="https://chmpx.antpick.ax/">chmpx</a> slave configuration
   *     file open failed
   */
  public static synchronized Cluster of(
      String pathname,
      short port,
      String cuk,
      boolean isRejoin,
      boolean isRetryRejoinForever,
      boolean isCleanup)
      throws IOException {
    if (pathname == null || pathname.isEmpty()) {
      throw new IllegalArgumentException("pathname should not be null or empty");
    }
    Path path = fs.getPath(pathname).toAbsolutePath();
    if (!path.toFile().exists()) {
      logger.error("pathname {} doesnt exist", path.toAbsolutePath());
      throw new IOException("pathname " + path.toAbsolutePath() + "doesn't exist");
    }
    if (port < 0) {
      throw new IllegalArgumentException("port is negative. should be positive");
    }

    // VM Crash Protection
    // https://java-native-access.github.io/jna/4.5.2/javadoc/overview-summary.html#crash-protection
    if (Native.isProtected()) {
      Native.setProtected(true);
    }

    // c library
    NATIVE_LIBC_INSTANCE = NativeLibrary.getInstance("c");
    if (NATIVE_LIBC_INSTANCE != null && C_INSTANCE == null) {
      C_INSTANCE = Native.load(CLibrary.class);
    }
    if (NATIVE_LIBC_INSTANCE == null || C_INSTANCE == null) {
      throw new IOException("can't load c library");
    }
    // k2hdkc library
    NATIVE_LIBK2HDKC_INSTANCE = NativeLibrary.getInstance("k2hdkc");
    if (NATIVE_LIBK2HDKC_INSTANCE != null && INSTANCE == null) {
      INSTANCE = Native.load(K2hdkcLibrary.class);
    }
    if (NATIVE_LIBK2HDKC_INSTANCE == null || INSTANCE == null) {
      throw new IOException("can't load k2hdkc library");
    }
    // chmpx library
    NATIVE_LIBCHMPX_INSTANCE = NativeLibrary.getInstance("chmpx");
    if (NATIVE_LIBCHMPX_INSTANCE != null && CHMPX_INSTANCE == null) {
      CHMPX_INSTANCE = Native.load(ChmpxLibrary.class);
    }
    if (NATIVE_LIBCHMPX_INSTANCE == null && CHMPX_INSTANCE == null) {
      throw new IOException("can't load chmpx library");
    }
    // k2hash library
    NATIVE_LIBK2HASH_INSTANCE = NativeLibrary.getInstance("k2hash");
    if (NATIVE_LIBK2HASH_INSTANCE != null && K2HASH_INSTANCE == null) {
      K2HASH_INSTANCE = Native.load("k2hash", K2hashLibrary.class);
    }
    if (NATIVE_LIBK2HASH_INSTANCE == null || K2HASH_INSTANCE == null) {
      throw new IOException("can't load k2hash library");
    }
    return new Cluster(path, port, cuk, isRejoin, isRetryRejoinForever, isCleanup);
  }

  /**
   * Enables the communication log.
   *
   * @param isEnable {@code true} if enables
   */
  private void enableComLog(boolean isEnable) {
    if (isEnable) {
      if (Objects.requireNonNull(this.nativeLogLevel) == NativeLogLevel.SeveritySilent) {
          INSTANCE.k2hdkc_disable_comlog();
      } else {
          INSTANCE.k2hdkc_enable_comlog();
      }
    } else {
      INSTANCE.k2hdkc_disable_comlog();
    }
  }

  /**
   * Enables the <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> C library log.
   *
   * @param isEnable {@code true} if enables
   */
  private void enableK2hdkcLog(boolean isEnable) {
    if (isEnable) {
      switch (this.nativeLogLevel) {
        case SeveritySilent:
          INSTANCE.k2hdkc_set_debug_level_silent();
          break;
          case SeverityWarning:
          INSTANCE.k2hdkc_set_debug_level_warning();
          break;
        case SeverityInfo:
          INSTANCE.k2hdkc_set_debug_level_message();
          break;
        case SeverityDump:
          INSTANCE.k2hdkc_set_debug_level_dump();
          break;
        default:
          INSTANCE.k2hdkc_set_debug_level_error();
          break;
      }
    } else {
      INSTANCE.k2hdkc_set_debug_level_silent();
    }
  }

  /**
   * Enables the <a href="https://chmpx.antpick.ax/">chmpx</a> C library log.
   *
   * @param isEnable {@code true} if enables
   */
  private void enableChmpxLog(boolean isEnable) {
    if (isEnable) {
      switch (this.nativeLogLevel) {
        case SeveritySilent:
          CHMPX_INSTANCE.chmpx_set_debug_level_silent();
          break;
          case SeverityWarning:
          CHMPX_INSTANCE.chmpx_set_debug_level_warning();
          break;
        case SeverityInfo:
          CHMPX_INSTANCE.chmpx_set_debug_level_message();
          break;
        case SeverityDump:
          CHMPX_INSTANCE.chmpx_set_debug_level_dump();
          break;
        default:
          CHMPX_INSTANCE.chmpx_set_debug_level_error();
          break;
      }
    } else {
      INSTANCE.k2hdkc_set_debug_level_silent();
    }
  }

  /**
   * Enables the <a href="https://k2hash.antpick.ax/">k2hash</a> C library log.
   *
   * @param isEnable {@code true} if enables
   */
  private void enableK2hashLog(boolean isEnable) {
    if (isEnable) {
      switch (this.nativeLogLevel) {
        case SeveritySilent:
          K2HASH_INSTANCE.k2h_set_debug_level_silent();
          break;
          case SeverityWarning:
          K2HASH_INSTANCE.k2h_set_debug_level_warning();
          break;
        case SeverityInfo:
          case SeverityDump:
              K2HASH_INSTANCE.k2h_set_debug_level_message();
          break;
          // no k2h_set_debug_level_dump function
          default:
          K2HASH_INSTANCE.k2h_set_debug_level_error();
          break;
      }
    } else {
      INSTANCE.k2hdkc_set_debug_level_silent();
    }
  }

  /**
   * Closes(Disposes) native libraries called by JNA.
   *
   */
  @Override
  public void close() {
    INSTANCE = null;
    CHMPX_INSTANCE = null;
    K2HASH_INSTANCE = null;
    C_INSTANCE = null;

    NATIVE_LIBK2HDKC_INSTANCE.close();
    NATIVE_LIBCHMPX_INSTANCE.close();
    NATIVE_LIBK2HASH_INSTANCE.close();
    NATIVE_LIBC_INSTANCE.close();
  }

  /**
   * Returns a <a href="https://chmpx.antpick.ax/">chmpx</a> cluster configuration file path string
   *
   * @return a <a href="https://chmpx.antpick.ax/">chmpx</a> cluster configuration file path string
   */
  public String getPath() {
    return this.path.toString();
  }

  /**
   * Returns a <a href="https://chmpx.antpick.ax/">chmpx</a> slave control port
   *
   * @return a <a href="https://chmpx.antpick.ax/">chmpx</a> slave control port
   */
  public short getPort() {
    return this.port;
  }

  /**
   * Returns a <a href="https://chmpx.antpick.ax/">chmpx</a> slave cuk(Cloud Unique Key) string
   *
   * @return a <a href="https://chmpx.antpick.ax/">chmpx</a> slave cuk(Cloud Unique Key) string
   */
  public String getCuk() {
    return this.cuk;
  }

  /**
   * Returns a flag to rejoin cluster automatically
   *
   * @return {@code true} if try connecting once after failing to connect with a cluster
   */
  public boolean isRejoin() {
    return this.isRejoin;
  }

  /**
   * Returns a flag to do retry to rejoin cluster automatically
   *
   * @return {@code true} if try connecting with a <a href="https://chmpx.antpick.ax/">chmpx</a>
   *     slave server forever after failure of connection with a <a
   *     href="https://chmpx.antpick.ax/">chmpx</a> slave server
   */
  public boolean isRetryRejoinForever() {
    return this.isRetryRejoinForever;
  }

  /**
   * Returns a flag to clear configuration backup
   *
   * @return {@code true} if backup configuration files remove when leaveing from a cluster
   */
  public boolean isCleanup() {
    return this.isCleanup;
  }

  /**
   * Retrieves the value of a key. If you want to set attributes(e.g. password and expirationDuration
   * etc.), please use {@link GetCmd} class. Please note this method returns empty object if cluster
   * returns empty result.
   *
   * @param key a key string
   * @return the value of a key.
   * @throws IllegalArgumentException if an illegal augment exists
   * @throws IOException if underlying library errors occur.
   */
  public <T> Optional<T> get(String key) throws IOException {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException(
          "key should not be null or empty or empty. key.length() must be greater than equal zero.");
    }
    GetCmd cmd = GetCmd.of(key);
    try (Session sess = Session.of(this)) {
      Optional<Result<T>> result = cmd.execute(sess);
      if (result.isPresent()) {
        T val = result.get().getValue();
        return Optional.of(val);
      } else {
        logger.error("{} returns no result elements", cmd.getClass());
      }
      return Optional.empty(); // empty
    } catch (IOException ex) {
      logger.error(ex.getMessage());
      throw ex;
    }
  }

  /**
   * Sets the value of a key. If you want to set attributes(e.g. password and expirationDuration
   * etc.), please use {@link SetCmd} class.
   *
   * @param key a key string
   * @param val a value string
   * @return <code>true</code> if success. <code>false</code> otherwise
   * @throws IllegalArgumentException if an illegal augment exists
   * @throws IOException if underlying library errors occur.
   */
  public boolean set(String key, String val) throws IOException {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException(
          "key should not be null or empty or empty. key.length() must be greater than equal zero.");
    }
    if (val == null || val.isEmpty()) {
      throw new IllegalArgumentException(
          "val is null or empty. valy.length() must be greater than equal zero.");
    }
    SetCmd cmd = SetCmd.of(key, val);
    try (Session sess = Session.of(this)) {
      Optional<Result<Boolean>> result = cmd.execute(sess);
      if (result.isPresent()) {
        boolean isSuccess = result.get().isSuccess();
        if (!isSuccess) {
          logger.error("{} failed", cmd.getClass());
        }
        return isSuccess;
      } else {
        logger.error("empty result");
      }
      return false;
    } catch (IOException ex) {
      logger.error(ex.getMessage());
      throw ex;
    }
  }

  /**
   * Removes the value of a key.
   *
   * @param key a key string
   * @return <code>true</code> if success. <code>false</code> otherwise
   * @throws IllegalArgumentException if an illegal augment exists
   * @throws IOException if underlying library errors occur.
   */
  public boolean remove(String key) throws IOException {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException(
          "key should not be null or empty or empty. key.length() must be greater than equal zero.");
    }
    RemoveCmd cmd = RemoveCmd.of(key);
    try (Session sess = Session.of(this)) {
      Optional<Result<Boolean>> result = cmd.execute(sess);
      if (result.isPresent()) {
        boolean isSuccess = result.get().isSuccess();
        if (!isSuccess) {
          logger.error("RemoveCmd failed");
        }
        return isSuccess;
      }
      return false;
    } catch (IOException ex) {
      logger.error(ex.getMessage());
      throw ex;
    }
  }

  /**
   * Sets a subkey of a key.
   *
   * @param key a key string
   * @param subkeys an array of subkey strings
   * @return <code>true</code> if success. <code>false</code> otherwise
   * @throws IllegalArgumentException if an illegal augment exists
   * @throws IOException if underlying library errors occur.
   */
  public boolean setSubkeys(String key, String[] subkeys) throws IOException {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException(
          "key should not be null or empty or empty. key.length() must be greater than equal zero.");
    }
    if (subkeys == null || subkeys.length == 0) {
      throw new IllegalArgumentException(
          "val is null or empty. valy.length() must be greater than equal zero.");
    }
    SetSubkeysCmd cmd = SetSubkeysCmd.of(key, subkeys);
    try (Session sess = Session.of(this)) {
      Optional<Result<Boolean>> result = cmd.execute(sess);
      if (result.isPresent()) {
        boolean isSuccess = result.get().isSuccess();
        if (!isSuccess) {
          logger.error("SetSubkeysCmd failed");
        }
        return isSuccess;
      } else {
        logger.error("empty result");
        return false;
      }
    } catch (IOException ex) {
      logger.error(ex.getMessage());
      throw ex;
    } catch (NoSuchElementException ex) {
      logger.error("{} returns no result elements {}", cmd.getClass(), ex.getMessage());
      return false;
    }
  }

  /**
   * Retrieves the subkeys of a key.
   *
   * @param key a key string
   * @return a list of subkeys
   * @throws IllegalArgumentException if an illegal augment exists
   * @throws IOException if underlying library errors occur.
   */
  public List<String> getSubkeys(String key) throws IOException {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException(
          "key should not be null or empty or empty. key.length() must be greater than equal zero.");
    }
    GetSubkeysCmd cmd = GetSubkeysCmd.of(key);
    try (Session sess = Session.of(this)) {
      Optional<Result<List<String>>> result = cmd.execute(sess);
      if (result.isPresent()) {
        List<String> list = result.get().getValue();
        return list;
      } else {
        logger.error("{} returns no result elements", cmd.getClass());
      }
      return new ArrayList<>(); // returns empty list
    } catch (IOException ex) {
      logger.error(ex.getMessage());
      throw ex;
    }
  }

  /**
   * Clear subkeys of a key. Another subkeys that a subkey has will be removed recursively.
   *
   * @param key a key string
   * @return <code>true</code> if success. <code>false</code> otherwise.
   * @throws IllegalArgumentException if an illegal augment exists
   * @throws IOException if underlying library errors occur.
   */
  public boolean clearSubkeys(String key) throws IOException {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException(
          "key should not be null or empty or empty. key.length() must be greater than equal zero.");
    }
    ClearSubkeysCmd cmd = ClearSubkeysCmd.of(key);
    try (Session sess = Session.of(this)) {
      Optional<Result<Boolean>> result = cmd.execute(sess);
      if (result.isPresent()) {
        if (!result.get().isSuccess()) {
          logger.error("{} failed {}", cmd.getClass(), cmd);
          return false;
        }
        return true;
      }
      return false;
    } catch (IOException ex) {
      logger.error(ex.getMessage());
      throw ex;
    }
  }

  /** Initializes log files on each native stack. */
  public void initNativeLog() {
    // 1. init output file
    boolean isSuccess = INSTANCE.k2hdkc_unset_debug_file();
    if (!isSuccess) {
      logger.error("INSTANCE.k2hdkc_unset_debug_file returns false");
    }
    isSuccess = CHMPX_INSTANCE.chmpx_unset_debug_file();
    if (!isSuccess) {
      logger.error("CHMPX_INSTANCE.chmpx_unset_debug_file returns false");
    }
    isSuccess = K2HASH_INSTANCE.k2h_unset_debug_file();
    if (!isSuccess) {
      logger.error("K2HASH_INSTANCE.k2h_unset_debug_file returns false");
    }
    // 2. init native stack level
    this.nativeStackLogLevel = DEFAULT_NATIVE_STACK_LOG_LEVEL;
    // 3. init native level
    this.nativeLogLevel = DEFAULT_NATIVE_LOG_LEVEL;
  }

  /**
   * Enables logging on each native stack.
   *
   * @param pathname a log file string to put logs.
   * @param stackLogLevel a log level of a native libary stack
   * @param logLevel a log level of the native libary
   * @return {@code true} if success. {@code false} otherwise.
   * @throws IllegalArgumentException if an illegal augment exists
   */
  public boolean setNativeLogLevel(
      String pathname, NativeStackLogLevel stackLogLevel, NativeLogLevel logLevel) {

    // 1. output file
    boolean isSuccess;
    if (pathname == null || pathname.isEmpty()) {
      throw new IllegalArgumentException("file is empty");
    }
    Path absPath = fs.getPath(pathname).toAbsolutePath();
    isSuccess = INSTANCE.k2hdkc_set_debug_file(absPath.toString());
    if (!isSuccess) {
      logger.error("INSTANCE.k2hdkc_set_debug_file returns false");
      return false;
    }
    isSuccess = CHMPX_INSTANCE.chmpx_set_debug_file(absPath.toString());
    if (!isSuccess) {
      logger.error("CHMPX_INSTANCE.chmpx_set_debug_file returns false");
      return false;
    }
    isSuccess = K2HASH_INSTANCE.k2h_set_debug_file(absPath.toString());
    if (!isSuccess) {
      logger.error("K2HASH_INSTANCE.k2h_set_debug_file returns false");
      return false;
    }

    logger.info(
        "old loglevel: stackLogLevel {} nativeLogLevel {}",
        this.nativeStackLogLevel.name(),
        this.nativeLogLevel.name());

    this.nativeStackLogLevel = stackLogLevel;
    this.nativeLogLevel = logLevel;

    logger.info(
        "new loglevel: stackLogLevel {} nativeLogLevel {}",
        this.nativeStackLogLevel.name(),
        this.nativeLogLevel.name());

    if (this.nativeStackLogLevel == NativeStackLogLevel.StackComlog) {
      this.enableComLog(true);
    }
    if (this.nativeStackLogLevel == NativeStackLogLevel.StackK2hdkc) {
      this.enableComLog(true);
      this.enableK2hdkcLog(true);
    }
    if (this.nativeStackLogLevel == NativeStackLogLevel.StackChmpx) {
      this.enableComLog(true);
      this.enableK2hdkcLog(true);
      this.enableChmpxLog(true);
    }
    if (this.nativeStackLogLevel == NativeStackLogLevel.StackK2hash) {
      this.enableComLog(true);
      this.enableK2hdkcLog(true);
      this.enableChmpxLog(true);
      this.enableK2hashLog(true);
    }
    if (this.nativeStackLogLevel == NativeStackLogLevel.StackSilent) {
      this.enableComLog(false);
      this.enableK2hdkcLog(false);
      this.enableChmpxLog(false);
      this.enableK2hashLog(false);
    }

    return true;
  }
}

//
// Local variables:
// tab-width: 2
// c-basic-offset: 2
// indent-tabs-mode: nil
// End:
// vim600: noexpandtab sw=2 ts=2 fdm=marker
// vim<600: noexpandtab sw=2 ts=2
//
