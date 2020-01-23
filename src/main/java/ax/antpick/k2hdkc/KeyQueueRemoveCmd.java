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
n * THE SOFTWARE.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes a new element to a queue.
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
public class KeyQueueRemoveCmd extends CmdBase implements Cmd<Map<String, String>> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(KeyQueueRemoveCmd.class);

  /* -- private instance members -- */
  /** The prefix of a queue */
  private final String prefix;
  /** isFifo */
  private boolean isFifo = DEFAULT_IS_FIFO;
  /** pass a password string to retrieve the value. */
  private String pass = DEFAULT_PASS;
  /** The number of elements to be removed */
  private int removeElementSize = DEFAULT_REMOVE_ELEMENT_SIZE;
  /** <code> true </code> if return removed elements */
  private boolean needReturnValue = DEFAULT_NEED_RETURN_VALUE; // java.util.Queueのremoveでは値が帰ってくる。

  /* -- Constructors -- */
  /**
   * Constructs a KeyQueueRemoveCmd instance.
   *
   * @param prefix a prefix string
   * @return a KeyQueueRemoveCmd instance.
   * @throws IllegalArgumentException if a key string is null or empty
   */
  public static KeyQueueRemoveCmd of(String prefix) {
    if (prefix == null || prefix.isEmpty()) {
      throw new IllegalArgumentException("prefix is null");
    }
    return new KeyQueueRemoveCmd(
        prefix,
        DEFAULT_IS_FIFO,
        DEFAULT_PASS,
        DEFAULT_REMOVE_ELEMENT_SIZE,
        DEFAULT_NEED_RETURN_VALUE);
  }

  /**
   * Constructs a KeyQueueRemoveCmd instance.
   *
   * @param prefix a prefix string
   * @param isFifo {@code true} if this queue element come in first go out first.
   * @param password a password string to retrieve the value
   * @param elements number of elements to be removed.
   * @param needReturnValue {@code true} if return elements to be removed.
   * @throws IllegalArgumentException if a key string is null or empty
   */
  private KeyQueueRemoveCmd(
      String prefix, boolean isFifo, String password, int elements, boolean needReturnValue) {
    if (prefix == null || prefix.isEmpty()) {
      throw new IllegalArgumentException("prefix is null");
    }
    this.prefix = prefix;
    this.isFifo = isFifo;
    this.pass = password;
    this.removeElementSize = elements;
    this.needReturnValue = needReturnValue;
  }

  /* -- Instance methods -- */
  /**
   * Removes an element from this queue.
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
    assert (this.prefix != null && this.prefix.length() != 0);

    boolean isSuccess = false;
    Map<String, String> map = new HashMap<>();
    if (this.needReturnValue) {
      for (int i = 0; i < this.removeElementSize; i++) {
        PointerByReference ppkey = new PointerByReference();
        PointerByReference ppval = new PointerByReference();
        isSuccess =
            INSTANCE.k2hdkc_pm_keyq_str_pop_wp(
                session.getHandle(), this.prefix, this.isFifo, this.pass, ppkey, ppval);

        if (!isSuccess) {
          logger.error("INSTANCE.k2hdkc_pm_keyq_str_pop_wp returns false");
        } else {
          Pointer pkey = ppkey.getValue();
          Pointer pval = ppval.getValue();
          if (pkey != null && pval != null) {
            String rkey = pkey.getString(0);
            String rval = pval.getString(0);
            if (rkey == null || rkey.isEmpty() || rval == null || rval.isEmpty()) {
              logger.warn("INSTANCE.k2hdkc_pm_keyq_str_pop_wp returns null");
            } else {
              map.put(rkey, rval);
            }
          } else {
            logger.warn("The pointer of INSTANCE.k2hdkc_pm_keyq_str_pop_wp returns null");
          }
        }
      }
    } else {
      isSuccess =
          INSTANCE.k2hdkc_pm_keyq_str_remove_wp(
              session.getHandle(), this.prefix, this.removeElementSize, this.isFifo, this.pass);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_keyq_str_remove_wp returns false");
      }
    }

    // String cmd, boolean isSuccess, String value, long code, long detailCode) {
    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cMap = (T) map;
    Result<T> result =
        Result.of(KeyQueueRemoveCmd.class.getSimpleName(), isSuccess, cMap, code, detailCode);
    return Optional.of(result);
  }
}
