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
 * Represents an operation of a <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> cluster. Each of a
 * <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> Command class implements the {@code execute}
 * method. The {@link Cmd#execute} method accepts a {@link Session} argument and returns a {@link
 * Result} instance.
 *
 * <p>This interface is currently a package-private interface because I think it's hard for
 * implementators to use the <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> C functions very well.
 * I will move this interface to a public one if I could think it is much easier to use them.
 *
 * <p>Here is a pseudo code implements the interface:
 *
 * <pre>
 * public class ExampleCmd extends CmdBase implements Cmd {
 *   private final String key;
 *   public SampleCmd(String key) {
 *     this.key = key;
 *   }
 *  {@literal @Override}
 *   public Optional{@literal Optional<Result<T>>} execute(Session session) throws IOException {
 *
 *     // get value from cluster.
 *     String rval = null;
 *     String password = null;
 *     PointerByReference ppval = new PointerByReference();
 *     // Call the k2hdkc C function
 *     boolean isSuccess = INSTANCE.k2hdkc_pm_get_str_value_wp(session.getHandle(), this.key, password, ppval);
 *
 *     {@literal Result<T>} result = Result.of(SampleCmd.class.getSimpleName(), isSuccess, (T)Boolean.valueOf(isSuccess), 0, 0);
 *     return Optional.of(result);
 *   }
 * }</pre>
 *
 * <p>Or, you can implement the interface as a Functional interface. Supposing the interface is a
 * public, you could write this:
 *
 * <pre>
 * try (
 *   Cluster c = Cluster.of("slave.ini");
 *   Session s = Session.of(); ) {
 *  {@literal Cmd<Session, Optional<Result<T>>> cmd = s ->} {
 *     try {
 *       K2hdkcLibrary INSTANCE = Session.getLibrary(); // can throws IOException
 *       boolean b = INSTANCE.k2hdkc_pm_set_str_value_wa(s.getHandle(), "key", "value", false, null, null);
 *       {@literal Result<T>} rb = Result.of("set", b, (T)Boolean.valueOf(b), 0, 0);
 *       return Optional.of(rb);
 *     } catch (IOException ex) {
 *       return Optional.empty();
 *     }
 *  };
 *   System.out.println(cmd.execute(s));
 * } catch (IOException ex) {
 *   System.err.println(ex.getMessage());
 * }
 * </pre>
 *
 * @author Hirotaka Wakabayashi
 */
public interface Cmd<T> {
  /** Default removing subkeys recursively is <code>false</code>. */
  public static final boolean DEFAULT_REMOVE_RECURSIVELY = false;
  /** Default clear all subkeys is <code>false</code>. */
  public static final boolean DEFAULT_IS_CLEAR_SUBKEYS = false;
  /** Default clear all subkeys is <code>false</code>. */
  public static final String[] DEFAULT_SUBKEYS = null;
  /** Default parent string is <code>null</code>. */
  public static String DEFAULT_PARENT_KEY = null;
  /** Default clear all subkeys is <code>false</code>. */
  public static final boolean DEFAULT_IS_CHECK_PARENT_ATTRS = false;
  /** Default pass is null. */
  public static final int DEFAULT_REMOVE_ELEMENT_SIZE = 1;
  /** Default isReturnValue is <code>true</code>. */
  public static boolean DEFAULT_NEED_RETURN_VALUE = true;
  /** Default isFifo is <code>true</code>. */
  public static boolean DEFAULT_IS_FIFO = true;
  /** Default checkParentAttrs is <code>true</code>. */
  public static boolean DEFAULT_CHECK_PARENT_ATTRS = true;
  /** Default isIncrement is <code>true</code>. */
  public static boolean DEFAULT_IS_INCREMENT = true;
  /** Default passphrase string is <code>null</code>. */
  public static String DEFAULT_PASS = null;
  /** Default data expiration duration is <code>0</code>. */
  public static long DEFAULT_EXPIRATION_DURATION = 0L;
  /** DataType of data in CAS operation */
  public enum DataType {
    /** one byte(8 bits) */
    BYTE(1),
    /** two bytes(16 bits) */
    SHORT(2),
    /** 4 bytes(32 bits) */
    INT(4),
    /** 8 bytes(64 bits) */
    LONG(8);
    /** data length of each member */
    int bytes;
    /**
     * DataType constructor.
     *
     * @param bytes bytes of the member
     */
    private DataType(int bytes) {
      this.bytes = bytes;
    }
  }

  /**
   * Executes a command and return a result.
   *
   * @param session a {@link Session} instance.
   * @throws IOException if the native C library file can't be open.
   * @return a Optional instance.
   */
  <T> Optional<Result<T>> execute(Session session) throws IOException;

  /**
   * Packs a byte array to integer. People who use the CAS command family can use this helper
   * function(implicitly public method).
   *
   * @param b a byte array
   * @return a interger variable
   */
  public static int bytesToInt(byte[] b) {
    if (b.length != 4) {
      throw new IllegalArgumentException("");
    }
    return (b[0] << 24) | (b[1] << 16) | (b[2] << 8) | (b[3]);
  }

  /**
   * Returns a value as a integer value.
   *
   * @param value a byte array
   * @return a value in integer
   * @throws IllegalStateException this.value is not valid integer.
   */
  public static int getValusAsInt(byte[] value) {
    if (value.length != 4) {
      CmdLogger.logger.error("this.value.length != 4 {}, shoule be == 4", value.length);
      throw new IllegalStateException(
          "this.value seems not to be an integer value " + value.length);
    }
    int rval =
        value[3] << 24 | (value[2] & 0xFF) << 16 | (value[1] & 0xFF) << 8 | (value[0] & 0xFF);
    // int rval = ByteBuffer.wrap(this.value).getInt();
    CmdLogger.logger.debug("rval {}", rval);
    if (rval < 0) {
      CmdLogger.logger.error("rval < 0 {}, shoule be >= 0", value.toString());
      throw new IllegalStateException("rval < 0, should be >= 0");
    }
    return rval;
  }
}

final class CmdLogger {
  static final Logger logger = LoggerFactory.getLogger(Cmd.class);
}
