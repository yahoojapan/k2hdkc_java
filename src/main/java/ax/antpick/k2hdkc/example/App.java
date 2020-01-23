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
 */
package ax.antpick.k2hdkc.example;

import ax.antpick.k2hdkc.*;
import com.sun.jna.*;
import com.sun.jna.ptr.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an example to add a key of "keystring" with a value of "valuestring" to a <a
 * href="https://k2hdkc.antpick.ax/">k2hdkc</a> cluster.
 *
 * <p>The Cluster class has three main roles.
 *
 * <ol>
 *   <li>Cluster holds settings to connect a <a href="https://chmpx.antpick.ax/">chmpx</a> slave
 *       server.
 *   <li>Cluster is responsible to control native libraries log levels.
 *   <li>Cluster encapsulates the K2hdkcLibrary C API and Cmd classes.
 * </ol>
 *
 * <p>Before running the code, You should run three processes.
 *
 * <ol>
 *   <li>A <a href="https://chmpx.antpick.ax/">chmpx</a> server process
 *   <li>A <a href="https://chmpx.antpick.ax/">chmpx</a> slave process
 *   <li>A <a href="https://k2hdkc.antpick.ax/">k2hdkc</a> server process
 * </ol>
 *
 * <p>The following commands in this repository will run all processes you need in localhost.
 *
 * <pre>{@code
 * $ cd cluster
 * $ sh start_server.sh
 * }</pre>
 *
 * @author Hirotaka Wakabayashi
 */
public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    try (Cluster c = Cluster.of("cluster/slave.yaml");
        Session s = Session.of(c)) {
      SetCmd set = SetCmd.of("key", "value");
      assert ((Boolean) set.execute(s).get().getValue() == true);
      RenameCmd rm = RenameCmd.of("key", "newkey");
      assert ((Boolean) rm.execute(s).get().getValue() == true);
      GetCmd get = GetCmd.of("newkey");
      String str = (String) get.execute(s).get().getValue();
      System.out.println(str);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
