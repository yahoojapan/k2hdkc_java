/*
 * The MIT License
 *
 * Copyright 2018 Yahoo Japan Corporation.
 *
n * Permission is hereby granted, free of charge, to any person obtaining a copy
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets a variable from a cluster using a CAS operation.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Typical use case of this class is a counter. You will usually take the following 3 steps.
 *
 * <ol>
 *   <li>Initializes a variable in a cluster by using a CasInitCmd object.
 *   <li>Updates the variable by using a CasSetCmd or CasIncDecCmd object.
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
public class CasGetCmd extends CmdBase implements Cmd<ByteArrayOutputStream> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(CasGetCmd.class);

  /* -- private instance members -- */
  /** key */
  private final String key;
  /** variable data type */
  private DataType type = DataType.BYTE;
  /** pass */
  private String pass = DEFAULT_PASS;
  /** expiration */
  private long expirationDuration = DEFAULT_EXPIRATION_DURATION;

  /* -- Constructors -- */
  /**
   * Constructs a CasGetCmd instance.
   *
   * @param key a key string
   * @param type a data type
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   * @return a CasGetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasGetCmd of(String key, DataType type, String pass, long expirationDuration) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    if (expirationDuration < 0) {
      throw new IllegalArgumentException(
          "expirationDuration is negation, should be greater than equal zero.");
    }
    return new CasGetCmd(key, type, pass, expirationDuration);
  }

  /**
   * Constructs a CasGetCmd instance.
   *
   * @param key a key string
   * @param type a {@code DateType} instance.
   * @return a CasGetCmd instance.
   * @throws IllegalArgumentException if a key or a val string is null or empty
   */
  public static CasGetCmd of(String key, DataType type) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new CasGetCmd(key, type, DEFAULT_PASS, DEFAULT_EXPIRATION_DURATION);
  }

  /**
   * Constructs a CasGetCmd instance.
   *
   * @param key a key string
   * @param type a Data Type
   * @param pass a password string
   * @param expirationDuration a duration to expire.
   */
  private CasGetCmd(String key, DataType type, String pass, long expirationDuration) {
    assert (key != null && !key.isEmpty());
    assert (expirationDuration >= 0);
    this.key = key;
    this.type = type;
    this.pass = pass;
    this.expirationDuration = expirationDuration;
  }

  /* -- Instance methods -- */
  /**
   * Gets a variable from a cluster using a CAS operation.
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
    byte[] val = null;
    switch (this.type.name()) {
        case "BYTE": {
            ByteByReference pval = new ByteByReference();
            isSuccess =
                    INSTANCE.k2hdkc_pm_cas8_str_get_wa(session.getHandle(), this.key, this.pass, pval);
            if (!isSuccess) {
                logger.error("INSTANCE.k2hdkc_pm_cas8_str_get_wa returns false");
            }
            val = new byte[]{pval.getValue()};
            break;
        }
        case "SHORT": {
            ShortByReference pval = new ShortByReference();
            isSuccess =
                    INSTANCE.k2hdkc_pm_cas16_str_get_wa(session.getHandle(), this.key, this.pass, pval);
            if (!isSuccess) {
                logger.error("INSTANCE.k2hdkc_pm_cas16_str_get_wa returns false");
            }
            short shortVal = pval.getValue();
            val = new byte[]{(byte) shortVal, (byte) (shortVal >> 8)}; // TODO little endian only

            break;
        }
        case "INT": {
            IntByReference pval = new IntByReference();
            isSuccess =
                    INSTANCE.k2hdkc_pm_cas32_str_get_wa(session.getHandle(), this.key, this.pass, pval);
            if (!isSuccess) {
                logger.error("INSTANCE.k2hdkc_pm_cas32_str_get_wa returns false");
            }
            int intVal = pval.getValue();
            val =
                    new byte[]{
                            (byte) intVal, (byte) (intVal >> 8), (byte) (intVal >> 8), (byte) (intVal >> 8)
                    }; // TODO little endian only

            break;
        }
        case "LONG": {
            LongByReference pval = new LongByReference();
            isSuccess =
                    INSTANCE.k2hdkc_pm_cas64_str_get_wa(session.getHandle(), this.key, this.pass, pval);
            if (!isSuccess) {
                logger.error("INSTANCE.k2hdkc_pm_cas64_str_get_wa returns false");
            }
            long longVal = pval.getValue();
            val =
                    new byte[]{
                            (byte) longVal,
                            (byte) (longVal >> 8),
                            (byte) (longVal >> 8),
                            (byte) (longVal >> 8),
                            (byte) (longVal >> 8),
                            (byte) (longVal >> 8),
                            (byte) (longVal >> 8),
                            (byte) (longVal >> 8)
                    }; // TODO little endian only

            break;
        }
        default:
            logger.error(
                    "invalid request. this.type.name {} type.ordinal() {}",
                    this.type.name(),
                    this.type.ordinal());
            throw new IllegalArgumentException("type name and val are invalid");
      }
    // String cmd, boolean isSuccess, long code, long detailCode) {
    long code = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    ByteArrayOutputStream bos = new ByteArrayOutputStream(val.length);
    bos.write(val);
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cBos = (T) bos;
    Result<T> result =
        Result.of(CasGetCmd.class.getSimpleName(), isSuccess, cBos, code, detailCode);
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
