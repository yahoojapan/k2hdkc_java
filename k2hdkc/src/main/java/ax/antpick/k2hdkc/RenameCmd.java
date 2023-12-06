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
 * Rename a key in a cluster.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Supposing you want to rename a key(the name is "key") to another key "newkey", you could write
 * this as:
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
 *      SetCmd set = SetCmd.of("key", "value");
 *      assert ((Boolean) set.execute(s).get().getValue() == true);
 *      RenameCmd rm = RenameCmd.of("key", "newkey");
 *      assert ((Boolean) rm.execute(s).get().getValue() == true);
 *      GetCmd get = GetCmd.of("newkey");
 *      String str = (String) get.execute(s).get().getValue();
 *      System.out.println(str);
 *    } catch (IOException ex) {
 *      System.out.println(ex.getMessage());
 *      assert (false);
 *    }
 *  }
 * }
 * }</pre>
 *
 * <p>You shoule see the "true" message on the stdout if you could successfully run it. Before
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
public class RenameCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(RenameCmd.class);

  /* -- private instance members -- */
  /** key */
  private final String key;
  /** newKey */
  private final String newKey;
  /** parentKey */
  private String parentKey = DEFAULT_PARENT_KEY;
  /** checkParentAttrs */
  private boolean checkParentAttrs = DEFAULT_IS_CHECK_PARENT_ATTRS;
  /** pass */
  private String password = DEFAULT_PASS;
  /** expiration */
  private long expirationDuration = DEFAULT_EXPIRATION_DURATION;

  /* -- Constructors -- */
  /**
   * Constructs a RenameCmd instance.
   *
   * @param key a key string
   * @param newKey a new key string
   * @return a RenameCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static RenameCmd of(String key, String newKey) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (newKey == null || newKey.isEmpty()) {
      throw new IllegalArgumentException("val is null");
    }
    return new RenameCmd(
        key,
        newKey,
        DEFAULT_PARENT_KEY,
        DEFAULT_IS_CHECK_PARENT_ATTRS,
        DEFAULT_PASS,
        DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a RenameCmd instance.
   *
   * @param key a key string
   * @param newKey a newKey string
   * @param parentKey a parentKey string
   * @param checkParentAttrs <code>true</code> if checking parent attributes before changing a
   *     subkeys. <code>false</code> otherwise.
   * @param password a password string
   * @param expirationDuration a duration to expire.
   * @return a RenameCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static RenameCmd of(
      String key,
      String newKey,
      String parentKey,
      boolean checkParentAttrs,
      String password,
      long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (newKey == null || newKey.isEmpty()) {
      throw new IllegalArgumentException("newKey is null");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new RenameCmd(key, newKey, parentKey, checkParentAttrs, password, expirationDuration);
  }

  /**
   * Constructs a RenameCmd instance.
   *
   * @param key a key string
   * @param newKey a newKey string
   * @param parentKey a parentKey string
   * @param checkParentAttrs <code>true</code> if checking parent attributes before changing a
   *     subkeys. <code>false</code> otherwise.
   * @param password a password string
   * @param expirationDuration a duration to expire.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  private RenameCmd(
      String key,
      String newKey,
      String parentKey,
      boolean checkParentAttrs,
      String password,
      long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (newKey != null && !newKey.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.newKey = newKey;
    this.parentKey = parentKey;
    this.checkParentAttrs = checkParentAttrs;
    this.password = password;
    this.expirationDuration = expirationDuration;
  }

  /* -- Instance methods -- */
  /**
   * Rename an old key with a new key.
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
    boolean isSuccess =
        INSTANCE.k2hdkc_pm_rename_with_parent_str_wa(
            session.getHandle(),
            this.key,
            this.newKey,
            this.parentKey,
            this.checkParentAttrs,
            this.password,
            (this.expirationDuration != 0L) ? lref : null);
    if (!isSuccess) {
      logger.error("INSTANCE.k2hdkc_pm_rename_with_parent_str_wa returns false");
      return Optional.empty(); // TODO distinguish empty and error.
    }
    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cSuccess = (T) Boolean.valueOf(isSuccess);
    Result<T> result =
        Result.of(RenameCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
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
