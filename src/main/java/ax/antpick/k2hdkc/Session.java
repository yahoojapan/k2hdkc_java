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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds a {@link Cluster} object and a connection handle with a <a
 * href="https://chmpx.antpick.ax/">chmpx</a> slave process.
 *
 * @author Hirotaka Wakabayashi
 *     <p><b>An Usage Example:</b>
 *     <p>Following code will open a connection with a <a href="https://chmpx.antpick.ax/">chmpx</a>
 *     slave process and close it.
 *     <pre>{@code
 * import ax.antpick.k2hdkc.*;
 * import com.sun.jna.*;
 * import com.sun.jna.ptr.*;
 * import java.io.IOException;
 *
 * public class App {
 *     private static final String CHMPX_SLAVE_CONFIG = "cluster/slave.yaml";
 *     public static void main(String[] args) {
 *     try (Cluster c = Cluster.of(CHMPX_SLAVE_CONFIG);
 *        Session s = Session.of(c); ) {
 *       System.out.println(s.toString()); // prints session information.
 *     } catch (IOException ex) {
 *       System.out.println(ex.getMessage());
 *       assert (false);
 *     }
 *   }
 * }
 * }</pre>
 *     <p>Before running the code above, You should run three processes.
 *     <ol>
 *       <li>A chmpx server process
 *       <li>A chmpx slave process
 *       <li>A k2hdkc server process
 *     </ol>
 *     <p>The following commands in this repository will run all processes you need in localhost.
 *     <pre>{@code
 * $ cd cluster
 * $ sh start_server.sh
 * }</pre>
 *
 * @author Hirotaka Wakabayashi
 */
public class Session implements AutoCloseable {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(Session.class);
  /** The default file system that is accessible to the JVM. */
  private static final FileSystem fs = FileSystems.getDefault();
  /** A <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> shared library instance */
  private static K2hdkcLibrary INSTANCE = null;
  /** A <a href="https://k2hash.antpick.ax/">k2hash</a> shared library insrtance */
  private static K2hashLibrary K2HASH_INSTANCE = null;

  /* -- public Static members -- */
  /** <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> default handle */
  public static final long K2HDKC_INVALID_HANDLE = 0L;
  /** <a href="https://k2hash.antpick.ax/">k2hash</a> default handle */
  public static final long K2HASH_INVALID_HANDLE = 0L;

  /* -- private instance members -- */
  /** A {@link Cluster} object */
  private final Cluster cluster;
  /** A <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> cluster handle */
  private long handle = K2HDKC_INVALID_HANDLE;

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
   * Creates a Session instance.
   *
   * @param cluster a {@link Cluster} instance
   * @throws IllegalArgumentException if pathname should not be null
   * @throws IOException if a <a href="https://chmpx.antpick.ax/">chmpx</a> configuration file is
   *     unavailable
   */
  private Session(Cluster cluster) throws IOException {
    assert (INSTANCE != null);
    if (cluster == null) {
      throw new IllegalArgumentException("cluster is null");
    }
    this.cluster = cluster;

    // open
    this.handle =
        INSTANCE.k2hdkc_open_chmpx_full(
            this.cluster.getPath(),
            this.cluster.getPort(),
            this.cluster.getCuk(),
            this.cluster.isRejoin(),
            this.cluster.isRetryRejoinForever(),
            this.cluster.isCleanup());
    if (this.handle <= K2HDKC_INVALID_HANDLE) {
      throw new IOException("INSTANCE.k2hdkc_open_chmpx_full failed");
    }
  }

  /**
   * Creates a Session instance.
   *
   * @param cluster a <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> cluster instance
   * @return a <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> instance
   * @throws IllegalArgumentException if pathname should not be null
   * @throws IOException if creating a <a href="https://chmpx.antpick.ax/">chmpx</a> configuration
   *     file failed
   */
  public static synchronized Session of(Cluster cluster) throws IOException {
    if (cluster == null) {
      throw new IllegalArgumentException("cluser is null");
    }
    if (INSTANCE == null) {
      INSTANCE =
          (K2hdkcLibrary) Native.synchronizedLibrary(Native.load("k2hdkc", K2hdkcLibrary.class));
    }
    if (INSTANCE == null) {
      throw new IOException("loading shared library error");
    }
    return new Session(cluster);
  }

  /* -- package private static methods -- */
  /**
   * Creates a {@link K2hdkcLibrary} instance.
   *
   * @throws IOException if failed to load the <a href="https://k2hdkc.antpick.ax/">k2hdkc</a>
   *     library
   * @return a {@link K2hdkcLibrary} instance
   */
  static synchronized K2hdkcLibrary getLibrary() throws IOException {
    if (INSTANCE == null) {
      INSTANCE =
          (K2hdkcLibrary) Native.synchronizedLibrary(Native.load("k2hdkc", K2hdkcLibrary.class));
    }
    if (INSTANCE == null) {
      throw new IOException("loading shared library error");
    }
    return INSTANCE;
  }

  /**
   * Creates a {@link K2hashLibrary} instance.
   *
   * @throws IOException if failed to load the <a href="https://k2hash.antpick.ax/">k2hash</a>
   *     library
   * @return a {@link K2hashLibrary} instance
   */
  static synchronized K2hashLibrary getK2hashLibrary() throws IOException {
    if (K2HASH_INSTANCE == null) {
      K2HASH_INSTANCE =
          (K2hashLibrary) Native.synchronizedLibrary(Native.load("k2hash", K2hashLibrary.class));
    }
    if (K2HASH_INSTANCE == null) {
      throw new IOException("loading shared library error");
    }
    return K2HASH_INSTANCE;
  }

  /* -- Instance methods -- */
  /**
   * Returns a <a href="https://k2hash.antpick.ax/">k2hash</a> data handle.
   *
   * @return a <a href="https://k2hash.antpick.ax/">k2hash</a> data handle.
   */
  public long getHandle() {
    assert (this.handle > K2HDKC_INVALID_HANDLE);
    return this.handle;
  }

  /**
   * Closes a connection with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave process.
   *
   * @throws IOException will not be thrown from this class even if k2hdkc_close_chmpx_ex returns
   *     false because it's not critical.
   */
  @Override
  public void close() throws IOException {
    assert (this.handle > K2HDKC_INVALID_HANDLE);
    boolean isSuccess = INSTANCE.k2hdkc_close_chmpx_ex(this.handle, true);
    if (!isSuccess) {
      logger.error("INSTANCE.k2hdkc_close_chmpx_ex returns false {}", this.handle);
    }
    this.handle = K2HDKC_INVALID_HANDLE; // internally mark the resource as closed
  }
}
