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

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Unit test for simple App. */
public class ClusterTest {
  private static final Logger logger = LoggerFactory.getLogger(ClusterTest.class);
  private static final String SLAVE_CLUSTER_CONFIG = "../cluster/slave.yaml";

  @BeforeEach
  public void setUp() {
    File fileDb = new File(SLAVE_CLUSTER_CONFIG);
    if (!fileDb.exists()) {
      fail();
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
      System.out.println(cluster); // calls blar blar method to suppress "never referenced" warnings
      assertTrue(true);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      fail();
    }
  }

  /** K2hdkc Constructor */
  @Test
  public void testOfArg2() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG, Cluster.DEFAULT_PORT)) {
      System.out.println(cluster); // calls blar blar method to suppress "never referenced" warnings
      assertTrue(true);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      fail();
    }
  }

  /** K2hdkc Constructor */
  @Test
  public void testOfArg3() {
    try (Cluster cluster =
        Cluster.of(SLAVE_CLUSTER_CONFIG, Cluster.DEFAULT_PORT, Cluster.DEFAULT_CUK)) {
      System.out.println(cluster); // calls blar blar method to suppress "never referenced" warnings
      assertTrue(true);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      fail();
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
      System.out.println(cluster); // calls blar blar method to suppress "never referenced" warnings
      assertTrue(true);
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      fail();
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
      fail();
    }
  }

  /** K2hdkc Cluster enableNativeLogArg3 */
  @Test
  public void testOfEnableNativeLog3() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG);
        Session sess = Session.of(cluster)) {
      System.out.println(sess); // calls blar blar method to suppress "never referenced" warnings
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
        System.out.println("nativelog must be created");
        fail();
      }
    } catch (IOException e) {
      logger.error("message {}", e.getMessage());
      // assertFalse(ex.getMessage(), true);
    }
  }

  /** K2hdkc Cluster setValue */
  @Test
  public void testOfSetValue() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      assertTrue(cluster.remove("testOfSetValue"));
      assertTrue(cluster.set("testOfSetValue", "testOfSetValue"));
      assertEquals("testOfSetValue", cluster.get("testOfSetValue").get());
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      fail();
    }
  }

  /** K2hdkc Cluster setValue */
  @Test
  public void testOfGetValue() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      assertTrue(cluster.remove("testOfGetValue"));
      assertTrue(cluster.set("testOfGetValue", "testOfGetValue"));
      assertEquals("testOfGetValue", cluster.get("testOfGetValue").get());
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      fail();
    }
  }

  /** K2hdkc Cluster setSubkeys */
  @Test
  public void testOfSetSubkeys() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      assertTrue(cluster.clearSubkeys("testOfSetSubkeys"));
      String[] subkeys = {"a", "b"};
      assertTrue(cluster.setSubkeys("testOfSetSubkeys", subkeys));
      assertEquals(2, cluster.getSubkeys("testOfSetSubkeys").size());
      assertEquals("a", cluster.getSubkeys("testOfSetSubkeys").get(0));
      assertEquals("b", cluster.getSubkeys("testOfSetSubkeys").get(1));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      fail();
    }
  }

  /** K2hdkc Cluster getSubkeys */
  @Test
  public void testOfGetSubkeys() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      assertTrue(cluster.clearSubkeys("testOfGetSubkeys"));
      String[] subkeys = {"a", "b"};
      assertTrue(cluster.setSubkeys("testOfGetSubkeys", subkeys));
      assertEquals("a", cluster.getSubkeys("testOfGetSubkeys").get(0));
      assertEquals("b", cluster.getSubkeys("testOfGetSubkeys").get(1));
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      fail();
    }
  }

  /** K2hdkc Cluster clearSubkeys */
  @Test
  public void testOfClearSubkeys() {
    try (Cluster cluster = Cluster.of(SLAVE_CLUSTER_CONFIG)) {
      String[] subkeys = {"a", "b"};
      assertTrue(cluster.setSubkeys("testOfClearSubkeys", subkeys));
      assertEquals(2, cluster.getSubkeys("testOfClearSubkeys").size());
      assertTrue(cluster.clearSubkeys("testOfClearSubkeys"));
      assertEquals(0, cluster.getSubkeys("testOfClearSubkeys").size());
    } catch (IOException e) {
      System.out.println("IOException " + e.getMessage());
      fail();
    }
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
