Codec
=====

[![Latest release](https://img.shields.io/github/release/albertus82/codec.svg)](https://github.com/albertus82/codec/releases/latest)
[![Build status](https://github.com/albertus82/codec/workflows/build/badge.svg)](https://github.com/albertus82/codec/actions)
[![Build status](https://ci.appveyor.com/api/projects/status/github/albertus82/codec?branch=master&svg=true)](https://ci.appveyor.com/project/albertus82/codec)
[![Build status](https://dev.azure.com/albertus82/codec/_apis/build/status/master)](https://dev.azure.com/albertus82/codec/_build/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=it.albertus%3Acodec&metric=alert_status)](https://sonarcloud.io/dashboard?id=it.albertus%3Acodec)

**Graphical user interface available for Windows, Linux & macOS.**

![Screenshot](https://user-images.githubusercontent.com/8672431/27011383-cbf09962-4eba-11e7-9c7e-24215c5e06e4.png)

## Download

Download the [latest release](https://github.com/albertus82/codec/releases/latest) from the [releases page](https://github.com/albertus82/codec/releases).

## Installation

* **Windows**: if you downloaded a ZIP package, simply unpack the archive; otherwise run the installer (EXE) to install the application.

  If Windows complains with a ***Windows protected your PC*** popup, you may need to click ***Run anyway*** to proceed with the installation.

  ![Windows protected your PC](https://user-images.githubusercontent.com/8672431/31048995-7145b034-a62a-11e7-860b-c477237145ce.png)

  In order to enable the *Run anyway* button, you may need to open the *Properties* of the installer, tab *General*, section *Security* (if available), and tick the ***Unblock*** option.
* **Linux** & **macOS**: unpack the archive.

**This application requires [Java SE Runtime Environment (JRE)](https://www.java.com) v1.8 (or newer) to run.**

## Command line reference

```ruby
Usage: codec <mode> <algorithm> [-c <charset>] { <text to process> | -f <source file> <destination file> }

Mode:
    e    Encode
    d    Decode

Algorithms: Base16, Base32, Base64, Ascii85, basE91, CRC-16, CRC-32, Adler-32,
            MD2, MD4, MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512,
            SHA-512/224, SHA-512/256, SHA3-224, SHA3-256, SHA3-384, SHA3-512,
            RIPEMD-128, RIPEMD-160, RIPEMD-256, RIPEMD-320, Tiger, Whirlpool

Example: codec e base64 -c UTF-8 "Lorem ipsum dolor sit amet"
```

## Acknowledgements

This application includes software developed by the **Eclipse Foundation** that is distributed under the [Eclipse Public License](https://eclipse.org/org/documents/epl-v10.php).
