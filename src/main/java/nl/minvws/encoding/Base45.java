/*
 * Copyright 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.minvws.encoding;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Base45 {

    private static final int BaseSize = 45;
    private static final int ChunkSize = 2;
    private static final int EncodedChunkSize = 3;
    private static final int SmallEncodedChunkSize = 2;
    private static final int ByteSize = 256;

    private Base45() {
    }

    /**
     * Returns a {@link Encoder} that encodes using the type Base45 encoding scheme.
     * @return A Base45 encoder.
     */
    public static Encoder getEncoder() {
        return Encoder.ENCODER;
    }

    /**
     * Returns a {@link Decoder} that decodes using the type Base45 encoding scheme.
     * @return A Base45 decoder.
     */
    public static Decoder getDecoder() {
        return Decoder.DECODER;
    }

    /**
     * This class implements an encoder for encoding byte data using the Base45 encoding scheme
     * Instances of {@link Encoder} class are safe for use by multiple concurrent threads.
     */
    public static class Encoder {

        /**
         * This array is a lookup table that translates integer
         * index values into their "Base45 Alphabet" equivalents as specified.
         */
        private static final byte[] toBase45 = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
            'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '$', '%',
            '*', '+', '-', '.', '/', ':'
        };

        static final Encoder ENCODER = new Encoder();

        /**
         * Encodes all bytes from the specified byte array into a newly-allocated
         * byte array using the {@link Base45} encoding scheme. The returned byte
         * array is of the length of the resulting bytes.
         *
         * @param src the byte array to encode
         * @return A newly-allocated byte array containing the resulting encoded bytes.
         */
        public byte[] encode(byte[] src) {
            int wholeChunkCount = src.length / ChunkSize;
            byte[] result = new byte[wholeChunkCount * EncodedChunkSize + (src.length % ChunkSize == 1 ? SmallEncodedChunkSize : 0)];

            int resultIndex = 0;
            int wholeChunkLength = wholeChunkCount * ChunkSize;
            for (int i = 0; i < wholeChunkLength;) {
                int value = (src[i++] & 0xff) * ByteSize + (src[i++] & 0xff);
                result[resultIndex++] = toBase45[value % BaseSize];
                result[resultIndex++] = toBase45[(value / BaseSize) % BaseSize];
                result[resultIndex++] = toBase45[(value / (BaseSize * BaseSize)) % BaseSize];
            }

            if (src.length % ChunkSize != 0) {
                result[result.length - 2] = toBase45[(src[src.length - 1] & 0xff) % BaseSize];
                result[result.length - 1] = (src[src.length - 1] & 0xff)
                        < BaseSize ? toBase45[0] : toBase45[(src[src.length - 1] & 0xff) / BaseSize % BaseSize];
            }
            return result;
        }

        /**
         * Encodes the specified byte array into a String using the {@link Base45} encoding scheme.
         *
         * An invocation of this method has exactly the same
         * effect as invoking {@code new String(encode(src), StandardCharsets.ISO_8859_1)}.
         * Even though it's deprecated it's added to be similar to Base64 in java.
         *
         * @param src the byte array to encode
         * @return A String containing the resulting Base45 encoded characters
         */
        @SuppressWarnings("deprecation")
        public String encodeToString(byte[] src) {
            byte[] encoded = encode(src);
            return new String(encoded, 0, 0, encoded.length);
        }

    }

    /**
     * This class implements a decoder for decoding byte data using the
     * Base45 encoding scheme
     *
     * <p> Instances of {@link Decoder} class are safe for use by
     * multiple concurrent threads.
     */
    public static class Decoder {

        private Decoder() {
        }

        /**
         * Lookup table for decoding unicode characters drawn from the
         * "Base45 Alphabet".
         */
        private static final int[] fromBase45 = new int[256];

        static {
            Arrays.fill(fromBase45, -1);
            for (int i = 0; i < Encoder.toBase45.length; i++) {
                fromBase45[Encoder.toBase45[i]] = i;
            }
        }

        static final Decoder DECODER = new Decoder();

        /**
         * Decodes all bytes from the input byte array using the {@link Base45}
         * encoding scheme, writing the results into a newly-allocated output
         * byte array. The returned byte array is of the length of the resulting
         * bytes.
         *
         * @param src the byte array to decode
         * @return A newly-allocated byte array containing the decoded bytes.
         * @throws IllegalArgumentException if {@code src} is not in valid Base45 scheme
         */
        public byte[] decode(byte[] src) {
            int remainderSize = src.length % EncodedChunkSize;

            int[] buffer = new int[src.length];
            for (int i = 0; i < src.length; ++i) {
                buffer[i] = fromBase45[Byte.toUnsignedInt(src[i])];
                if (buffer[i] == -1) {
                    throw new IllegalArgumentException();
                }
            }

            int wholeChunkCount = buffer.length / EncodedChunkSize;
            byte[] result = new byte[wholeChunkCount * ChunkSize + (remainderSize == ChunkSize ? 1 : 0)];
            int resultIndex = 0;
            int wholeChunkLength = wholeChunkCount * EncodedChunkSize;
            for (int i = 0;  i < wholeChunkLength; ) {
                int val = buffer[i++] + BaseSize * buffer[i++] + BaseSize * BaseSize * buffer[i++];
                if (val > 0xFFFF) {
                    throw new IllegalArgumentException();
                }
                result[resultIndex++] = (byte)(val / ByteSize);
                result[resultIndex++] = (byte)(val % ByteSize);
            }

            if (remainderSize != 0) {
                result[resultIndex] = (byte) (buffer[buffer.length - 2] + BaseSize * buffer[buffer.length - 1]);
            }
            return result;
        }

        public byte[] decode(String src) {
            return decode(src.getBytes(StandardCharsets.ISO_8859_1));
        }
    }
}
