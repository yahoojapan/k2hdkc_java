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
/**
 * Provides classes that are fundamental to the applications to communicate with a <a
 * href="https://k2hdkc.antpick.ax/">k2hdk</a> clustering system.
 * <p>
 * <h2>Primary Packages</h2>
 *
 * <p>Contains 3 primary classes.
 *
 * <ol>
 *   <li>The class {@link ax.antpick.k2hdkc.Cluster} holds configurations to connect a <a
 *       href="https://chmpx.antpick.ax/">chmpx</a> slave process and it provides simple methods to
 *       set a value to a <a href="https://k2hdkc.antpick.ax/">k2hdk</a> and get a value from it.
 *       You can extends the {@link ax.antpick.k2hdkc.Cluster} class if you want to add more methods.
 *   <li>The class {@link ax.antpick.k2hdkc.Session} provides a connection with a <a
 *       href="https://chmpx.antpick.ax/">chmpx</a> slave process which communicate with a <a
 *       href="https://k2hdkc.antpick.ax/">k2hdk</a> clustering system.
 *   <li>Subclasses of {@link ax.antpick.k2hdkc.CmdBase} provide an operation against a <a
 *       href="https://k2hdkc.antpick.ax/">k2hdk</a> clustering system and holds the result of the
 *       operation in {@link ax.antpick.k2hdkc.Result} object.
 * </ol>
 *
 * <h3>Usage Examples</h3>
 *
 * <p>Please see each class document for the example.
 * <p>
 * <h2>Using Native C Libraries by using JNA </h2>
 *
 * <p>The class {@link ax.antpick.k2hdkc.K2hdkcLibrary}, {@link ax.antpick.k2hdkc.ChmpxLibrary} and {@link ax.antpick.k2hdkc.K2hashLibrary} implements <a
 * href="https://github.com/java-native-access/jna">Java Native Access</a>'s {@code Library}
 * interface. Their visibilities are mostly package private because we think it is hard to control
 * for application programmers. So they are not extensible currently.
 * <p>
 * <h2>Logging</h2>
 *
 * <p>The <a href="https://logback.qos.ch/">Logback</a> is a successor to the popular log4j project.
 * Suppose you want to write logs to stdout in the "debug" level, you could write a logback.xml as:
 *
 * <pre>{@code
 * <configuration>
 *
 *  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
 *    <!-- encoders are assigned the type
 *         ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
 *    <encoder>
 *      <!-- <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern> -->
 *      <pattern>%-5level [%thread] %date{ISO8601} %F:%L - %msg%n</pattern>
 *    </encoder>
 *  </appender>
 *
 *  <root level="debug">
 *    <appender-ref ref="STDOUT" />
 *  </root>
 * }</pre>
 *
 * Please see the <a href="https://logback.qos.ch/manual/index.html">logback manual</a> more
 * details.
 *
 * <p>In addtion, we use native C API libraries. We sometimes want to see how they work. The class
 * {@link ax.antpick.k2hdkc.Cluster} provides methods to change log levels. The following code works as:
 *
 * <ol>
 *   <li>Logs are written to /tmp/native.log
 *   <li>Change the k2hdkc native library's loglevel to the info level.
 * </ol>
 *
 * <pre>{@code
 *   try (Cluster cluster = Cluster.of("slave.yaml");
 *       Session sess = Session.of(cluster); ) {
 *     cluster.setNativeLogLevel(
 *         "/tmp/native.log",
 *         Cluster.NativeStackLogLevel.StackK2hdkc,
 *         Cluster.NativeLogLevel.SeverityInfo);
 *     // ...
 *   } catch (IOException ex) {
 *     logger.error("message {}", ex.getMessage());
 *   }
 * }
 * }</pre>
 *
 * @author Hirotaka Wakabayashi
 */
package ax.antpick.k2hdkc;

//
// Local variables:
// tab-width: 2
// c-basic-offset: 2
// indent-tabs-mode: nil
// End:
// vim600: noexpandtab sw=2 ts=2 fdm=marker
// vim<600: noexpandtab sw=2 ts=2
//
