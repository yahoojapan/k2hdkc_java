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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.jna.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Unit test for simple App. */
public class CmdTest {
  private static final Logger logger = LoggerFactory.getLogger(CmdTest.class);
  private static final String SLAVE_CLUSTER_CONFIG = "cluster/slave.yaml";
  private static final String[] TEST_KEY_ARRAY = {"testGetValueArg1", "testSetValueArg2"};

  @BeforeEach
  public void setUp() {
    File fileDb = new File(SLAVE_CLUSTER_CONFIG);
    if (!fileDb.exists()) {
      assertTrue(false);
    }
    // open
    K2hdkcLibrary INSTANCE =
        (K2hdkcLibrary) Native.synchronizedLibrary(Native.load("k2hdkc", K2hdkcLibrary.class));
    long handle =
        INSTANCE.k2hdkc_open_chmpx_full(
            SLAVE_CLUSTER_CONFIG,
            Cluster.DEFAULT_PORT,
            Cluster.DEFAULT_CUK,
            Cluster.DEFAULT_REJOIN,
            Cluster.DEFAULT_RETRY_REJOIN_FOREVER,
            Cluster.DEFAULT_CLEANUP);

    // remove
    Stream<String> stream = Stream.of(TEST_KEY_ARRAY);
    stream.forEach(
        s -> {
          boolean isSuccess = INSTANCE.k2hdkc_pm_remove_str_all(handle, s);
          if (!isSuccess) {
            logger.error("INSTANCE.k2hdkc_pm_remove_str_all returns false. key {}", s);
          }
        });

    // close
    boolean isSuccess = INSTANCE.k2hdkc_close_chmpx_ex(handle, true);
    if (!isSuccess) {
      logger.warn("INSTANCE.k2hdkc_close_chmpx_ex returns false");
    }
  }

  @AfterEach
  public void tearDown() {
    // if any
  }

  /** GetCmd */
  @Test
  public void testGetValueArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      SetCmd set = SetCmd.of("testGetValueArg1", "testGetValueArg1");
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());
      GetCmd get = GetCmd.of("testGetValueArg1");
      assertTrue(get != null);
      assertTrue(get.execute(sess) != null);
      assertTrue(get.execute(sess).isPresent());
      assertTrue(get.execute(sess).get().isSuccess());
      String str = (String) get.execute(sess).get().getValue();
      assertTrue(str.equals("testGetValueArg1"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** SetCmd */
  @Test
  public void testSetValueArg2() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      SetCmd set = SetCmd.of("testSetValueArg2", "testSetValueArg2");
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());
      GetCmd get = GetCmd.of("testSetValueArg2");
      assertTrue(get != null);
      assertTrue(get.execute(sess) != null);
      assertTrue(get.execute(sess).isPresent());
      assertTrue(get.execute(sess).get().isSuccess());
      String str = (String) get.execute(sess).get().getValue();
      assertTrue(str.equals("testSetValueArg2"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** SetSubkeysCmd */
  @Test
  public void testSetSubkeysArg2() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      String[] subkeys = {"testSetSubkeysArg2"};
      SetSubkeysCmd set = SetSubkeysCmd.of("testSetSubkeysArg2", subkeys);
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());

      GetSubkeysCmd get = GetSubkeysCmd.of("testSetSubkeysArg2");
      assertTrue(get != null);
      assertTrue(get.execute(sess) != null);
      assertTrue(get.execute(sess).isPresent());
      assertTrue(get.execute(sess).get().isSuccess());
      Optional<Result<List<String>>> list = get.execute(sess);
      Result<List<String>> res = list.get();
      assertTrue(res.getValue().get(0).equals("testSetSubkeysArg2"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** GetSubkeysCmd */
  @Test
  public void testGetSubkeysArg2() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      String[] subkeys = {"testGetSubkeysArg2"};
      SetSubkeysCmd set = SetSubkeysCmd.of("testGetSubkeysArg2", subkeys);
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());

      GetSubkeysCmd get = GetSubkeysCmd.of("testGetSubkeysArg2");
      assertTrue(get != null);
      assertTrue(get.execute(sess) != null);
      assertTrue(get.execute(sess).isPresent());
      assertTrue(get.execute(sess).get().isSuccess());
      Optional<Result<List<String>>> list = get.execute(sess);
      Result<List<String>> res = list.get();
      assertTrue(res.getValue().get(0).equals("testGetSubkeysArg2"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** ClearSubkeysCmd */
  @Test
  public void testClearSubkeysArg2() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      String[] subkeys = {"testClearSubkeysArg2"};
      SetSubkeysCmd set = SetSubkeysCmd.of("testClearSubkeysArg2", subkeys);
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());

      ClearSubkeysCmd clear = ClearSubkeysCmd.of("testClearSubkeysArg2");
      assertTrue(clear != null);
      assertTrue(clear.execute(sess) != null);
      assertTrue(clear.execute(sess).get().isSuccess());
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** AddSubkeyCmd */
  @Test
  public void testAddSubkeyArg2() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      // 1. setsubkeys
      String[] subkeys = {"subkey1", "subkey2"};
      SetSubkeysCmd set = SetSubkeysCmd.of("testAddSubkeyArg2", subkeys);
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());
      // 2. addsubkey
      String subkey = "subkey3";
      AddSubkeyCmd add = AddSubkeyCmd.of("testAddSubkeyArg2", subkey);
      assertTrue(add != null);
      assertTrue(add.execute(sess) != null);
      assertTrue(add.execute(sess).get().isSuccess());
      // 3. getsubkeys
      GetSubkeysCmd get = GetSubkeysCmd.of("testAddSubkeyArg2");
      assertTrue(get != null);
      assertTrue(get.execute(sess) != null);
      assertTrue(get.execute(sess).isPresent());
      assertTrue(get.execute(sess).get().isSuccess());
      Optional<Result<List<String>>> list = get.execute(sess);
      Result<List<String>> res = list.get();
      assertTrue(res.getValue().get(0).equals("subkey1"));
      assertTrue(res.getValue().get(1).equals("subkey2"));
      assertTrue(res.getValue().get(2).equals("subkey3"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** RemoveSubkeyCmd */
  @Test
  public void testRemoveSubkeyArg2() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      // 1. addsubkey
      String[] subkeys = {"subkey1", "subkey2"};
      SetAllCmd set = SetAllCmd.of("testRemoveSubkeyArg2", "val", subkeys);
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());

      // 2. getsubkeys
      GetSubkeysCmd getsub = GetSubkeysCmd.of("testRemoveSubkeyArg2");
      assertTrue(getsub != null);
      assertTrue(getsub.execute(sess) != null);
      assertTrue(getsub.execute(sess).isPresent());
      assertTrue(getsub.execute(sess).get().isSuccess());
      Optional<Result<List<String>>> list = getsub.execute(sess);
      Result<List<String>> res = list.get();
      assertTrue(res.getValue().get(0).equals("subkey1"));
      assertTrue(res.getValue().get(1).equals("subkey2"));

      // 3. removesubkey
      RemoveSubkeyCmd rmsub = RemoveSubkeyCmd.of("testRemoveSubkeyArg2", "subkey1");
      assertTrue(rmsub != null);
      assertTrue(rmsub.execute(sess) != null);

      // 4. getsubkeys
      GetSubkeysCmd getsub2 = GetSubkeysCmd.of("testRemoveSubkeyArg2");
      assertTrue(getsub2 != null);
      assertTrue(getsub2.execute(sess) != null);
      assertTrue(getsub2.execute(sess).isPresent());
      assertTrue(getsub2.execute(sess).get().isSuccess());
      Optional<Result<List<String>>> list2 = getsub2.execute(sess);
      Result<List<String>> res2 = list.get();
      assertTrue(res2.getValue().get(0).equals("subkey1"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** GetAttrsCmd */
  @Test
  public void testGetAttrsArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      GetAttrsCmd get = GetAttrsCmd.of("testGetAttrsArg1");
      assertTrue(get != null);
      assertTrue(get.execute(sess) != null);
      assertTrue(get.execute(sess).isPresent() == false);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** RemoveCmd */
  @Test
  public void testRemoveCmdArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      SetCmd set = SetCmd.of("testRemoveCmdArg1", "testRemoveCmdArg1");
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());
      GetCmd get = GetCmd.of("testRemoveCmdArg1");
      assertTrue(get != null);
      assertTrue(get.execute(sess) != null);
      assertTrue(get.execute(sess).get().isSuccess());
      String str = (String) get.execute(sess).get().getValue();
      assertTrue(str.equals("testRemoveCmdArg1"));
      RemoveCmd rm = RemoveCmd.of("testRemoveCmdArg1");
      assertTrue(rm != null);
      assertTrue(rm.execute(sess) != null);
      assertTrue(rm.execute(sess).get().isSuccess());
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** RenameCmd */
  @Test
  public void testRenameCmdArg2() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      SetCmd set = SetCmd.of("testRenameCmdArg2", "testRenameCmdArg2");
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());
      GetCmd get = GetCmd.of("testRenameCmdArg2");
      assertTrue(get != null);
      assertTrue(get.execute(sess) != null);
      assertTrue(get.execute(sess).get().isSuccess());
      String str = (String) get.execute(sess).get().getValue();
      assertTrue(str.equals("testRenameCmdArg2"));
      // 1. rename
      RenameCmd mv = RenameCmd.of("testRenameCmdArg2", "testRenameCmdArg2Renamed");
      assertTrue(mv != null);
      assertTrue(mv.execute(sess) != null);
      assertTrue(mv.execute(sess).get().isSuccess());
      // 2. get
      GetCmd get2 = GetCmd.of("testRenameCmdArg2Renamed");
      assertTrue(get2 != null);
      assertTrue(get2.execute(sess) != null);
      assertTrue(get2.execute(sess).get().isSuccess());
      String str2 = (String) get2.execute(sess).get().getValue();
      assertTrue(str2.equals("testRenameCmdArg2"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** SetAllCmd */
  @Test
  public void testSetAllArg3() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      String[] subkeys = {"testSetAllArg3"};
      SetAllCmd set = SetAllCmd.of("testSetAllArg3", "testSetAllArg3", subkeys);
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());
      GetCmd get = GetCmd.of("testSetAllArg3");
      assertTrue(get != null);
      assertTrue(get.execute(sess) != null);
      assertTrue(get.execute(sess).get().isSuccess());
      String str = (String) get.execute(sess).get().getValue();
      assertTrue(str.equals("testSetAllArg3"));
      GetSubkeysCmd getsubkeys = GetSubkeysCmd.of("testSetAllArg3");
      assertTrue(getsubkeys != null);
      assertTrue(getsubkeys.execute(sess) != null);
      assertTrue(getsubkeys.execute(sess).isPresent());
      assertTrue(getsubkeys.execute(sess).get().isSuccess());
      Optional<Result<List<String>>> list = getsubkeys.execute(sess);
      Result<List<String>> res = list.get();
      assertTrue(res.getValue().get(0).equals("testSetAllArg3"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** CasInitCmd */
  @Test
  public void testCasInitCmdArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      CasInitCmd init = CasInitCmd.of("testCasInitCmdArg1", 0);
      assertTrue(init != null);
      assertTrue(init.execute(sess) != null);
      assertTrue(init.execute(sess).get().isSuccess());
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** CasSetCmd */
  @Test
  public void testCasSetCmdArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      CasInitCmd init = CasInitCmd.of("testCasSetCmdArg1", 0);
      assertTrue(init != null);
      assertTrue(init.execute(sess) != null);
      assertTrue(init.execute(sess).get().isSuccess());
      CasSetCmd set = CasSetCmd.of("testCasSetCmdArg1", 0, 1);
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  public int getValusAsInt(byte[] value) {
    if (value.length != 4) {
      logger.error("this.value.length != 4 {}, shoule be == 4", value.length);
      throw new IllegalStateException(
          "this.value seems not to be an integer value " + value.length);
    }
    int rval =
        value[3] << 24 | (value[2] & 0xFF) << 16 | (value[1] & 0xFF) << 8 | (value[0] & 0xFF);
    // int rval = ByteBuffer.wrap(this.value).getInt();
    logger.debug("rval {}", rval);
    if (rval < 0) {
      logger.error("rval < 0 {}, shoule be >= 0", value.toString());
      throw new IllegalStateException("rval < 0, should be >= 0");
    }
    return rval;
  }

  /** CasGetCmd */
  @Test
  public void testCasGetCmdArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      CasInitCmd init = CasInitCmd.of("testCasGetCmdArg1", 0);
      assertTrue(init != null);
      assertTrue(init.execute(sess) != null);
      assertTrue(init.execute(sess).get().isSuccess());
      CasSetCmd set = CasSetCmd.of("testCasGetCmdArg1", 0, 1);
      assertTrue(set != null);
      assertTrue(set.execute(sess) != null);
      assertTrue(set.execute(sess).get().isSuccess());
      CasGetCmd get = CasGetCmd.of("testCasGetCmdArg1", CasGetCmd.DataType.INT);
      assertTrue(get != null);
      assertTrue(get.execute(sess) != null);
      assertTrue(get.execute(sess).get().isSuccess());
      ByteArrayOutputStream bos = (ByteArrayOutputStream) get.execute(sess).get().getValue();
      assertTrue(getValusAsInt(bos.toByteArray()) == 1);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** CasIncDecCmd */
  @Test
  public void testCasIncDecCmdArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      // 1. init
      CasInitCmd init = CasInitCmd.of("testCasIncDecCmdArg1", 0);
      assertTrue(init != null);
      assertTrue(init.execute(sess) != null);
      assertTrue(init.execute(sess).get().isSuccess());
      {
        CasGetCmd get = CasGetCmd.of("testCasIncDecCmdArg1", Cmd.DataType.INT);
        if (get.execute(sess).isPresent()) {
          ByteArrayOutputStream bos = (ByteArrayOutputStream) get.execute(sess).get().getValue();
          assertTrue(getValusAsInt(bos.toByteArray()) == 0);
        }
      }

      // 2. increment
      CasIncDecCmd inc = CasIncDecCmd.of("testCasIncDecCmdArg1");
      assertTrue(inc != null);
      Optional<Result<Boolean>> r = inc.execute(sess);
      if (r.isPresent()) {
        Boolean rb = r.get().getValue();
        assertTrue(rb);
      }
      {
        CasGetCmd get = CasGetCmd.of("testCasIncDecCmdArg1", Cmd.DataType.INT);
        if (get.execute(sess).isPresent()) {
          ByteArrayOutputStream bos = (ByteArrayOutputStream) get.execute(sess).get().getValue();
          assertTrue(getValusAsInt(bos.toByteArray()) == 1);
        }
      }
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** QueueAddCmd */
  @Test
  public void testQueueAddCmdArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      // 1. add an element to this queue
      QueueAddCmd qadd = QueueAddCmd.of("testQueueAddCmdArg1", "q1");
      assertTrue(qadd.execute(sess).get().isSuccess());
      // 2. remove an element from this queue
      QueueRemoveCmd qrm = QueueRemoveCmd.of("testQueueAddCmdArg1");
      Optional<Result<List<String>>> list = qrm.execute(sess);
      Result<List<String>> res = list.get();
      assertTrue(res.getValue().get(0).equals("q1"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** QueueRemoveCmd */
  @Test
  public void testQueueRemoveCmdArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      // 1. add an element to this queue
      QueueAddCmd qadd = QueueAddCmd.of("testQueueRemoveCmdArg1", "q1");
      assertTrue(qadd.execute(sess).get().isSuccess());
      // 2. remove an element from this queue
      QueueRemoveCmd qrm = QueueRemoveCmd.of("testQueueRemoveCmdArg1");
      Optional<Result<List<String>>> list = qrm.execute(sess);
      Result<List<String>> res = list.get();
      assertTrue(res.getValue().get(0).equals("q1"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** KeyQueueAddCmd */
  @Test
  public void testKeyQueueAddCmdArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      // 1. add an element to this queue
      KeyQueueAddCmd qadd = KeyQueueAddCmd.of("testQueueAddCmdArg1", "key1", "val1");
      assertTrue(qadd.execute(sess).get().isSuccess());
      // 2. remove an element from this queue
      KeyQueueRemoveCmd qrm = KeyQueueRemoveCmd.of("testQueueAddCmdArg1");
      Optional<Result<Map<String, String>>> map = qrm.execute(sess);
      Result<Map<String, String>> res = map.get();
      assertTrue(res.getValue().containsKey("key1"));
      assertTrue(res.getValue().containsValue("val1"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** KeyQueueRemoveCmd */
  @Test
  public void testKeyQueueRemoveCmdArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      // 1. add an element to this queue
      KeyQueueAddCmd qadd = KeyQueueAddCmd.of("testQueueRemoveCmdArg1", "key1", "val1");
      assertTrue(qadd.execute(sess).get().isSuccess());
      // 2. remove an element from this queue
      KeyQueueRemoveCmd qrm = KeyQueueRemoveCmd.of("testQueueRemoveCmdArg1");
      Optional<Result<Map<String, String>>> map = qrm.execute(sess);
      Result<Map<String, String>> res = map.get();
      assertTrue(res.getValue().containsKey("key1"));
      assertTrue(res.getValue().containsValue("val1"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }
}
