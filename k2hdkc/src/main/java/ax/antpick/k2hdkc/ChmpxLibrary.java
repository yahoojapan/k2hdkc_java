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

import com.sun.jna.Library;

/**
 * This JNA interface provides functions defined in the <a
 * href="https://chmpx.antpick.ax/">chmpx</a> C library. This class is a package-private class.
 *
 * <p><b>An Usage Example(for this library maitainers):</b>
 *
 * <p>Suppose you want to set the K2HDBGMODE enviroment "INFO". You could write this as:
 *
 * <pre>{@code
 * package ax.antpick;
 *
 * import com.sun.jna.*;
 * import com.sun.jna.ptr.*;
 *
 * public class App {
 *   public static void main(String[] args) {
 *     ChmpxLibrary INSTANCE =
 *         (ChmpxLibrary) Native.synchronizedLibrary(Native.loadLibrary("chmpx", ChmpxLibrary.class));
 *     INSTANCE.chmpx_set_debug_level_message();
 *   }
 * }
 * }</pre>
 *
 * @author Hirotaka Wakabayashi
 */
interface ChmpxLibrary extends Library {
  /**
   * Increases the level of the importance of the message. If the current level is the highest one,
   * the next level falls back to the lowest one.
   */
  void chmpx_bump_debug_level();

  /**
   * Changes the level of the importance of the message to the "silent" level which is the highest
   * level. No logs write even if library detects critical errors.
   */
  void chmpx_set_debug_level_silent();

  /**
   * Changes the level of the importance of the message to the "error" level. You may take action
   * immediately if you see error messages.
   */
  void chmpx_set_debug_level_error();

  /**
   * Changes the level of the importance of the message to the "warning" level. In addition to error
   * logs, warning logs write. A warning is not an error but it can be an error in some cases. You
   * should examine it.
   */
  void chmpx_set_debug_level_warning();

  /**
   * Changes the level of the importance of the message to the "info" level. In addition to warning
   * and error logs, informational logs write.
   */
  void chmpx_set_debug_level_message();

  /**
   * Changes the level of the importance of the message to the "dump" level which is the lowest
   * level. Many logs write for debugging. You should not use in production in most cases.
   */
  void chmpx_set_debug_level_dump();

  /**
   * Sets the log file. The default log file is stderr.
   *
   * @param filepath A filepath string
   * @return <code>true</code> if set the debug file, <code>false</code> otherwise
   */
  boolean chmpx_set_debug_file(String filepath);

  /**
   * Changes the log file to stderr.
   *
   * @return <code>true</code> if unset the debug file, <code>false</code> otherwise
   */
  boolean chmpx_unset_debug_file();

  /**
   * Determines the level of importance and the log file by referring environments.
   *
   * <p>K2HDBGMODE ... the environment for the level of importance.
   *
   * <p>K2HDBGFILE ... the environment for the log file.
   *
   * @return <code>true</code> if load debug environments, <code>false</code> otherwise
   */
  boolean chmpx_load_debug_env();
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
