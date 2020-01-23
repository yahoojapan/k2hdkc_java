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
 * This JNA interface provides functions defined in the <a
 * href="https://k2hash.antpick.ax/">k2hash</a> C library. This class is a package-private class.
 *
 * <p><b>An Usage Example:</b>
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
 *     K2hashLibrary INSTANCE =
 *         (K2hashLibrary) Native.synchronizedLibrary(Native.loadLibrary("k2hash", CLibrary.class));
 *     INSTANCE.k2h_set_debug_level_message();
 *   }
 * }
 * }</pre>
 *
 * @author Hirotaka Wakabayashi
 */
interface K2hashLibrary extends Library {
  /** Raises the <a href="https://k2hash.antpick.ax/">k2hash</a>'s log level. */
  void k2h_bump_debug_level();

  /**
   * Sets the <a href="https://k2hash.antpick.ax/">k2hash</a>'s level of the importance of the
   * message is the "silent" level which is the highest level
   */
  void k2h_set_debug_level_silent();

  /**
   * Sets the <a href="https://k2hash.antpick.ax/">k2hash</a>'s level of the importance of the
   * message is the "error" level or higher.
   */
  void k2h_set_debug_level_error();

  /**
   * Sets the <a href="https://k2hash.antpick.ax/">k2hash</a>'s level of the importance of the
   * message is the "warning" level or higher.
   */
  void k2h_set_debug_level_warning();

  /**
   * Sets the <a href="https://k2hash.antpick.ax/">k2hash</a>'s level of the importance of the
   * message is the "dump" level which is the lowest level.
   */
  void k2h_set_debug_level_message();

  /**
   * Sets the <a href="https://k2hash.antpick.ax/">k2hash</a>'s log file name.
   *
   * @param filepath A log file string to put logs.
   * @return <code>true</code> if set the debug file, <code>false</code> otherwise
   */
  boolean k2h_set_debug_file(String filepath);

  /**
   * Sets the <a href="https://k2hash.antpick.ax/">k2hash</a>'s log file to the standard
   * error(stderr).
   *
   * @return <code>true</code> if unset the debug file, <code>false</code> otherwise
   */
  boolean k2h_unset_debug_file();

  /**
   * Sets the <a href="https://k2hash.antpick.ax/">k2hash</a>'s level of the importance of the
   * message by referring the K2HDBGMODE enviroment and sets a log file by referring the K2HDBGFILE
   * enviroment.
   *
   * @return <code>true</code> if load debug environments, <code>false</code> otherwise
   */
  boolean k2h_load_debug_env();

  /**
   * Sets the <a href="https://k2hash.antpick.ax/">k2hash</a>'s level of the importance of the
   * message higher by receiving a SIGUSR1 signal. If the level reachs the highest, the level falls
   * back to the lowest level.
   *
   * @return <code>true</code> if set the signal handler, <code>false</code> otherwise
   */
  boolean k2h_set_bumup_debug_signal_user1();

  /**
   * Frees the address of the K2HKEYPCK pointer.
   *
   * @param pkeys a pointer to an array of a pointer of a subkey string
   * @param keycnt the number of subkeys
   * @return <code>true</code> if success <code>false</code> otherwise
   */
  // extern bool k2h_free_keypack(PK2HKEYPCK pkeys, int keycnt);
  boolean k2h_free_keypack(PointerByReference pkeys, int keycnt);

  /**
   * Frees the array of a pointer of a subkey string.
   *
   * @param pkeys a pointer to an array of a pointer of a subkey string
   * @return <code>true</code> if success <code>false</code> otherwise
   */
  // extern bool k2h_free_keyarray(char** pkeys);
  boolean k2h_free_keyarray(String[] pkeys);

  /**
   * Frees the address of the K2HATTRPCK pointer.
   *
   * @param pattrs an array of a pointer of an attribute pack structure
   * @param attrcnt the number of attribure structures
   * @return <code>true</code> if success <code>false</code> otherwise
   */
  // extern bool k2h_free_attrpack(PK2HATTRPCK pattrs, int attrcnt);
  boolean k2h_free_attrpack(K2hashAttrPack[] pattrs, int attrcnt);
}
