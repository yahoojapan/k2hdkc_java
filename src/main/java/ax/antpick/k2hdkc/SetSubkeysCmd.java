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
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces current subkeys with new one.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Supposing you want to clear the current subkeys("subkey1" and "subkey2") with a new
 * subkey("subkey3"), you could write this as:
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
 *         Session s = Session.of(c); ) {
 *       String[] subkeys = {"subkey1", "subkey2"};
 *       SetAllCmd set = SetAllCmd.of("key", "val", subkeys);
 *       assert (set.execute(s).get().isSuccess());
 *       // 1. get key
 *       GetCmd get = GetCmd.of("key");
 *       String str = (String) get.execute(s).get().getValue();
 *       assert (str.equals("val"));
 *       // 2. get subkeys
 *       GetSubkeysCmd getsub = GetSubkeysCmd.of("key");
 *       List<String> list = (List<String>) getsub.execute(s).get().getValue();
 *       assert (list.get(0).equals("subkey1"));
 *       assert (list.get(1).equals("subkey2"));
 *       // 3. set subkeys
 *       String[] subkeys3 = {"subkey3"};
 *       SetSubkeysCmd setsub = SetSubkeysCmd.of("key", subkeys3);
 *       assert (setsub.execute(s).get().isSuccess());
 *       // 4. get subkeys
 *       GetSubkeysCmd getsub2 = GetSubkeysCmd.of("key");
 *       List<String> list2 = (List<String>) getsub2.execute(s).get().getValue();
 *       assert (list2.get(0).equals("subkey3"));
 *     } catch (IOException ex) {
 *       System.out.println(ex.getMessage());
 *       assert (false);
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>You shoule see the "subkey1", "subkey2" and "subkey3" on the stdout if you could successfully
 * run it. Before running the code above, You should run three processes.
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
public class SetSubkeysCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(SetSubkeysCmd.class);

  /* -- private instance members -- */
  /** a key string */
  private final String key;
  /** an array of subkey string */
  private final String[] subkeys;

  /* -- Constructors -- */
  /**
   * Constructs a SetSubkeys instance.
   *
   * @param key a key string
   * @param subkeys an array of subkeys
   * @return a SetSubkeysCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static SetSubkeysCmd of(String key, String[] subkeys) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (subkeys == null || subkeys.length == 0) {
      throw new IllegalArgumentException("subkeys is null");
    }
    return new SetSubkeysCmd(key, subkeys);
  }

  /**
   * Constructs a SetSubkeys instance.
   *
   * @param key a key string
   * @param subkeys an array of subkeys
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  private SetSubkeysCmd(String key, String[] subkeys) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (subkeys == null || subkeys.length == 0) {
      throw new IllegalArgumentException("subkeys is null");
    }
    this.key = key;
    this.subkeys = subkeys;
  }

  /* -- Instance methods -- */
  /**
   * Replaces current subkeys with new one.
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
    StringArray jnaArray = new StringArray(this.subkeys); // null is acceptable.

    boolean isSuccess = INSTANCE.k2hdkc_pm_set_str_subkeys(session.getHandle(), key, jnaArray);
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
        Result.of(SetSubkeysCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
    return Optional.of(result);
  }
}
