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
n * all copies or substantial portions of the Software.
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds a new subkey to a current subkey.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Supposing you want to add a subkey(the name is "subkey") to a key(the name is "key"), you
 * could write this as:
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
public class AddSubkeyCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(AddSubkeyCmd.class);

  /* -- private instance members -- */
  /** The string key element in a cluster. */
  private final String key;
  /** The child key element of the key element. */
  private final String subkey;

  /* -- Constructors -- */
  /**
   * Constructs a {@link AddSubkeyCmd} instance.
   *
   * @param key a key string
   * @param subkey a val string
   * @return an {@link AddSubkeyCmd} object
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static AddSubkeyCmd of(String key, String subkey) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (subkey == null || subkey.isEmpty()) {
      throw new IllegalArgumentException("subkeys should not be null or empty");
    }
    return new AddSubkeyCmd(key, subkey);
  }

  /**
   * Constructs a {@link AddSubkeyCmd} instance.
   *
   * @param key the string key element in a cluster
   * @param subkey a subkey string
   */
  private AddSubkeyCmd(String key, String subkey) {
    assert (key != null && !key.isEmpty());
    assert (subkey != null && !subkey.isEmpty());
    this.key = key;
    this.subkey = subkey;
  }

  /* -- Instance methods -- */
  /**
   * Adds a new subkey to the current subkeys of a key.
   *
   * @param session a {@link Session} instance.
   * @return an Optional {@link Result} instance.
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

    // 1. get the current subkeys.
    PointerByReference ppskeypck =
        INSTANCE.k2hdkc_pm_get_str_direct_subkeys(session.getHandle(), this.key);
    List<String> list = null;
    if (ppskeypck != null) {
      Pointer ptr = ppskeypck.getPointer();
      if (ptr != null) {
        String[] array = ptr.getStringArray(0);
        if (array.length > 0) {
          list = Arrays.asList(array);
        }
      }
    }
    // 2. append the new subkey to them.
    List<String> l2 = new ArrayList<>();
    l2.add(subkey);
    if (list != null) {
      l2.addAll(list);
    }
    String[] subkeys = l2.toArray(new String[l2.size()]);

    // 3. set the new subkeys.
    StringArray jnaArray = new StringArray(subkeys); // null is acceptable.

    boolean isSuccess = INSTANCE.k2hdkc_pm_set_str_subkeys(session.getHandle(), key, jnaArray);
    if (!isSuccess) {
      logger.error("INSTANCE.k2hdkc_pm_set_str_value_wa returns false");
    }

    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cSuccess = (T) Boolean.valueOf(isSuccess);
    Result<T> result =
        Result.of(AddSubkeyCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
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
