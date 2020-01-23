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

import com.sun.jna.*;
import com.sun.jna.ptr.*;

/**
 * This JNA interface provides functions in the standard C library.
 *
 * <p><b>An Usage Example:</b>
 *
 * <p>Suppose you want to set the K2HDBGMODE enviroment "INFO" and print it, you could write this
 * as:
 *
 * <pre>{@code
 * package com.example;
 *
 * import ax.antpick.k2hdkc.*;
 * import com.sun.jna.*;
 * import com.sun.jna.ptr.*;
 * import java.util.*;
 * import java.util.stream.*;
 * import org.slf4j.Logger;
 * import org.slf4j.LoggerFactory;
 *
 * public class App {
 *   private static final Logger logger = LoggerFactory.getLogger(App.class);
 *
 *   public static void main(String[] args) {
 *     CLibrary INSTANCE =
 *         (CLibrary) Native.synchronizedLibrary(Native.loadLibrary("c", CLibrary.class));
 *     INSTANCE.setenv("K2HDBGMODE", "INFO", 1);
 *     System.out.println(INSTANCE.getenv("K2HDBGMODE"));
 *   }
 * }
 * }</pre>
 *
 * @author Hirotaka Wakabayashi
 */
public interface CLibrary extends Library {
  /**
   * Frees the memory space pointed to by ptr.
   *
   * @param ptr a pointer to be free
   */
  // void free(void *ptr); // should we use the jna's Native.free(long ptr) ?
  void free(Pointer ptr);

  /**
   * Adds an enviroments.
   *
   * @param name an environment string
   * @param value the value
   * @param overwrite if the {@code name} already exists in the environment, then its {@code value}
   *     is changed to {@code value} if {@code overwrite} is nonzero. If {@code overwrite} is zero,
   *     then the {@code value} of {@code name} is not changed.
   * @return returns zero on success, or -1 on error, with errno set to indicate the cause of the
   *     error.
   */
  // int setenv(const char *name, const char *value, int overwrite);
  int setenv(String name, String value, int overwrite);

  /**
   * Deletes an variable name from the environment.
   *
   * @param name an environment string
   * @return returns zero on success, or -1 on error, with errno set to indicate the cause of the
   *     error.
   */
  // int unsetenv(const char *name);
  int unsetenv(String name);

  /**
   * Gets the value of an enviroment.
   *
   * @param name an environment string
   * @return the value of an enviroment.
   */
  // char *getenv(const char *name);
  String getenv(String name);

  /**
   * Opens a file.
   *
   * @param path a path string
   * @param mode a mode string
   * @return a pointer to a FIlE structure
   */
  // FILE *fopen(const char *path, const char *mode);
  Pointer fopen(String path, String mode);

  /**
   * Closes a file.
   *
   * @param fp a pointer to a FILE strucutre
   * @return 0 if success. Otherwise, EOF is returned and errno is set to indicate the error.
   */
  // int fclose(FILE *fp);
  int fclose(Pointer fp);
}
