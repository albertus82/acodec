package it.albertus.codec;

import sun.misc.BASE64Encoder;

public class Base64Encoder {

	public static void main(String[] args) {
		if (args.length == 1) {
			System.out.println("Input:  \"" + args[0] + "\"");
			System.out.println("Output: \"" + new BASE64Encoder().encode(args[0].getBytes()).replaceAll("[\\r\\n]", "") + "\"");
		}
		else {
			System.out.println("Usage: Base64Encoder \"Text to encode\"");
		}
	}
}
