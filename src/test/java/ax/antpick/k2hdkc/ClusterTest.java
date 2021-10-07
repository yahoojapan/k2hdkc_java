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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Unit test for simple App. */
public class ClusterTest {
  private static final Logger logger = LoggerFactory.getLogger(ClusterTest.class);
  private static final String SLAVE_CLUSTER_CONFIG = "cluster/slave.yaml";
  private static final String[] TEST_KEY_ARRAY = {"testGetValueArg1", "testSetValueArg2"};

  @BeforeEach
  public void setUp() {
    File fileDb = new File(SLAVE_CLUSTER_CONFIG);
    if (!fileDb.exists()) {
      assertTrue(false);
    }
  }

  @AfterEach
  public void tearDown() {
    // if any
  }

  /** K2hdkc Constructor */
  @Test
  public void testOfArg1() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      cluster.toString(); // calls blar blar method to suppress "never referenced" warnings
      assertTrue(true);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** K2hdkc Constructor */
  @Test
  public void testOfArg2() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG, Cluster.DEFAULT_PORT)) {
      cluster.toString(); // calls blar blar method to suppress "never referenced" warnings
      assertTrue(true);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** K2hdkc Constructor */
  @Test
  public void testOfArg3() {
    try (Cluster cluster =
        Cluster.of(SLAVE_CLUSTER_CONFIG, Cluster.DEFAULT_PORT, Cluster.DEFAULT_CUK)) {
      cluster.toString(); // calls blar blar method to suppress "never referenced" warnings
      assertTrue(true);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** K2hdkc Constructor */
  @Test
  public void testOfArg5() {
    try (Cluster cluster =
        Cluster.of(
            SLAVE_CLUSTER_CONFIG,
            Cluster.DEFAULT_PORT,
            Cluster.DEFAULT_CUK,
            Cluster.DEFAULT_REJOIN,
            Cluster.DEFAULT_RETRY_REJOIN_FOREVER,
            Cluster.DEFAULT_CLEANUP)) {
      cluster.toString(); // calls blar blar method to suppress "never referenced" warnings
      assertTrue(true);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** K2hdkc Cluster initNativeLog */
  @Test
  public void testOfInitNativeLog() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      cluster.initNativeLog();
      assertTrue(true);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** K2hdkc Cluster enableNativeLogArg3 */
  @Test
  public void testOfEnableNativeLog3() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster); ) {
      sess.toString(); // calls blar blar method to suppress "never referenced" warnings
      File nativeLog = new File("/tmp/native.log");
      if (nativeLog.exists()) {
        nativeLog.delete();
      }
      cluster.setNativeLogLevel(
          "/tmp/native.log",
          Cluster.NativeStackLogLevel.StackK2hdkc,
          Cluster.NativeLogLevel.SeverityInfo);
      Path path = Paths.get("/tmp/native.log");
      if (!path.toFile().exists()) {
	logger.error("nativelog must be created");
        assertTrue(false);
      }
    } catch (IOException e) {
      logger.error("message {}", e.getMessage());
      assertTrue(true);
    }
  }

  /** K2hdkc Cluster setValue */
  @Test
  public void testOfSetValue() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      assertTrue(cluster.remove("testOfSetValue"));
      assertTrue(cluster.set("testOfSetValue", "testOfSetValue"));
      assertTrue(cluster.get("testOfSetValue").get().equals("testOfSetValue"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** K2hdkc Cluster setValue */
  @Test
  public void testOfGetValue() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      /*
           cluster.enableNativeLog(
               "/tmp/native.log",
               Cluster.NativeStackLogLevel.StackK2hdkc,
               Cluster.NativeLogLevel.SeverityDump);
      */
      assertTrue(cluster.remove("testOfGetValue"));
      assertTrue(cluster.set("testOfGetValue", "testOfGetValue"));
      assertTrue(cluster.get("testOfGetValue").get().equals("testOfGetValue"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** K2hdkc Cluster setSubkeys */
  @Test
  public void testOfSetSubkeys() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      assertTrue(cluster.clearSubkeys("testOfSetSubkeys"));
      String[] subkeys = {"a", "b"};
      assertTrue(cluster.setSubkeys("testOfSetSubkeys", subkeys));
      assertTrue(cluster.getSubkeys("testOfSetSubkeys").size() == 2);
      assertTrue(cluster.getSubkeys("testOfSetSubkeys").get(0).equals("a"));
      assertTrue(cluster.getSubkeys("testOfSetSubkeys").get(1).equals("b"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** K2hdkc Cluster getSubkeys */
  @Test
  public void testOfGetSubkeys() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      assertTrue(cluster.clearSubkeys("testOfGetSubkeys"));
      String[] subkeys = {"a", "b"};
      assertTrue(cluster.setSubkeys("testOfGetSubkeys", subkeys));
      assertTrue(cluster.getSubkeys("testOfGetSubkeys").get(0).equals("a"));
      assertTrue(cluster.getSubkeys("testOfGetSubkeys").get(1).equals("b"));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }

  /** K2hdkc Cluster clearSubkeys */
  @Test
  public void testOfClearSubkeys() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      String[] subkeys = {"a", "b"};
      assertTrue(cluster.setSubkeys("testOfClearSubkeys", subkeys));
      assertTrue(cluster.getSubkeys("testOfClearSubkeys").size() == 2);
      assertTrue(cluster.clearSubkeys("testOfClearSubkeys"));
      assertTrue(cluster.getSubkeys("testOfClearSubkeys").size() == 0);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      assertFalse(true);
    }
  }
}
