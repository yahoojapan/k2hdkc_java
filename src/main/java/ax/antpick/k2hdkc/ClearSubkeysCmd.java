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

import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clears subkeys of a key. Another subkeys that a subkey has will be removed recursively.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>This example adds a subkey to a key and retrieves it, then clears it.
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
 *       // 1. add a subkey
 *       AddSubkeyCmd cmd = AddSubkeyCmd.of("key", "subkey");
 *       System.out.println(cmd.execute(s).get().getValue()); // true
 *       // 2. get subkeys
 *       GetSubkeysCmd get = GetSubkeysCmd.of("key");
 *       List<String> list = (List<String>) get.execute(s).get().getValue(); // subkey
 *       System.out.println(list.get(0)); // subkey
 *       // 3. clear subkeys
 *       ClearSubkeysCmd clear = ClearSubkeysCmd.of("key");
 *       System.out.println(clear.execute(s).get().getValue()); // true
 *     } catch (IOException ex) {
 *       System.out.println(ex.getMessage());
 *       assert (false);
 *     }
 *   }
 * }
 * }</pre>
 *
 * Before running the code above, You should run three processes.
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
public class ClearSubkeysCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(ClearSubkeysCmd.class);

  /* -- private instance members -- */
  /** The key to be retrieved. */
  private final String key;

  /* -- Constructors -- */
  /**
   * Constructs a ClearSubkeysCmd instance.
   *
   * @param key a key string
   * @return a ClearSubkeysCmd instance.
   * @throws IllegalArgumentException if a key string is null or empty
   */
  public static ClearSubkeysCmd of(String key) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new ClearSubkeysCmd(key);
  }

  /**
   * Constructs a ClearSubkeysCmd instance.
   *
   * @param key a key string
   * @throws IllegalArgumentException if a key string is null or empty
   */
  private ClearSubkeysCmd(String key) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    this.key = key;
  }

  /* -- Instance methods -- */
  /**
   * Clear subkeys of a key. Another subkeys that a subkey has will be removed recursively.
   *
   * @param session a {@link Session} object
   * @return the {@literal Optional<Result<T>>} object
   * @throws IllegalArgumentException if an illegal augment exists
   * @throws IOException if underlying library errors occur.
   */
  @Override
  public <T> Optional<Result<T>> execute(Session session) throws IOException {
    if (session == null) {
      throw new IllegalArgumentException("session should not be null");
    }
    K2hdkcLibrary INSTANCE = Session.getLibrary(); // throws IOException.
    assert (INSTANCE != null);
    K2hashLibrary K2HASH_INSTANCE = Session.getK2hashLibrary(); // throws IOException.
    assert (K2HASH_INSTANCE != null);
    // key
    assert (this.key != null && this.key.length() != 0);

    // clear subkeys
    boolean isSuccess = INSTANCE.k2hdkc_pm_clear_str_subkeys(session.getHandle(), this.key);
    if (!isSuccess) {
      logger.error("INSTANCE.k2hdkc_pm_clear_str_subkeys returns false");
    }
    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cSuccess = (T) Boolean.valueOf(isSuccess);
    Result<T> result =
        Result.of(ClearSubkeysCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
    return Optional.of(result);
  }
}
