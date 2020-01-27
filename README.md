k2hdkc_java
------
![](https://github.com/yahoojapan/k2hdkc_java/workflows/.github/workflows/build/badge.svg)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/yahoojapan/k2hdkc_nodejs/master/LICENSE)

### Overview

k2hdkc_java is an official java driver for [k2hdkc](https://k2hdkc.antpick.ax/), which is a highly available and scalable distributed KVS clustering system.

### Install

Add the following repository and the dependency to your pom.xml or settings.xml.
```
  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>false</enabled></snapshots>
        </repository>
        <repository>
          <id>github</id>
          <name>GitHub yahoojapan Apache Maven Packages</name>
          <url>https://maven.pkg.github.com/yahoojapan/k2hdkc_java</url>
        </repository>
      </repositories>
    </profile>
  </profiles>

```
```
        <dependency>
          <groupId>ax.antpick</groupId>
          <artifactId>k2hdkc</artifactId>
        </dependency> 
```

### Development

Clone this repository and go into the directory, then run the following command.
```
$ mvn clean exec:exec package
```

### Documents
  - [Document top page](https://java.k2hdkc.antpick.ax/)
  - [About K2HDKC](https://k2hdkc.antpick.ax/)
  - [About AntPickax](https://antpick.ax/)

### Packages

  - [GitHub Packages](https://github.com/yahoojapan/k2hdkc_java/packages)

### License

MIT License. See the LICENSE file.

## AntPickax

[AntPickax](https://antpick.ax/) is 
  - an open source team in [Yahoo Japan Corporation](https://about.yahoo.co.jp/info/en/company/). 
  - a product family of open source software developed by [AntPickax](https://antpick.ax/).


