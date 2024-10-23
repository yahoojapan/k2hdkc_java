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

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds common data types, members and instance methods of derived classes.
 *
 * @author Hirotaka Wakabayashi
 */
public class Result<T> {

  /* -- private instance members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(Result.class);

  /** The request command name */
  private String cmd = null;
  /** The result in boolean */
  private boolean isSuccess = false;
  /** The result data object */
  private final T value;
  /** The value the k2hdkc_get_res_code() returns */
  private long code = 0L;
  /** The value the k2hdkc_get_res_subcode() returns */
  private long detailCode = 0L;

  /* -- Constructors -- */
  /**
   * Creates a Result instance.
   *
   * @param cmd a command name
   * @param isSuccess <code>true</code> if success. <code>false</code> otherwise
   * @param value a response data in binary format
   * @param code a response code
   * @param detailCode a detail response code
   */
  private Result(String cmd, boolean isSuccess, T value, long code, long detailCode) {
    this.cmd = cmd;
    this.isSuccess = isSuccess;
    this.value = value;
    this.code = code;
    this.detailCode = detailCode;
  }

  /* -- public Static methods -- */
  /**
   * Creates a Result instance.
   *
   * @param cmd a command name
   * @param isSuccess <code>true</code> if success. <code>false</code> otherwise
   * @param value a response data in binary format
   * @param code a response code
   * @param detailCode a detail response code
   * @return a new Result instance
   */
  public static <T> Result<T> of(
      String cmd, boolean isSuccess, T value, long code, long detailCode) {
    return new Result<>(cmd, isSuccess, value, code, detailCode);
  }

  /**
   * Returns <code>true</code> if the request result is success. <code>false</code> otherwise.
   *
   * @return <code>true</code> if the request result is success. <code>false</code> otherwise.
   */
  public boolean isSuccess() {
    return this.isSuccess;
  }

  /**
   * Retrieves the command name in String representation.
   *
   * @return a command string
   */
  public String getCmd() {
    return this.cmd;
  }

  /**
   * Returns a value
   *
   * @return a value
   */
  public T getValue() {
    return this.value;
  }
  /**
   * Retrieves the latest response code from the k2hdkc server.
   *
   * @return a response code from the k2hdkc server
   */
  public long getCode() {
    return this.code;
  }
  /**
   * Retrieves the latest response code in details from the k2hdkc server.
   *
   * @return a response code(subcoe) from the k2hdkc server
   */
  public long getDetailCode() {
    return this.detailCode;
  }

  /**
   * Returns full of members as a string.
   *
   * @return full of members as a string in a key=value manner
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getName());
    sb.append("[");
    for (Field field : this.getClass().getSuperclass().getDeclaredFields()) {
      field.setAccessible(true);
      sb.append(field.getName());
      sb.append("=");
      try {
        sb.append(field.get(this));
      } catch (IllegalArgumentException | IllegalAccessException ex) {
        sb.append("null");
      }
      sb.append(",");
    }
    sb.append("]");
    return sb.toString();
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
