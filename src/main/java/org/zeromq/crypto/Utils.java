//package org.zeromq.crypto;
//
//import java.security.NoSuchAlgorithmException;
//import java.security.NoSuchProviderException;
//import java.security.SecureRandom;
//
//public class Utils
//{
//    private Utils()
//    {
//    }
//
//    public static byte[] random32Bytes() throws NoSuchAlgorithmException, NoSuchProviderException
//    {
//        return randomNBytes(32);
//    }
//
//    public static byte[] randomNBytes(int n) throws NoSuchAlgorithmException, NoSuchProviderException
//    {
//        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
//        sr.nextBytes(new byte[1]); // Ensure it's properly seeded
//        byte[] bytes = new byte[n];
//        sr.nextBytes(bytes);
//        return bytes;
//    }
//
//}
