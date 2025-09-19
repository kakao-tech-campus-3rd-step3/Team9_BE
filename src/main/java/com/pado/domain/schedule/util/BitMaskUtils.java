package com.pado.domain.schedule.util;

public final class BitMaskUtils {

    private BitMaskUtils() {
    }

    public static int bitIndexFromCandidateNumber(long candidateNumber) {
        if (candidateNumber <= 0L || (candidateNumber & (candidateNumber - 1L)) != 0L) {
            throw new IllegalArgumentException("candidate_number must be power of two");
        }
        return Long.numberOfTrailingZeros(candidateNumber);
    }

    public static void setBit(byte[] bytes, int bitIndex, boolean value) {
        int byteIndex = bitIndex / 8;
        int bitInByte = bitIndex % 8;
        ensureCapacity(bytes, byteIndex);
        int mask = 1 << bitInByte;
        int cur = Byte.toUnsignedInt(bytes[byteIndex]);
        if (value) {
            cur |= mask;
        } else {
            cur &= ~mask;
        }
        bytes[byteIndex] = (byte) cur;
    }

    public static long toUnsignedLong(byte[] bytes) {
        int len = Math.min(bytes.length, 8);
        long v = 0L;
        for (int i = 0; i < len; i++) {
            long b = Byte.toUnsignedLong(bytes[i]);
            v |= (b << (i * 8));
        }
        return v;
    }

    public static int popcount(byte[] bytes) {
        int c = 0;
        for (byte b : bytes) {
            c += Integer.bitCount(Byte.toUnsignedInt(b));
        }
        return c;
    }

    private static void ensureCapacity(byte[] bytes, int byteIndex) {
        if (byteIndex >= bytes.length) {
            throw new IllegalArgumentException("occupancy_bits buffer too small");
        }
    }
}
