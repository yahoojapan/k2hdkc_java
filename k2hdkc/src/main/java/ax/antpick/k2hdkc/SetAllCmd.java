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
 * Stores a key with a value and subkeys. You can encrypt value with a password and expire the data
 * in a certain duration.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>The following code will store a key with a value and two subkeys in a <a
 * href="https://k2hdkc.antpick.ax/">k2hdkc</a> cluster.
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
 *  public static void main(String[] args) {
 *    try (Cluster c = Cluster.of("cluster/slave.yaml");
 *        Session s = Session.of(c); ) {
 *      String[] subkeys = {"subkey1", "subkey2"};
 *      SetAllCmd set = SetAllCmd.of("key", "val", subkeys);
 *      assert (set.execute(s).get().isSuccess());
 *      // 1. get key
 *      GetCmd get = GetCmd.of("key");
 *      String str = (String) get.execute(sess).get().getValue();
 *      System.out.println(str);
 *      // 2. get subkeys
 *      GetSubkeysCmd getsub = GetSubkeysCmd.of("key");
 *      List<String> list = (List<String>) get.execute(sess).get().getValue();
 *      System.out.println(list.get(0)); // subkey1
 *      System.out.println(list.get(1)); // subkey2
 *    } catch (IOException ex) {
 *      System.out.println(ex.getMessage());
 *      assert (false);
 *    }
 *  }
 * }
 * }</pre>
 *
 * <p>You shoule see only the "key", "subkey1" and "subkey2" message on the stdout if you could
 * successfully run it. Before running the code above, You should run three processes.
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
public class SetAllCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(SetAllCmd.class);

  /* -- private instance members -- */
  /** key */
  private final String key;
  /** val */
  private final String val;
  /** subkeys */
  private String[] subkeys = DEFAULT_SUBKEYS;
  /** pass */
  private String pass = DEFAULT_PASS;
  /** expiration */
  private long expirationDuration = DEFAULT_EXPIRATION_DURATION;

  /* -- Constructors -- */
  /**
   * Constructs a SetAllCmd instance.
   *
   * @param key a key string
   * @param val a val string
   * @return a SetAllCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static SetAllCmd of(String key, String val) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (val == null || val.isEmpty()) {
      throw new IllegalArgumentException("val is null");
    }
    return SetAllCmd.of(key, val, DEFAULT_SUBKEYS, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a SetAllCmd instance.
   *
   * @param key a key string
   * @param val a val string
   * @param subkeys an array of subkey strings
   * @return a SetAllCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static SetAllCmd of(String key, String val, String[] subkeys) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (val == null || val.isEmpty()) {
      throw new IllegalArgumentException("val is null");
    }
    return SetAllCmd.of(key, val, subkeys, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a SetAllCmd instance.
   *
   * @param key a key string
   * @param val a val string
   * @param subkeys an array of subkey strings
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a SetAllCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static SetAllCmd of(
      String key, String val, String[] subkeys, String pass, long expirationDuration) {
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
    return new SetAllCmd(key, val, subkeys, pass, expirationDuration);
  }

  /**
   * Constructs a SetAllCmd instance.
   *
   * @param key a key string
   * @param val a val string
   * @param subkeys an array of subkeys
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  private SetAllCmd(
      String key, String val, String[] subkeys, String pass, long expirationDuration) {
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
    this.key = key;
    this.val = val;
    this.subkeys = subkeys;
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /* -- Instance methods -- */
  /**
   * Stores a key with a value and subkeys.
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
    assert (this.key != null && !this.key.isEmpty());

    // set
    LongByReference lref = new LongByReference(this.expirationDuration);

    // set
    StringArray jnaArray = new StringArray(this.subkeys); // null is acceptable.

    boolean isSuccess =
        INSTANCE.k2hdkc_pm_set_str_all_wa(
            session.getHandle(),
            this.key,
            this.val,
            jnaArray,
            this.pass,
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
        Result.of(SetAllCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
    return Optional.of(result);
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
