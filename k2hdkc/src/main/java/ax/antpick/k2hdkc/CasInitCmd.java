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
import java.nio.ByteBuffer;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes a variable in a cluster by using a CAS operation.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Supposing you want to initialize a variable to zero, you could write this as:
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
 * <p>You shoule see the "true" on stdout if you could successfully run it. Before running the code
 * above, You should run three processes.
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
public class CasInitCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(CasInitCmd.class);

  /* -- private instance members -- */
  /** key */
  private final String key;
  /** variable data type */
  private DataType type = DataType.BYTE;
  /** value */
  private byte[] val = null;
  /** pass */
  private String pass = DEFAULT_PASS;
  /** expiration */
  private long expirationDuration = DEFAULT_EXPIRATION_DURATION;

  /* -- Constructors -- */
  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a byte
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a CasInitCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasInitCmd of(String key, byte val, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new CasInitCmd(key, val, pass, expirationDuration);
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a new byte
   * @return a CasInitCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasInitCmd of(String key, byte val) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new CasInitCmd(key, val, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a val(short)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a CasInitCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasInitCmd of(String key, short val, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new CasInitCmd(key, val, pass, expirationDuration);
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a short
   * @return a CasInitCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasInitCmd of(String key, short val) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new CasInitCmd(key, val, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a val(int)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a CasInitCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasInitCmd of(String key, int val, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new CasInitCmd(key, val, pass, expirationDuration);
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a val(int)
   * @return a CasInitCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasInitCmd of(String key, int val) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new CasInitCmd(key, val, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a val(long)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a CasInitCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasInitCmd of(String key, long val, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new CasInitCmd(key, val, pass, expirationDuration);
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a val(long)
   * @return a CasInitCmd instance
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasInitCmd of(String key, long val) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new CasInitCmd(key, val, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a byte
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   */
  private CasInitCmd(String key, byte val, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.val = new byte[] {val};
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a new val(2bytes)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   */
  private CasInitCmd(String key, short val, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.type = CasInitCmd.DataType.SHORT;
    this.val = new byte[] {(byte) val, (byte) (val >> 8)}; // TODO little endian only
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /**
   * Transform an integer variable to a byte array.
   *
   * @param val a integer variable
   * @return a byte array
   */
  private static byte[] intToBytes(int val) {
    /* 00000000 00000000 00000000 00000001 */
    return new byte[] {
      (byte) (val << 24), (byte) (val << 16), (byte) (val << 8), (byte) (val)
    }; // TODO little endian only
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a val(4bytes)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   */
  private CasInitCmd(String key, int val, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.type = CasInitCmd.DataType.INT;
    this.val = intToBytes(val);
    // assertion
    int rval = ByteBuffer.wrap(this.val).getInt();
    assert (val == rval);

    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /**
   * Constructs a CasInitCmd instance.
   *
   * @param key a key string
   * @param val a new val(8bytes)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   */
  private CasInitCmd(String key, long val, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.type = CasInitCmd.DataType.LONG;
    this.val =
        new byte[] {
          (byte) val,
          (byte) (val >> 8),
          (byte) (val >> 8),
          (byte) (val >> 8),
          (byte) (val >> 8),
          (byte) (val >> 8),
          (byte) (val >> 8),
          (byte) (val >> 8)
        }; // TODO little endian only
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /* -- Instance methods -- */
  /**
   * Initialize the value of a key in a CAS operation.
   *
   * @param session a session instance
   * @return the result instance
   * @throws IllegalArgumentException if an illegal augment exists.
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

    boolean isSuccess = false;
    LongByReference lref = new LongByReference(this.expirationDuration);
    if (this.type == DataType.BYTE && this.val.length == 1) {
      isSuccess =
          INSTANCE.k2hdkc_pm_cas8_str_init_wa(
              session.getHandle(),
              this.key,
              this.val[0],
              this.pass,
              (this.expirationDuration != 0) ? lref : null);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_cas8_str_init_wa returns false, should be true");
      }
    } else if (this.type == DataType.SHORT && this.val.length == 2) {
      isSuccess =
          INSTANCE.k2hdkc_pm_cas16_str_init_wa(
              session.getHandle(),
              this.key,
              (short) (this.val[0] << 8 | this.val[1] & 0xFF),
              this.pass,
              (this.expirationDuration != 0) ? lref : null);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_cas16_str_init_wa returns false, should be true");
      }
    } else if (this.type == DataType.INT && this.val.length == 4) {
      isSuccess =
          INSTANCE.k2hdkc_pm_cas32_str_init_wa(
              session.getHandle(),
              this.key,
              Cmd.bytesToInt(this.val),
              this.pass,
              (this.expirationDuration != 0) ? lref : null);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_cas32_str_init_wa returns false, should be true");
      }
    } else if (this.type == DataType.LONG && this.val.length == 8) {
      isSuccess =
          INSTANCE.k2hdkc_pm_cas64_str_init_wa(
              session.getHandle(),
              this.key,
              (this.val[0] << 8
                  | this.val[1] << 8
                  | this.val[2] << 8
                  | this.val[3] << 8
                  | this.val[4] << 8
                  | this.val[5] << 8
                  | this.val[6] << 8
                  | this.val[7] & 0xFF),
              this.pass,
              (this.expirationDuration != 0) ? lref : null);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_cas64_str_init_wa returns false, should be true");
      }
    } else {
      logger.error(
          "invalid request. this.type length {} should not be this.oldval.length {} should be {}",
          this.type.name(),
          this.val.length,
          this.type.ordinal());
      throw new IllegalArgumentException("type and val are invalid");
    }
    // String cmd, boolean isSuccess, long code, long detailCode) {
    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cSuccess = (T) Boolean.valueOf(isSuccess);
    Result<T> result =
        Result.of(CasInitCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
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
