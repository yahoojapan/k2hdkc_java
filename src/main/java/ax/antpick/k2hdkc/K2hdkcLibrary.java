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
 * This JNA interface provides functions in the <a
 * href="https://github.com/yahoojapan/k2hdkc/blob/master/lib/k2hdkc.h">k2hdkc</a> C library.
 *
 * <p>This interface is currently a package-private interface because I think it's hard for
 * implementators to use the <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> C functions very well.
 * I will move this interface to a public one if I could think it is much easier to use them.
 *
 * <p>Here is a pseudo code supposing the interface is public:
 *
 * <pre>{@code
 * import ax.antpick.k2hdkc.*;
 * import com.sun.jna.*;
 * import com.sun.jna.ptr.*;
 * import java.io.IOException;
 * import java.util.*;
 * import java.util.stream.*;
 *
 * public class App {
 *   public static void main(String[] args) {
 *     K2hdkcLibrary INSTANCE =
 *       (K2hdkcLibrary) Native.synchronizedLibrary(Native.load("k2hdkc", K2hdkcLibrary.class));
 *     short port = 8031;
 *     String cuk = "";
 *     long handle = INSTANCE.k2hdkc_open_chmpx_full("cluster/slave.yaml", port, cuk, true, true, true);
 *     boolean isSuccess = INSTANCE.k2hdkc_close_chmpx_ex(handle, true);
 *     System.out.println("isSuccess " + isSuccess);
 *   }
 * }
 * }</pre>
 *
 * @author Hirotaka Wakabayashi
 */
interface K2hdkcLibrary extends Library {
  /**
   * Opens a connection with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave process.
   *
   * @param file a path string of a <a href="https://chmpx.antpick.ax/">chmpx</a> configuration file
   * @param port a port string of a <a href="https://chmpx.antpick.ax/">chmpx</a> control port
   * @param cuk a cuk(Cloud Unique Key) string of a <a href="https://chmpx.antpick.ax/">chmpx</a>
   *     configuration file
   * @param isAutoRejoin <code>true</code> if a process connects once again with a <a
   *     href="https://chmpx.antpick.ax/">chmpx</a> process automatically. <code>false</code>
   *     otherwise.
   * @param isRetryRejoin <code>true</code> if a process connects again with a <a
   *     href="https://chmpx.antpick.ax/">chmpx</a> process automatically until it will succeed.
   *     <code> false </code> otherwise.
   * @param isClearBackupFile <code>true</code> if a process clears backup configuration files when
   *     closing <a href="https://chmpx.antpick.ax/">chmpx</a> connections. <code>false</code>
   *     otherwise.p
   * @return a connection handle with <a href="https://chmpx.antpick.ax/">chmpx</a> slave process
   */
  // extern k2hdkc_chmpx_h k2hdkc_open_chmpx_full(const char* config, short ctlport, const char*
  // cuk, bool is_auto_rejoin, bool no_giveup_rejoin, bool is_clean_bup);
  long k2hdkc_open_chmpx_full(
      String file,
      short port,
      String cuk,
      boolean isAutoRejoin,
      boolean isRetryRejoin,
      boolean isClearBackupFile);

  /**
   * Closes a connection wiwth a <a href="https://chmpx.antpick.ax/">chmpx</a> slave process.
   *
   * @param handle a connection handle with <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param isClearBackupFile <code>true</code> if a process clears backup configuration files when
   *     closing <a href="https://chmpx.antpick.ax/">chmpx</a> connections. <code>false</code>
   *     otherwise.
   * @return returns <code>true</code> if successfully close. <code>false</code> otherwise.
   */
  // extern bool k2hcdkc_close_chmpx_ex(k2hdkc_chmpx_h handle, bool is_clean_bup);
  boolean k2hdkc_close_chmpx_ex(long handle, boolean isClearBackupFile);

  /**
   * Retrieves the value of a key string
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @param pass a passphrase
   * @param ppval a pointer to the value
   * @return returns <code>true</code> if successfully close. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_get_str_value_wp(k2hdkc_chmpx_h handle, const char* pkey, const char*
  // encpass, char** ppval);
  boolean k2hdkc_pm_get_str_value_wp(
      long handle, String pkey, String pass, PointerByReference ppval);

  /**
   * Retrieves the value of a key string
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process.
   * @param pkey a key string
   * @param pval a value string
   * @param rmsubkeylist <code>true</code> if remove subkeys. <code>false</code> otherwise
   * @param pass a passphrase string
   * @param expiration a reference to expiration duration in second
   * @return returns <code>true</code> if successfully close. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_set_str_value_wa(k2hdkc_chmpx_h handle, const char* pkey, const char*
  // pval, bool rmsubkeylist, const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_set_str_value_wa(
      long handle,
      String pkey,
      String pval,
      boolean rmsubkeylist,
      String pass,
      LongByReference expiration);

  /**
   * Removes subkeys of a key.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process.
   * @param pkey a key string
   * @param pskeyarray subkeys of a key
   * @return returns <code>true</code> if successfully close. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_set_str_subkeys(k2hdkc_chmpx_h handle, const char* pkey, const char**
  // pskeyarray);
  boolean k2hdkc_pm_set_str_subkeys(long handle, String pkey, StringArray pskeyarray);

  /**
   * Sets subkeys of a key.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @param psubkey a subkey string
   * @param subkeyLength the length of a subkey string
   * @param removeRecursively {@code true} if removing any subkeys of a subkey.
   * @return returns <code>true</code> if successfully close. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_remove_str_subkey(k2hdkc_chmpx_h handle, const char* pkey, const char*
  // psubkey, size_t subkeylength, bool is_nest);
  boolean k2hdkc_pm_remove_str_subkey(
      long handle, String pkey, String psubkey, int subkeyLength, boolean removeRecursively);

  /**
   * Gets subkeys of a key.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @return pointer to a subkeys of a key
   */
  // extern char** k2hdkc_pm_get_str_direct_subkeys(k2hdkc_chmpx_h handle, const char* pkey);
  PointerByReference k2hdkc_pm_get_str_direct_subkeys(long handle, String pkey);

  /**
   * Removes a key and subkeys of the key
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process.
   * @param pkey a key string
   * @return returns <code>true</code> if successfully close. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_remove_str_all(k2hdkc_chmpx_h handle, const char* pkey);
  boolean k2hdkc_pm_remove_str_all(long handle, String pkey);

  /**
   * Clears subkeys of the key
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @return returns <code>true</code> if successfully close. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_clear_str_subkeys(k2hdkc_chmpx_h handle, const char* pkey);
  boolean k2hdkc_pm_clear_str_subkeys(long handle, String pkey);

  /**
   * Retrieves the latest response code from the k2hdkc server.
   *
   * @return response code
   */
  // extern dkcres_type_t k2hdkc_get_lastres_code(void);
  long k2hdkc_get_lastres_code();

  /**
   * Retrieves the latest response code in details from the k2hdkc server.
   *
   * @return response code
   */
  // extern dkcres_type_t k2hdkc_get_lastres_subcode(void);
  long k2hdkc_get_lastres_subcode();

  /**
   * Retrieves whether the latest request is succeeded.
   *
   * @return {@code true} if succeeded.
   */
  // extern bool k2hdkc_is_lastres_success(void);
  boolean k2hdkc_is_lastres_success();

  /**
   * Retrieves the response code from the k2hdkc server.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process.
   * @return response code
   */
  // extern dkcres_type_t k2hdkc_get_res_code(k2hdkc_chmpx_h handle);
  long k2hdkc_get_res_code(long handle);

  /**
   * Retrieves the response code in details from the k2hdkc server.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process.
   * @return response code
   */
  // extern dkcres_type_t k2hdkc_get_res_subcode(k2hdkc_chmpx_h handle);
  long k2hdkc_get_res_subcode(long handle);

  /**
   * Retrieves whether the request is succeeded.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process.
   * @return {@code true} if succeeded.
   */
  // extern bool k2hdkc_is_res_success(k2hdkc_chmpx_h handle);
  boolean k2hdkc_is_res_success(long handle);

  /**
   * Sets the level of the importance of the message higher by receiving a SIGUSR1 signal. If the
   * level reachs the highest, the level falls back to the lowest level.
   */
  // extern void k2hdkc_bump_debug_level(void);
  void k2hdkc_bump_debug_level();

  /**
   * Sets the level of the importance of the message is the "silent" level which is the highest
   * level
   */
  // extern void k2hdkc_set_debug_level_silent(void);
  void k2hdkc_set_debug_level_silent();

  /** Sets the level of the importance of the message is the "error" level */
  // extern void k2hdkc_set_debug_level_error(void);
  void k2hdkc_set_debug_level_error();

  /** Sets the level of the importance of the message is the "warning" level */
  // extern void k2hdkc_set_debug_level_warning(void);
  void k2hdkc_set_debug_level_warning();

  /** Sets the level of the importance of the message is the "info" level */
  // extern void k2hdkc_set_debug_level_message(void);
  void k2hdkc_set_debug_level_message();

  /**
   * Sets the level of the importance of the message is the "dump" level which is the lowest level
   */
  // extern void k2hdkc_set_debug_level_dump(void);
  void k2hdkc_set_debug_level_dump();

  /**
   * Sets the log file name.
   *
   * @param filepath A log file string to put logs.
   * @return <code>true</code> if set the debug file, <code>false</code> otherwise
   */
  // extern bool k2hdkc_set_debug_file(const char* filepath);
  boolean k2hdkc_set_debug_file(String filepath);

  /**
   * Sets the log file to the standard error(stderr).
   *
   * @return <code>true</code> if unset the debug file, <code>false</code> otherwise
   */
  // extern bool k2hdkc_unset_debug_file(void);
  boolean k2hdkc_unset_debug_file();

  /**
   * Sets the level of the importance of the message by referring the K2HDBGMODE enviroment and sets
   * a log file by referring the K2HDBGFILE enviroment.
   *
   * @return <code>true</code> if load debug environments, <code>false</code> otherwise
   */
  // extern bool k2hdkc_load_debug_env(void);
  boolean k2hdkc_load_debug_env();

  /**
   * Retrieves whether the communication log is enabled
   *
   * @return {@code true} if enabled
   */
  // extern bool k2hdkc_is_enable_comlog(void);
  boolean k2hdkc_is_enable_comlog();

  /** Enables the communication log. */
  // extern void k2hdkc_enable_comlog(void);
  void k2hdkc_enable_comlog();

  /** Disables the communication log. */
  // extern void k2hdkc_disable_comlog(void);
  void k2hdkc_disable_comlog();

  /** Switches the communication logging enable/disable */
  // extern void k2hdkc_toggle_comlog(void);
  void k2hdkc_toggle_comlog();

  /**
   * Gets Attributes of a key.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @param pattrspckcnt the number of existing attributes.
   * @return pointer to a set of attributes of a key
   */
  // extern PK2HDKCATTRPCK k2hdkc_pm_get_str_direct_attrs(k2hdkc_chmpx_h handle, const char* pkey,
  // int* pattrspckcnt);
  K2hashAttrPack k2hdkc_pm_get_str_direct_attrs(
      long handle, String pkey, IntByReference pattrspckcnt);

  /**
   * Removes a key
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_remove_str(k2hdkc_chmpx_h handle, const char* pkey);
  boolean k2hdkc_pm_remove_str(long handle, String pkey);

  /**
   * Rename a key
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param key key string
   * @param newKey a key string
   * @param parentKey a parent key string
   * @param checkParentAttrs <code>true</code> if checking a parent attribute before changing a
   *     subkeys. <code>false</code> otherwise.
   * @param password a password string
   * @param expirationDuration a duration of data expiration
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_rename_with_parent_str_wa(k2hdkc_chmpx_h handle, const char* poldkey,
  // const char* pnewkey, const char* pparentkey, bool checkattr, const char* encpass, const time_t*
  // expire);n
  boolean k2hdkc_pm_rename_with_parent_str_wa(
      long handle,
      String key,
      String newKey,
      String parentKey,
      boolean checkParentAttrs,
      String password,
      LongByReference expirationDuration);

  /**
   * Retrieves the value of a key string
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @param pval a value string
   * @param pskeyarray subkeys of a key
   * @param pass a passphrase string
   * @param expirationDuration a reference to expiration duration in second
   * @return returns <code>true</code> if successfully close. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_set_str_all_wa(k2hdkc_chmpx_h handle, const char* pkey, const char* pval,
  // const char** pskeyarray, const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_set_str_all_wa(
      long handle,
      String pkey,
      String pval,
      StringArray pskeyarray,
      String pass,
      LongByReference expirationDuration);

  /**
   * Inserts a value to a tail of a queue.
   *
   * @param handle a <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> queue handle
   * @param pprefix a prefix string
   * @param pkey a key string
   * @param pval a value string
   * @param isFifo <code>true</code> if a fifo queue. <code>false</code> otherwise.
   * @param checkParentAttrs <code>true</code> if checking a parent attribute before changing a
   *     subkeys. <code>false</code> otherwise.
   * @param password a password string
   * @param expirationDuration a duration of data expiration
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_keyq_str_push_wa(k2hdkc_chmpx_h handle, const char* pprefix, const char*
  // pkey, const char* pval, bool is_fifo, bool checkattr, const char* encpass, const time_t*
  // expire);
  boolean k2hdkc_pm_keyq_str_push_wa(
      long handle,
      String pprefix,
      String pkey,
      String pval,
      boolean isFifo,
      boolean checkParentAttrs,
      String password,
      LongByReference expirationDuration);

  /**
   * Inserts a value to a tail of a queue.
   *
   * @param handle a <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> queue handle
   * @param pprefix a prefix string
   * @param pval a value string
   * @param isFifo <code>true</code> if a fifo queue. <code>false</code> otherwise.
   * @param checkParentAttrs <code>true</code> if checking a parent attribute before changing a
   *     subkeys. <code>false</code> otherwise.
   * @param password a password string
   * @param expirationDuration a duration of data expiration
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_q_str_push_wa(k2hdkc_chmpx_h handle, const char* pprefix, const char*
  // pval, bool is_fifo, bool checkattr, const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_q_str_push_wa(
      long handle,
      String pprefix,
      String pval,
      boolean isFifo,
      boolean checkParentAttrs,
      String password,
      LongByReference expirationDuration);

  /**
   * Removes a value from a tail of a queue.
   *
   * @param handle a <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> queue handle.
   * @param pprefix a prefix string.
   * @param isFifo <code>true</code> if a fifo queue. <code>false</code> otherwise.
   * @param password a password string
   * @param ppval a pointer to a value string.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_q_str_pop_wp(k2hdkc_chmpx_h handle, const char* pprefix, bool is_fifo,
  // const char* encpass, const char** ppval);
  boolean k2hdkc_pm_q_str_pop_wp(
      long handle, String pprefix, boolean isFifo, String password, PointerByReference ppval);

  /**
   * Removes a value to a tail of a queue.
   *
   * @param handle a <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> queue handle.
   * @param pprefix a prefix string.
   * @param isFifo <code>true</code> if a fifo queue. <code>false</code> otherwise.
   * @param password a password string.
   * @param ppkey a pointer to a key string.
   * @param ppval a pointer to a value string.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_keyq_str_pop_wp(k2hdkc_chmpx_h handle, const char* pprefix, bool is_fifo,
  // const char* encpass, const char** ppkey, const char** ppval);
  boolean k2hdkc_pm_keyq_str_pop_wp(
      long handle,
      String pprefix,
      boolean isFifo,
      String password,
      PointerByReference ppkey,
      PointerByReference ppval);

  /**
   * Removes the number of values from a tail of a queue.
   *
   * @param handle a <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> queue handle.
   * @param pprefix a prefix string.
   * @param count the number of count to remove.
   * @param isFifo <code>true</code> if a fifo queue. <code>false</code> otherwise.
   * @param password a password string
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_q_str_remove_wp(k2hdkc_chmpx_h handle, const char* pprefix, int count,
  // bool is_fifo, const char* encpass);
  boolean k2hdkc_pm_q_str_remove_wp(
      long handle, String pprefix, int count, boolean isFifo, String password);

  /**
   * Removes the number of values from a tail of a queue.
   *
   * @param handle a <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> queue handle.
   * @param pprefix a prefix string.
   * @param count the number of count to remove.
   * @param isFifo <code>true</code> if a fifo queue. <code>false</code> otherwise.
   * @param password a password string
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_keyq_str_remove_wp(k2hdkc_chmpx_h handle, const char* pprefix, int count,
  // bool is_fifo, const char* encpass);
  boolean k2hdkc_pm_keyq_str_remove_wp(
      long handle, String pprefix, int count, boolean isFifo, String password);

  /**
   * Initialize a 64bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @param val a value string
   * @param password a password string
   * @param expirationDuration a duration to expire data
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // 1. CAS INIT
  // extern bool k2hdkc_pm_cas64_str_init_wa(k2hdkc_chmpx_h handle, const char* pkey, uint64_t val,
  // const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_cas64_str_init_wa(
      long handle, String pkey, long val, String password, LongByReference expirationDuration);

  /**
   * Initialize a 32bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @param val a value string
   * @param password a password string
   * @param expirationDuration a duration to expire data
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_cas32_str_init_wa(k2hdkc_chmpx_h handle, const char* pkey, uint32_t val,
  // const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_cas32_str_init_wa(
      long handle, String pkey, int val, String password, LongByReference expirationDuration);

  /**
   * Initialize a 16bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @param val a value string
   * @param password a password string
   * @param expirationDuration a duration to expire data
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_cas16_str_init_wa(k2hdkc_chmpx_h handle, const char* pkey, uint16_t val,
  // const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_cas16_str_init_wa(
      long handle, String pkey, short val, String password, LongByReference expirationDuration);

  /**
   * Initialize an 8bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @param val a value string
   * @param password a password string
   * @param expirationDuration a duration to expire data
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_cas8_str_init_wa(k2hdkc_chmpx_h handle, const char* pkey, uint8_t val,
  // const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_cas8_str_init_wa(
      long handle, String pkey, byte val, String password, LongByReference expirationDuration);

  /**
   * Retrieves a 64bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string
   * @param password a password string
   * @param pval a pointer to a 64bit variable.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // 2. CAS GET
  // extern bool k2hdkc_pm_cas64_str_get_wa(k2hdkc_chmpx_h handle, const char* pkey, const char*
  // encpass, uint64_t* pval);
  boolean k2hdkc_pm_cas64_str_get_wa(
      long handle, String pkey, String password, LongByReference pval);

  /**
   * Retrieves a 32bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string.
   * @param password a password string.
   * @param pval a pointer to a 32bit variable.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_cas32_str_get_wa(k2hdkc_chmpx_h handle, const char* pkey, const char*
  // encpass, uint32_t* pval);
  boolean k2hdkc_pm_cas32_str_get_wa(
      long handle, String pkey, String password, IntByReference pval);

  /**
   * Retrieves a 16bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string.
   * @param password a password string.
   * @param pval a pointer to a 16bit variable.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_cas16_str_get_wa(k2hdkc_chmpx_h handle, const char* pkey, const char*
  // encpass, uint16_t* pval);
  boolean k2hdkc_pm_cas16_str_get_wa(
      long handle, String pkey, String password, ShortByReference pval);

  /**
   * Retrieves a 8bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string.
   * @param password a password string.
   * @param pval a pointer to a 8bit value.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_cas8_str_get_wa(k2hdkc_chmpx_h handle, const char* pkey, const char*
  // encpass, uint8_t* pval);
  boolean k2hdkc_pm_cas8_str_get_wa(
      long handle, String pkey, String password, ByteByReference pval);

  /**
   * Sets a 64bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string.
   * @param oldval a long value.
   * @param newval a long value.
   * @param password a password string.
   * @param expirationDuration a duration to expire data.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // 3. CAS_SET
  // extern bool k2hdkc_pm_cas64_str_set_wa(k2hdkc_chmpx_h handle, const char* pkey, uint64_t
  // oldval, uint64_t newval, const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_cas64_str_set_wa(
      long handle,
      String pkey,
      long oldval,
      long newval,
      String password,
      LongByReference expirationDuration);

  /**
   * Sets a 32bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string.
   * @param oldval a int value.
   * @param newval a int value.
   * @param password a password string.
   * @param expirationDuration a duration to expire data.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_cas32_str_set_wa(k2hdkc_chmpx_h handle, const char* pkey, uint32_t
  // oldval, uint32_t newval, const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_cas32_str_set_wa(
      long handle,
      String pkey,
      int oldval,
      int newval,
      String password,
      LongByReference expirationDuration);

  /**
   * Sets a 16bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string.
   * @param oldval a short value.
   * @param newval a short value.
   * @param password a password string.
   * @param expirationDuration a duration to expire data.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_cas16_str_set_wa(k2hdkc_chmpx_h handle, const char* pkey, uint16_t
  // oldval, uint16_t newval, const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_cas16_str_set_wa(
      long handle,
      String pkey,
      short oldval,
      short newval,
      String password,
      LongByReference expirationDuration);

  /**
   * Sets a 8bit CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string.
   * @param oldval a 8bit value.
   * @param newval a 8bit value.
   * @param password a password string.
   * @param expirationDuration a duration to expire data.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_cas8_str_set_wa(k2hdkc_chmpx_h handle, const char* pkey, uint8_t oldval,
  // uint8_t newval, const char* encpass, const time_t* expire);
  boolean k2hdkc_pm_cas8_str_set_wa(
      long handle,
      String pkey,
      byte oldval,
      byte newval,
      String password,
      LongByReference expirationDuration);

  /**
   * Increments a CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string.
   * @param password a password string.
   * @param expirationDuration a duration to expire data.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // 4. CAS INICREMENT
  // extern bool k2hdkc_pm_cas_str_increment_wa(k2hdkc_chmpx_h handle, const char* pkey, const char*
  // encpass, const time_t* expire);
  boolean k2hdkc_pm_cas_str_increment_wa(
      long handle, String pkey, String password, LongByReference expirationDuration);

  /**
   * Decrements a CAS variable.
   *
   * @param handle a connection handle with a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
   *     process
   * @param pkey a key string.
   * @param password a password string.
   * @param expirationDuration a duration to expire data.
   * @return <code>true</code> if success. <code>false</code> otherwise.
   */
  // extern bool k2hdkc_pm_cas_str_decrement_wa(k2hdkc_chmpx_h handle, const char* pkey, const char*
  // encpass, const time_t* expire);
  boolean k2hdkc_pm_cas_str_decrement_wa(
      long handle, String pkey, String password, LongByReference expirationDuration);
}
