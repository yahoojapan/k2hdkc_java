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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrievs attributes of a key.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Suppose you want to get attributes of "key" from a <a
 * href="https://k2hdkc.antpick.ax/">k2hdkc</a> cluster. You could write this as:
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
 *       GetAttrsCmd get = GetAttrsCmd.of("key");
 *       Map<String, String> map = (HashMap<String, String>) get.execute(s).get().getValue();
 *       System.out.println(map.toString());
 *     } catch (IOException ex) {
 *       System.out.println(ex.getMessage());
 *       assert (false);
 *     }
 *   }
 * }
 * }</pre>
 *
 * @author Hirotaka Wakabayashi
 */
public class GetAttrsCmd extends CmdBase implements Cmd<Map<String, String>> {

  /* -- private Static members -- */
  /** A logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(GetAttrsCmd.class);

  /* -- private instance members -- */
  /** The key to retrieve. */
  private final String key;

  /* -- Constructors -- */
  /**
   * Constructs a GetAttrsCmd instance.
   *
   * @param key a key string
   * @return a GetAttrsCmd instance.
   * @throws IllegalArgumentException if a key string is null or empty
   */
  public static GetAttrsCmd of(String key) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key should not be null or empty");
    }
    return new GetAttrsCmd(key);
  }

  /**
   * Constructs a GetAttrCmd instance.
   *
   * @param key a key string
   * @throws IllegalArgumentException if a key string is null or empty
   */
  private GetAttrsCmd(String key) {
    assert (key != null && !key.isEmpty());
    this.key = key;
  }

  /* -- Instance methods -- */
  /**
   * Retrievs attributes of a key.
   *
   * @param session a {@link Session}on instance
   * @return the result instance
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
    assert (this.key != null && !this.key.isEmpty());

    IntByReference pattrspckcnt = new IntByReference();
    K2hashAttrPack ppattrspck =
        INSTANCE.k2hdkc_pm_get_str_direct_attrs(session.getHandle(), this.key, pattrspckcnt);

    if (pattrspckcnt.getValue() == 0) {
      logger.warn("no attribute exists");
      return Optional.empty();
    }
    K2hashAttrPack[] attrs = (K2hashAttrPack[]) ppattrspck.toArray(pattrspckcnt.getValue());
    // Note: We must copy data because attrs must be free later.
    K2hashAttrPack[] newAttrs = Arrays.copyOfRange(attrs, 0, attrs.length);

    Map<String, String> map = new HashMap<>();
    Stream<K2hashAttrPack> stream = Stream.of(newAttrs);
    stream.forEach(
        attr -> {
          map.put(attr.pkey, attr.pval);
        });
    K2HASH_INSTANCE.k2h_free_attrpack(attrs, pattrspckcnt.getValue());

    // String cmd, boolean isSuccess, String value, long code, long detailCode) {
    long resCode = INSTANCE.k2hdkc_get_res_code(session.getHandle());
    long detailCode = INSTANCE.k2hdkc_get_res_subcode(session.getHandle());
    // Suppresses warnings because caller knows which type of T
    @SuppressWarnings("unchecked")
    T cMap = (T) map;
    Result<T> result =
        Result.of(GetAttrsCmd.class.getSimpleName(), true, cMap, resCode, detailCode);
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
