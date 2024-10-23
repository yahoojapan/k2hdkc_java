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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes a new element to a queue.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Supposing you want to add a new element(the element name is "e1" to a queue(the queue name is
 * "q1"), then you get(remove) the element from the queue, you could write this as:
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
 *       QueueAddCmd qadd = QueueAddCmd.of("q1", "e1");
 *       assert (qadd.execute(sess).get().isSuccess());
 *       // 2. remove an element from this queue
 *       QueueRemoveCmd qrm = QueueRemoveCmd.of("q1");
 *       List<String> r = (List<String>) qrm.execute(sess).get().getValue();
 *       assert (r.get(0).equals("e1"));
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
public class QueueRemoveCmd extends CmdBase implements Cmd<List<String>> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(QueueRemoveCmd.class);

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
  private boolean needReturnValue = DEFAULT_NEED_RETURN_VALUE;
  // The remove method of java.util.Queue returns removed values.

  /* -- Constructors -- */
  /**
   * Constructs a QueueRemoveCmd instance.
   *
   * @param prefix a prefix string
   * @return a QueueRemoveCmd instance.
   * @throws IllegalArgumentException if a key string is null or empty
   */
  public static QueueRemoveCmd of(String prefix) {
    if (prefix == null || prefix.isEmpty()) {
      throw new IllegalArgumentException("prefix is null");
    }
    return new QueueRemoveCmd(
        prefix,
        DEFAULT_IS_FIFO,
        DEFAULT_PASS,
        DEFAULT_REMOVE_ELEMENT_SIZE,
        DEFAULT_NEED_RETURN_VALUE);
  }

  /**
   * Constructs a QueueRemoveCmd instance.
   *
   * @param prefix a prefix string
   * @param isFifo {@code true} if this queue element come in first go out first.
   * @param password a password string to retrieve the value
   * @param elements number of elements to be removed.
   * @param needReturnValue {@code true} if return elements to be removed.
   * @throws IllegalArgumentException if a key string is null or empty
   */
  private QueueRemoveCmd(
      String prefix, boolean isFifo, String password, int elements, boolean needReturnValue) {
    assert (prefix != null && !prefix.isEmpty());
    this.prefix = prefix;
    this.isFifo = isFifo;
    this.pass = password;
    this.removeElementSize = elements;
    this.needReturnValue = needReturnValue;
  }

  /* -- Instance methods -- */
  /**
   * Removes elements from this queue.
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

    boolean isSuccess = false;
    List<String> list = new ArrayList<>();
    if (this.needReturnValue) {
      for (int i = 0; i < this.removeElementSize; i++) {
        PointerByReference ppval = new PointerByReference();
        isSuccess =
            INSTANCE.k2hdkc_pm_q_str_pop_wp(
                session.getHandle(), this.prefix, this.isFifo, this.pass, ppval);

        if (!isSuccess) {
          logger.error("INSTANCE.k2hdkc_pm_q_str_pop_wp returns false");
        } else {
          Pointer p = ppval.getValue();
          if (p != null) {
            String rval = p.getString(0);
            if (rval == null || rval.isEmpty()) {
              logger.warn("INSTANCE.k2hdkc_pm_q_str_pop_wp returns null");
            } else {
              list.add(rval);
            }
          } else {
            logger.warn("The pointer of INSTANCE.k2hdkc_pm_q_str_pop_wp returns null");
          }
        }
      }
    } else {
      isSuccess =
          INSTANCE.k2hdkc_pm_q_str_remove_wp(
              session.getHandle(), this.prefix, this.removeElementSize, this.isFifo, this.pass);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_q_str_remove_wp returns false");
      }
    }

    // String cmd, boolean isSuccess, String value, long code, long detailCode) {
    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cList = (T) list;
    Result<T> result =
        Result.of(QueueRemoveCmd.class.getSimpleName(), isSuccess, cList, code, detailCode);
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
