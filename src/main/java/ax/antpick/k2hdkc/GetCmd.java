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
import com.sun.jna.ptr.*;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrievs the value of a key.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Suppose you want to get the value of "key" from a <a
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
 *     try (Cluster c = Cluster.of("cluster/slave.yaml");
 *         Session sess = Session.of(c); ) {
 *       SetCmd set = SetCmd.of("key", "value");
 *       assert (set != null);
 *       assert (set.execute(sess) != null);
 *       assert (set.execute(sess).get().isSuccess());
 *       GetCmd get = GetCmd.of("key");
 *       assert (get != null);
 *       assert (get.execute(sess) != null);
 *       assert (get.execute(sess).get().isSuccess());
 *       String str = (String) get.execute(sess).get().getValue();
 *       assert (str.equals("value"));
 *     } catch (IOException ex) {
 *       System.out.println(ex.getMessage());
 *       assert (false);
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>You shoule see only the "true" and "value" message on the stdout if you could successfully run
 * it. Before running the code above, You should run three processes.
 *
 * <ol>
 *   <li>A chmpx server process
 *   <li>A chmpx slave process
 *   <li>A k2hdkc server process
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
public class GetCmd extends CmdBase implements Cmd<String> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(GetCmd.class);

  /* -- private instance members -- */
  /** The key to retrieve. */
  private final String key;
  /** pass a password string to retrieve the value(optional). */
  private String pass = DEFAULT_PASS;

  /* -- Constructors -- */
  /**
   * Constructs a GetCmd instance.
   *
   * @param key a key string
   * @return a GetCmd instance.
   * @throws IllegalArgumentException if a key string is null or empty
   */
  public static GetCmd of(String key) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new GetCmd(key, null);
  }

  /* -- Constructors -- */
  /**
   * Constructs a GetCmd instance.
   *
   * @param key a key string
   * @param pass a password string to retrieve the value(optional)
   * @return a GetCmd instance.
   * @throws IllegalArgumentException if a key string is null or empty
   */
  public static GetCmd of(String key, String pass) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new GetCmd(key, pass);
  }

  /**
   * Constructs a GetCmd instance.
   *
   * @param key a key string
   * @param pass a password string to retrieve the value(optional)
   * @throws IllegalArgumentException if a key string is null or empty
   */
  private GetCmd(String key, String pass) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    this.key = key;
    this.pass = pass;
  }

  /* -- Instance methods -- */
  /**
   * Retrieves the value of a key
   *
   * @param session a {@link Session} instance
   * @return the result instance
   * @throws IllegalArgumentException if an illegal augment exists
   * @throws IOException if underlying library errors occur.
   */
  @Override
  public <T> Optional<Result<T>> execute(Session session) throws IOException {
    if (session == null) {
      throw new IllegalArgumentException("session should not be null");
    }
    // open
    K2hdkcLibrary INSTANCE = Session.getLibrary(); // throws IOException.
    assert (INSTANCE != null);
    // key
    assert (this.key != null && this.key.length() != 0);

    // get
    PointerByReference ppval = new PointerByReference();
    boolean isSuccess =
        INSTANCE.k2hdkc_pm_get_str_value_wp(session.getHandle(), this.key, this.pass, ppval);
    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());

    if (isSuccess) {
      Pointer p = ppval.getValue();
      if (p != null) {
        String rval = p.getString(0);
        if (rval != null && !rval.isEmpty()) {
          // String cmd, boolean isSuccess, String value, long code, long detailCode) {
          // Suppresses warnings because caller knows which type of T
          @SuppressWarnings("unchecked")
          T cRval = (T) rval;
          Result<T> result =
              Result.of(GetCmd.class.getSimpleName(), isSuccess, cRval, code, detailCode);
          return Optional.of(result);
        }
        logger.warn("INSTANCE.k2hdkc_pm_get_str_value_wp returns null");
        return Optional.empty(); // TODO distinguish empty and error.
      }
      logger.warn("The pointer of INSTANCE.k2h_get_value_wp returns null");
      return Optional.empty(); // TODO distinguish empty and error.
    }
    logger.error("INSTANCE.k2hdkc_pm_get_str_value_wp returns false");
    return Optional.empty(); // TODO distinguish empty and error.
  }
}
