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

import com.sun.jna.ptr.*;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds a key to a cluster.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Supposing you want to add a key(the name is "key"), you could write this as:
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
 * <p>You shoule see only the "true" message on the stdout if you could successfully run it. Before
 * running the code above, You should run three processes.
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
public class SetCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(SetCmd.class);

  /* -- private instance members -- */
  /** key */
  private final String key;
  /** val */
  private final String val;
  /** clear subkeys */
  private boolean isClearSubkeys = DEFAULT_IS_CLEAR_SUBKEYS;
  /** pass */
  private String pass = DEFAULT_PASS;
  /** expiration */
  private long expirationDuration = DEFAULT_EXPIRATION_DURATION;

  /* -- Constructors -- */
  /**
   * Constructs a SetCmd instance.
   *
   * @param key a key string
   * @param val a val string
   * @return a SetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static SetCmd of(String key, String val) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (val == null || val.isEmpty()) {
      throw new IllegalArgumentException("val is null");
    }
    return SetCmd.of(key, val, false, null, 0L);
  }

  /**
   * Constructs a SetCmd instance.
   *
   * @param key a key string
   * @param val a val string
   * @param isClearSubkeys <code>true</code> clears subkeys. <code>false</code> otherwise.
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   * @return a SetCmd instance.
   */
  public static SetCmd of(
      String key, String val, boolean isClearSubkeys, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (val == null || val.isEmpty()) {
      throw new IllegalArgumentException("val is null");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new SetCmd(key, val, isClearSubkeys, pass, expirationDuration);
  }

  /**
   * Constructs a SetCmd instance.
   *
   * @param key a key string
   * @param val a val string
   * @param isClearSubkeys <code>true</code> clears subkeys. <code>false</code> otherwise.
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  private SetCmd(
      String key, String val, boolean isClearSubkeys, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (val != null && !val.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.val = val;
    this.isClearSubkeys = isClearSubkeys;
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /* -- Instance methods -- */
  /**
   * Updates the value of a key
   *
   * @param session a session instance
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

    // set
    boolean isRemoveSubkeyList = this.isClearSubkeys;
    String password = this.pass;
    // set
    LongByReference lref = new LongByReference(this.expirationDuration);

    boolean isSuccess =
        INSTANCE.k2hdkc_pm_set_str_value_wa(
            session.getHandle(),
            this.key,
            this.val,
            isRemoveSubkeyList,
            password,
            (this.expirationDuration != 0L) ? lref : null);
    if (!isSuccess) {
      logger.error("INSTANCE.k2hdkc_pm_set_str_value_wa returns false");
    }
    // String cmd, boolean isSuccess, long code, long detailCode) {
    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cSuccess = (T) Boolean.valueOf(isSuccess);
    Result<T> result =
        Result.of(SetCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
    return Optional.of(result);
  }
}
