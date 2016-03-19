Codec
=====

**Graphical user interface available for Windows, Linux & OS X.**

```ruby
Usage: codec <mode> <algorithm> [-c <charset>] { <text to process> | -f <source file> <destination file> }

Mode:
    e    Encode
    d    Decode

Algorithms: Base16, Base32, Base64, Ascii85, basE91, MD2, MD4, MD5, SHA-1, SHA-256, SHA-384, SHA-512

Example: codec e base64 -c UTF-8 "Lorem ipsum dolor sit amet"
```
