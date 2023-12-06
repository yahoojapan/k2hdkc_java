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
 * Sets a value in a cluster by using a CAS operation.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Supposing you want to initialize a variable to zero at first and then you want to change the
 * value to "1", you could write this as:
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
 * <p>You shoule see the "true" message twice on the stdout if you could successfully run it. Before
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
public class CasSetCmd extends CmdBase implements Cmd<Boolean> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(CasSetCmd.class);

  /* -- private instance members -- */
  /** key */
  private final String key;
  /** variable data type */
  private DataType type = DataType.BYTE;
  /** old value */
  private byte[] oldval = null;
  /** new value */
  private byte[] newval = null;
  /** pass */
  private String pass = DEFAULT_PASS;
  /** expiration */
  private long expirationDuration = DEFAULT_EXPIRATION_DURATION;

  /* -- Constructors -- */
  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old byte
   * @param newval a new byte
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a CasSetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasSetCmd of(
      String key, byte oldval, byte newval, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new CasSetCmd(key, oldval, newval, pass, expirationDuration);
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old byte
   * @param newval a new byte
   * @return a CasSetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasSetCmd of(String key, byte oldval, byte newval) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new CasSetCmd(key, oldval, newval, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old val(short)
   * @param newval a new val(short)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a CasSetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasSetCmd of(
      String key, short oldval, short newval, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new CasSetCmd(key, oldval, newval, pass, expirationDuration);
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old short
   * @param newval a new short
   * @return a CasSetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasSetCmd of(String key, short oldval, short newval) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new CasSetCmd(key, oldval, newval, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old val(int)
   * @param newval a new val(int)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a CasSetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasSetCmd of(
      String key, int oldval, int newval, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new CasSetCmd(key, oldval, newval, pass, expirationDuration);
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old val(int)
   * @param newval a new val(int)
   * @return a CasSetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasSetCmd of(String key, int oldval, int newval) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new CasSetCmd(key, oldval, newval, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old val(long)
   * @param newval a new val(long)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a CasSetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasSetCmd of(
      String key, long oldval, long newval, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new CasSetCmd(key, oldval, newval, pass, expirationDuration);
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old val(long)
   * @param newval a new val(long)
   * @return a CasSetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasSetCmd of(String key, long oldval, long newval) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new CasSetCmd(key, oldval, newval, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old byte
   * @param newval a new byte
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   */
  private CasSetCmd(String key, byte oldval, byte newval, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.type = CasSetCmd.DataType.BYTE;
    this.oldval = new byte[] {oldval};
    this.newval = new byte[] {newval};
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old val(2bytes)
   * @param newval a new val(2bytes)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   */
  private CasSetCmd(String key, short oldval, short newval, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.type = CasSetCmd.DataType.SHORT;
    this.oldval = new byte[] {(byte) oldval, (byte) (oldval >> 8)}; // TODO little endian only
    this.newval = new byte[] {(byte) newval, (byte) (newval >> 8)}; // TODO little endian only
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old val(4bytes)
   * @param newval a new val(4bytes)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   */
  private CasSetCmd(String key, int oldval, int newval, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.type = CasSetCmd.DataType.INT;
    this.oldval =
        new byte[] {
          (byte) (oldval << 24), (byte) (oldval << 16), (byte) (oldval << 8), (byte) (oldval)
        }; // TODO little endian only
    logger.debug("newval {} {}", newval, Integer.toBinaryString(newval));
    this.newval =
        new byte[] {
          (byte) (newval << 24), (byte) (newval << 16), (byte) (newval << 8), (byte) (newval)
        };
    logger.debug(
        "this.newval {} this.newval[0] {} this.newval[1] {} this.newval[2] {} this.newval[3] {}",
        this.newval,
        Integer.toBinaryString(this.newval[0] & 0xFF).replace(' ', '0'),
        Integer.toBinaryString(this.newval[1] & 0xFF).replace(' ', '0'),
        Integer.toBinaryString(this.newval[2] & 0xFF).replace(' ', '0'),
        Integer.toBinaryString(this.newval[3] & 0xFF).replace(' ', '0'));
    this.pass = pass;
    this.expirationDuration = expirationDuration;
    logger.debug(
        "rnewval {} {}",
        Cmd.bytesToInt(this.newval),
        Integer.toBinaryString(Cmd.bytesToInt(this.newval)));
  }

  /**
   * Constructs a CasSetCmd instance.
   *
   * @param key a key string
   * @param oldval an old val(8bytes)
   * @param newval a new val(8bytes)
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   */
  private CasSetCmd(String key, long oldval, long newval, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.type = CasSetCmd.DataType.LONG;
    this.oldval =
        new byte[] {
          (byte) oldval,
          (byte) (oldval >> 8),
          (byte) (oldval >> 8),
          (byte) (oldval >> 8),
          (byte) (oldval >> 8),
          (byte) (oldval >> 8),
          (byte) (oldval >> 8),
          (byte) (oldval >> 8)
        }; // TODO little endian only
    this.newval =
        new byte[] {
          (byte) newval,
          (byte) (newval >> 8),
          (byte) (newval >> 8),
          (byte) (newval >> 8),
          (byte) (newval >> 8),
          (byte) (newval >> 8),
          (byte) (newval >> 8),
          (byte) (newval >> 8)
        }; // TODO little endian only
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /* -- Instance methods -- */
  /**
   * Sets a variable by a CAS operation.
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

    boolean isSuccess = false;
    LongByReference lref = new LongByReference(this.expirationDuration);
    if (this.type == DataType.BYTE && this.oldval.length == 1) {
      isSuccess =
          INSTANCE.k2hdkc_pm_cas8_str_set_wa(
              session.getHandle(),
              this.key,
              this.oldval[0],
              this.newval[0],
              this.pass,
              (this.expirationDuration != 0) ? lref : null);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_cas8_str_set_wa returns false");
      }
    } else if (this.type == DataType.SHORT && this.oldval.length == 2) {
      isSuccess =
          INSTANCE.k2hdkc_pm_cas16_str_set_wa(
              session.getHandle(),
              this.key,
              (short) (this.oldval[0] << 8 | this.oldval[1] & 0xFF),
              (short) (this.newval[0] << 8 | this.newval[1] & 0xFF),
              this.pass,
              (this.expirationDuration != 0) ? lref : null);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_cas16_str_set_wa returns false");
      }
    } else if (this.type == DataType.INT && this.oldval.length == 4) {
      isSuccess =
          INSTANCE.k2hdkc_pm_cas32_str_set_wa(
              session.getHandle(),
              this.key,
              Cmd.bytesToInt(this.oldval),
              Cmd.bytesToInt(this.newval),
              this.pass,
              (this.expirationDuration != 0) ? lref : null);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_cas32_str_set_wa returns false");
      }
    } else if (this.type == DataType.LONG && this.oldval.length == 8) {
      isSuccess =
          INSTANCE.k2hdkc_pm_cas64_str_set_wa(
              session.getHandle(),
              this.key,
              (this.oldval[0] << 8
                  | this.oldval[1] << 8
                  | this.oldval[2] << 8
                  | this.oldval[3] << 8
                  | this.oldval[4] << 8
                  | this.oldval[5] << 8
                  | this.oldval[6] << 8
                  | this.oldval[7] & 0xFF),
              (this.newval[0] << 8
                  | this.newval[1] << 8
                  | this.newval[2] << 8
                  | this.newval[3] << 8
                  | this.newval[4] << 8
                  | this.newval[5] << 8
                  | this.newval[6] << 8
                  | this.newval[7] & 0xFF),
              this.pass,
              (this.expirationDuration != 0) ? lref : null);
      if (!isSuccess) {
        logger.error("INSTANCE.k2hdkc_pm_cas64_str_set_wa returns false");
      }
    } else {
      logger.error(
          "invalid request. this.type length {} should not be this.oldval.length {} should be {}",
          this.type.name(),
          this.oldval.length,
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
        Result.of(CasSetCmd.class.getSimpleName(), isSuccess, cSuccess, code, detailCode);
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
