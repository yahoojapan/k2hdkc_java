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
 * Removes a subkey from the current subkeys.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Supposing you want to set the subkeys("subkey1" and "subkey2") and remove a subkey("subkey1"),
 * you could write this as:
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
 *       // 3. remove subkeys
 *       RemoveSubkeyCmd rmsub = RemoveSubkeyCmd.of("key", "subkey1");
 *       assert (rmsub.execute(s).get().isSuccess());
 *       // 4. get subkeys
 *       GetSubkeysCmd getsub2 = GetSubkeysCmd.of("key");
 *       List<String> list2 = (List<String>) getsub2.execute(s).get().getValue();
 *       assert (list2.get(0).equals("subkey2"));
 *     } catch (IOException ex) {
 *       System.out.println(ex.getMessage());
 *       assert (false);
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>You shoule see the "subkey1", "subkey2" and "subkey2" on the stdout if you could successfully
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
public class RemoveSubkeyCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(RemoveSubkeyCmd.class);

  /* -- private instance members -- */
  /** key */
  private final String key;
  /** subkey */
  private final String subkey;
  /** recursive remove */
  private boolean removeRecursively = DEFAULT_REMOVE_RECURSIVELY;

  /* -- Constructors -- */
  /**
   * Constructs a RemoveSubkeyCmd instance.
   *
   * @param key a key string
   * @param subkey a subkey string
   * @return a RemoveSubkeyCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static RemoveSubkeyCmd of(String key, String subkey) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (subkey == null || subkey.isEmpty()) {
      throw new IllegalArgumentException("subkeys is null");
    }
    return new RemoveSubkeyCmd(key, subkey, DEFAULT_REMOVE_RECURSIVELY);
  }

  /**
   * Constructs a RemoveSubkeyCmd instance.
   *
   * @param key a key string
   * @param subkey a subkey string
   * @param isRemoveRecursively a flag to remove recursively.
   * @return a RemoveSubkeyCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static RemoveSubkeyCmd of(String key, String subkey, boolean isRemoveRecursively) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (subkey == null || subkey.isEmpty()) {
      throw new IllegalArgumentException("subkeys is null");
    }
    return new RemoveSubkeyCmd(key, subkey, isRemoveRecursively);
  }

  /**
   * Constructs a RemoveSubkeyCmd instance.
   *
   * @param key a key string.
   * @param subkey a subkey string.
   * @param removeRecursively {@code true} if remove subkeys recursively.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  private RemoveSubkeyCmd(String key, String subkey, boolean removeRecursively) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (subkey == null || subkey.isEmpty()) {
      throw new IllegalArgumentException("subkey should not be null or empty");
    }
    this.key = key;
    this.subkey = subkey;
    this.removeRecursively = removeRecursively;
  }

  /* -- Instance methods -- */
  /**
   * Remove subkeys.
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
    // subkey
    assert (this.subkey != null && !this.subkey.isEmpty());

    // set
    boolean isRemoveRecursive = false;
    boolean isSuccess =
        INSTANCE.k2hdkc_pm_remove_str_subkey(
            session.getHandle(), key, subkey, subkey.length(), this.removeRecursively);
    if (!isSuccess) {
      logger.error("INSTANCE.k2hdkc_pm_remove_str_subkey returns false");
    }
    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cSuccess = (T) Boolean.valueOf(isSuccess);
    Result<T> result =
        Result.of(RemoveSubkeyCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
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
