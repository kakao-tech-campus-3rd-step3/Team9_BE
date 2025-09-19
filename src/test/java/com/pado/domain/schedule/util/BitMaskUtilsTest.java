package com.pado.domain.schedule.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitMaskUtilsTest {

    @Test
    @DisplayName("candidate_number가 2^k일 때 비트 인덱스를 올바르게 계산한다")
    void bitIndex() {
        assertEquals(0, BitMaskUtils.bitIndexFromCandidateNumber(1L));
        assertEquals(1, BitMaskUtils.bitIndexFromCandidateNumber(2L));
        assertEquals(2, BitMaskUtils.bitIndexFromCandidateNumber(4L));
    }

    @Test
    @DisplayName("바이트 배열에 비트를 SET/UNSET 하고 popcount가 일치한다")
    void setAndPopcount() {
        byte[] buf = new byte[2];
        BitMaskUtils.setBit(buf, 0, true);
        BitMaskUtils.setBit(buf, 3, true);
        BitMaskUtils.setBit(buf, 9, true);

        assertEquals(3, BitMaskUtils.popcount(buf));

        BitMaskUtils.setBit(buf, 3, false);
        assertEquals(2, BitMaskUtils.popcount(buf));
    }

    @Test
    @DisplayName("occupancy_bits를 64비트까지 부호 없는 long으로 직렬화한다")
    void toUnsignedLong() {
        byte[] buf = new byte[8];
        BitMaskUtils.setBit(buf, 0, true);
        BitMaskUtils.setBit(buf, 5, true);
        BitMaskUtils.setBit(buf, 7, true);

        long v = BitMaskUtils.toUnsignedLong(buf);
        assertEquals(161L, v);
    }
}
