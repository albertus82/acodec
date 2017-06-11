Codec
=====

[![Build Status](https://travis-ci.org/Albertus82/Codec.svg?branch=master)](https://travis-ci.org/Albertus82/Codec)
[![Build status](https://ci.appveyor.com/api/projects/status/github/Albertus82/Codec?branch=master&svg=true)](https://ci.appveyor.com/project/Albertus82/Codec)

**Graphical user interface available for Windows, Linux & OS X.**

![Screenshot](https://user-images.githubusercontent.com/8672431/27011383-cbf09962-4eba-11e7-9c7e-24215c5e06e4.png)

Command line reference:

```ruby
Usage: codec <mode> <algorithm> [-c <charset>] { <text to process> | -f <source file> <destination file> }

Mode:
    e    Encode
    d    Decode

Algorithms: Base16, Base32, Base64, Ascii85, basE91, MD2, MD4, MD5, SHA-1, SHA-256, SHA-384, SHA-512

Example: codec e base64 -c UTF-8 "Lorem ipsum dolor sit amet"
```
