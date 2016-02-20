package it.albertus.codec;

import org.apache.commons.codec.binary.Base64;

public class Base64Encoder {

	public static void main(String[] args) {
		if (args.length == 1) {
			System.out.println("Input:  \"" + args[0] + "\"");
			System.out.println("Output: \"" + Base64.encodeBase64String(args[0].getBytes()).replaceAll("[\\r\\n]", "") + "\"");
		}
		else {
			System.out.println("Usage: Base64Encoder \"Text to encode\"");
		}
	}
}
