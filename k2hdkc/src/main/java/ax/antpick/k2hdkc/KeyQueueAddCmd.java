/*
 * The MIT License
 *
 * Copyright 2018 Yahoo Japan Corporation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
n * of this software and associated documentation files (the "Software"), to deal
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
 * Adds a new element to a queue.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Supposing you want to add a new element(the element key name is "key1" ant the value is "val1"
 * to a queue(the queue name is "q1"), then you get(remove) the element from the queue, you could
 * write this as:
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
 *     try (Cluster cluster = Cluster.of("cluster/slave.yaml");
 *         Session sess = Session.of(cluster); ) {
 *       // 1. add an element to this queue
 *       KeyQueueAddCmd qadd = KeyQueueAddCmd.of("testQueueAddCmdArg1", "key1", "val1");
 *       assert (qadd.execute(sess).get().isSuccess());
 *       // 2. remove an element from this queue
 *       KeyQueueRemoveCmd qrm = KeyQueueRemoveCmd.of("testQueueAddCmdArg1");
 *       Map<String, String> r = (Map<String, String>) qrm.execute(sess).get().getValue();
 *       assert (r.containsKey("key1"));
 *       assert (r.containsValue("val1"));
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
public class KeyQueueAddCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(KeyQueueAddCmd.class);

  /* -- private instance members -- */
  /** prefix is an identification of queues. */
  private final String prefix;
  /** key */
  private final String key;
  /** val */
  private final String val;
  /** isFifo */
  private boolean isFifo = DEFAULT_IS_FIFO;
  /** checkParentAttrs */
  private boolean checkParentAttrs = DEFAULT_CHECK_PARENT_ATTRS;
  /** pass */
  private String pass = DEFAULT_PASS;
  /** expiration */
  private long expirationDuration = DEFAULT_EXPIRATION_DURATION;

  /* -- Constructors -- */
  /**
   * Constructs a KeyQueueAddCmd instance.
   *
   * @param prefix a string for an identification of a queue.
   * @param key a key string
   * @param val a valur string
   * @return a KeyQueueAddCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static KeyQueueAddCmd of(String prefix, String key, String val) {
    if (prefix == null || prefix.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new KeyQueueAddCmd(
        prefix,
        key,
        val,
        DEFAULT_IS_FIFO,
        DEFAULT_CHECK_PARENT_ATTRS,
        DEFAULT_PASS,
        DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a KeyQueueAddCmd instance.
   *
   * @param prefix a string for an identification of a queue.
   * @param key a key string
   * @param val a valur string
   * @param isFifo <code> true </code> if the first element which comes in will go out. <code>
   *     false</code> otherwise.
   * @param checkParentAttrs <code>true</code> if checking a parent attribute before changing a
   *     subkeys. <code>false</code> otherwise.
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a KeyQueueAddCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static KeyQueueAddCmd of(
      String prefix,
      String key,
      String val,
      boolean isFifo,
      boolean checkParentAttrs,
      String pass,
      long expirationDuration) {
    if (prefix == null || prefix.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new KeyQueueAddCmd(prefix, key, val, isFifo, checkParentAttrs, pass, expirationDuration);
  }

  /**
   * Constructs a KeyQueueAddCmd instance.
   *
   * @param prefix a prefix string
   * @param key a key string
   * @param val a val string
   * @param isFifo <code> true </code> if the first element which comes in will go out. <code>
   *     false</code> otherwise.
   * @param checkParentAttrs <code>true</code> if checking a parent attribute before changing a
   *     subkeys. <code>false</code> otherwise.
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  private KeyQueueAddCmd(
      String prefix,
      String key,
      String val,
      boolean isFifo,
      boolean checkParentAttrs,
      String pass,
      long expirationDuration) {
    // null && empty
    assert (prefix != null && !prefix.isEmpty());
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.prefix = prefix;
    this.key = key;
    this.val = val;
    this.isFifo = isFifo;
    this.checkParentAttrs = checkParentAttrs;
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /* -- Instance methods -- */
  /**
   * Adds an element to this queue.
   *
   * @param session a Session instance
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
    // prefix
    assert (this.prefix != null && !this.prefix.isEmpty());
    // key
    assert (this.key != null && !this.key.isEmpty());

    boolean isSuccess;
    LongByReference lref = new LongByReference(this.expirationDuration);
    isSuccess =
        INSTANCE.k2hdkc_pm_keyq_str_push_wa(
            session.getHandle(),
            this.prefix,
            this.key,
            this.val,
            this.isFifo,
            this.checkParentAttrs,
            this.pass,
            (this.expirationDuration != 0) ? lref : null);
    if (!isSuccess) {
      logger.error("INSTANCE.k2hdkc_pm_keyq_str_push_wa returns false");
    }
    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cSuccess = (T) Boolean.valueOf(isSuccess);
    Result<T> result =
        Result.of(KeyQueueAddCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
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
