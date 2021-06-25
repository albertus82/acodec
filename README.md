ACodec
======

[![Latest release](https://img.shields.io/github/release/albertus82/acodec.svg)](https://github.com/albertus82/acodec/releases/latest)
[![Build status](https://github.com/albertus82/acodec/workflows/build/badge.svg)](https://github.com/albertus82/acodec/actions)
[![Build status](https://ci.appveyor.com/api/projects/status/github/albertus82/acodec?branch=master&svg=true)](https://ci.appveyor.com/project/albertus82/acodec)
[![Known Vulnerabilities](https://snyk.io/test/github/albertus82/acodec/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/albertus82/acodec?targetFile=pom.xml)

**Graphical user interface available for Windows, Linux & macOS.**

![Screenshot](https://user-images.githubusercontent.com/8672431/109070613-ffc20280-76f2-11eb-92cb-8f7e1a55e681.png)

## Download

Download the [latest release](https://github.com/albertus82/acodec/releases/latest) from the [releases page](https://github.com/albertus82/acodec/releases).

## Installation

* **Windows**: if you downloaded a ZIP package, simply unpack the archive; otherwise run the installer (EXE) to install the application.

  If Windows complains with a ***Windows protected your PC*** popup, you may need to click ***Run anyway*** to proceed with the installation.

  ![Windows protected your PC](https://user-images.githubusercontent.com/8672431/31048995-7145b034-a62a-11e7-860b-c477237145ce.png)

  In order to enable the *Run anyway* button, you may need to open the *Properties* of the installer, tab *General*, section *Security* (if available), and tick the ***Unblock*** option.
  > This workaround is required because the installer executables are not *signed*, and there are no free certificates I can use to sign them.
* **Linux** & **macOS**: unpack the archive.

**This application requires [Java SE Runtime Environment (JRE)](https://www.java.com) v1.8 (or newer) to run.**

## Command line reference

```ruby
Usage: acodec <mode> <algorithm> [-c <charset>] { <text to process> | -f <source file> [<destination file>] | -i (interactive input) }

Mode:
    e    Encode
    d    Decode

Algorithms: Base16, Base32, base32hex, Base45, Base64, base64url, Ascii85,
            basE91, CRC-16, CRC-32, CRC-32C, Adler-32, MD2, MD4, MD5, SHA-1,
            SHA-224, SHA-256, SHA-384, SHA-512, SHA-512/224, SHA-512/256,
            SHA3-224, SHA3-256, SHA3-384, SHA3-512, RIPEMD-128, RIPEMD-160,
            RIPEMD-256, RIPEMD-320, Tiger, Whirlpool

Example: acodec e base64 -c UTF-8 "Lorem ipsum dolor sit amet"
```

## Acknowledgements

Icon designed by [Aha-Soft](https://www.aha-soft.com).

This application uses or includes portions of the following third party software:

|Component                   |Author                     |License                                                |Home page                                     |
|----------------------------|---------------------------|-------------------------------------------------------|----------------------------------------------|
|Apache Commons & OpenJPA    |Apache Software Foundation |[License](https://www.apache.org/licenses/LICENSE-2.0) |[Home page](https://www.apache.org)           |
|Base45                      |Staat der Nederlanden      |[License](https://opensource.org/licenses/EUPL-1.2)    |[Home page](https://git.io/JnFST)             |
|basE91                      |Joachim Henke              |[License](http://base91.sourceforge.net/license.txt)   |[Home page](http://base91.sourceforge.net)    |
|Bouncy Castle               |Legion of the Bouncy Castle|[License](https://www.bouncycastle.org/license.html)   |[Home page](https://www.bouncycastle.org)     |
|Eclipse Platform & SWT      |Eclipse Foundation         |[License](https://www.eclipse.org/legal/epl-2.0/)      |[Home page](https://www.eclipse.org)          |
|FreeHEP                     |Mark Donszelmann           |[License](https://java.freehep.org/license.html)       |[Home page](https://java.freehep.org)         |
|Inno Setup                  |Jordan Russell             |[License](https://jrsoftware.org/files/is/license.txt) |[Home page](https://jrsoftware.org/isinfo.php)|
|Launch4j                    |Grzegorz Kowal             |[License](https://opensource.org/licenses/BSD-3-Clause)|[Home page](http://launch4j.sourceforge.net)  |
|Picocli                     |Remko Popma                |[License](https://git.io/JUqAY)                        |[Home page](https://picocli.info)             |
|Reflections                 |ronmamo                    |[License](https://git.io/Jtp8i)                        |[Home page](https://git.io/Jtp81)             |
|universalJavaApplicationStub|Tobias Fischer             |[License](https://git.io/JUqAq)                        |[Home page](https://git.io/JUqAF)             |
