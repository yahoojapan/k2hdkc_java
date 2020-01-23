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
 * Increments and Decrements a variable in a cluster by using a CAS operation.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Typical use case of this class is a counter. You will usually take the following 3 steps.
 *
 * <ol>
 *   <li>Initializes a CAS variable in a cluster by using a CasInitCmd object.
 *   <li>Updates the variable by using a CasIncDecCmd object.
 *   <li>Retrieves the variable by using a CasGetCmd object.
 * </ol>
 *
 * <p>Here is an example code.
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
 *       Session s = Session.of(c); ) {
 *       // 1. init
 *       CasInitCmd init = CasInitCmd.of("key", 0);
 *       System.out.println(init.execute(s).get().getValue()); // true
 *
 *       // 2. set
 *       CasSetCmd set = CasSetCmd.of("key", 0, 1); // CasIncDecCmd should be used here.
 *       System.out.println(set.execute(s).get().getValue()); // true
 *
 *       // 3. get
 *       CasGetCmd get = CasGetCmd.of("key", Cmd.DataType.INT);
 *       ByteArrayOutputStream bos = (ByteArrayOutputStream) get.execute(s).get().getValue();
 *       assert (Cmd.getValusAsInt(bos.toByteArray()) == 1);
 *     } catch (IOException ex) {
 *       System.out.println(ex.getMessage());
 *       assert (false);
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>You shoule see the "true" twice and "1" on the stdout if you could successfully run it. Before
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
public class CasIncDecCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(CasIncDecCmd.class);

  /* -- private instance members -- */
  /** key */
  private final String key;
  /** isIncrement */
  private boolean isIncrement = DEFAULT_IS_INCREMENT;
  /** pass */
  private String pass = DEFAULT_PASS;
  /** expiration */
  private long expirationDuration = DEFAULT_EXPIRATION_DURATION;

  /* -- Constructors -- */
  /**
   * Constructs a CasIncDecCmd instance.
   *
   * @param key a key string
   * @return a CasIncDecCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasIncDecCmd of(String key) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return CasIncDecCmd.of(key, DEFAULT_IS_INCREMENT, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a CasIncDecCmd instance.
   *
   * @param key a key string
   * @param isIncrement <code>true</code> is clear subkeys. <code>false</code> otherwise.
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a CasIncDecCmd instance.
   */
  public static CasIncDecCmd of(
      String key, boolean isIncrement, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new CasIncDecCmd(key, isIncrement, pass, expirationDuration);
  }

  /* -- Instance methods -- */
  /**
   * Constructs a CasIncDecCmd instance.
   *
   * @param key a key string
   * @param isIncrement <code>true</code> is clear subkeys. <code>false</code> otherwise.
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   */
  private CasIncDecCmd(String key, boolean isIncrement, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.isIncrement = isIncrement;
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /**
   * Increments or decrements a CAS variable.
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
    assert (expirationDuration >= 0);

    boolean isSuccess;
    LongByReference lref = new LongByReference(this.expirationDuration);
    if (this.isIncrement) {
      isSuccess =
          INSTANCE.k2hdkc_pm_cas_str_increment_wa(
              session.getHandle(),
              this.key,
              this.pass,
              (this.expirationDuration != 0) ? lref : null);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_cas_str_increment_wa returns false");
      }
    } else {
      isSuccess =
          INSTANCE.k2hdkc_pm_cas_str_decrement_wa(
              session.getHandle(),
              this.key,
              this.pass,
              (this.expirationDuration != 0) ? lref : null);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_cas_str_decrement_wa returns false");
      }
    }

    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cSuccess = (T) Boolean.valueOf(isSuccess);
    Result<T> result =
        Result.of(CasIncDecCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
    return Optional.of(result);
  }
}
