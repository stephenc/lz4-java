package biz.k11i.compress.lz4;

import java.util.Arrays;

public class LZ4CompressorImpl implements LZ4Compressor {
    private static final int INCOMPRESSIBLE = 128;

    private static final int HASH_LOG = 17;
    static final int HASH_TABLE_SIZE = 1 << HASH_LOG;
    static final int HASH_RIGHT_SHIFT_COUNT = LZ4Codec.MIN_MATCH * 8 - HASH_LOG;

    private int[] posHashTable;

    @Override
    public int compress(byte[] src, byte[] dest) {
        return compress(src, src.length, dest);
    }

    @Override
    public int compress(byte[] src, int srcSize, byte[] dest) {
        initializeCompression();

        int srcPos = 0;
        int srcLimit = srcSize - LZ4Codec.MIN_MATCH;

        int destPos = 0;

        int seq = 0;// (src[0] << 16) | (src[1] << 8) | src[2];
        int hashVal = 0;

        int refPos = 0;
        int distance = 0;

        int step = 1;
        int limit = INCOMPRESSIBLE;
        int anchor = 0;

        int runCodePos = 0;
        int literalLength = 0;
        int matchLength = 0;

        while (srcPos < srcLimit) {
            seq = (src[srcPos] << 24) | (src[srcPos + 1] << 16)
                    | (src[srcPos + 2] << 8) | src[srcPos + 3];
            hashVal = ((int) (seq * 2654435761L) >>> HASH_RIGHT_SHIFT_COUNT);

            refPos = posHashTable[hashVal];
            posHashTable[hashVal] = srcPos;

            distance = srcPos - refPos;

            // Min Match
            if (distance >= LZ4Codec.MAX_DISTANCE
                    || seq != ((src[refPos] << 24) | (src[refPos + 1] << 16)
                            | (src[refPos + 2] << 8) | src[refPos + 3])) {
                if (srcPos - anchor > limit) {
                    limit <<= 1;
                    step += 1 + (step >> 2);
                }

                srcPos += step;
                continue;
            }

            // catch up
            if (step > 1) {
                posHashTable[hashVal] = refPos;
                srcPos -= (step - 1);
                step = 1;
                continue;
            }

            limit = INCOMPRESSIBLE;

            literalLength = srcPos - anchor;
            runCodePos = destPos;
            destPos++;

            // Encode Literal length
            if (literalLength > (LZ4Codec.RUN_MASK - 1)) {
                dest[runCodePos] = (byte) (LZ4Codec.RUN_MASK << LZ4Codec.ML_BITS);
                destPos = encodeLength(dest, destPos, literalLength
                        - LZ4Codec.RUN_MASK);

            } else {
                dest[runCodePos] = (byte) (literalLength << LZ4Codec.ML_BITS);
            }

            // Copy Literals
            System.arraycopy(src, anchor, dest, destPos, literalLength);
            destPos += literalLength;

            // Encode Offset (little endian)
            if (distance < 0) {
                throw new RuntimeException("distance Ç™ 0 ñ¢ñûÇ…Ç»Ç¡ÇƒÇµÇ‹Ç¡ÇƒÇ¢Ç‹Ç∑ÅB");
            }
            dest[destPos++] = (byte) (distance & 0xFF);
            dest[destPos++] = (byte) ((distance >> 8) & 0xFF);

            // Start Counting
            srcPos += LZ4Codec.MIN_MATCH;
            refPos += LZ4Codec.MIN_MATCH;
            anchor = srcPos;
            while (srcPos < srcSize && src[srcPos] == src[refPos]) {
                srcPos++;
                refPos++;
            }

            matchLength = srcPos - anchor;

            // Encode Match length
            if (matchLength > (LZ4Codec.ML_MASK - 1)) {
                dest[runCodePos] |= (byte) LZ4Codec.ML_MASK;
                destPos = encodeLength(dest, destPos, matchLength
                        - LZ4Codec.ML_MASK);

            } else {
                dest[runCodePos] |= (byte) matchLength;
            }

            anchor = srcPos;
        }

        literalLength = srcSize - anchor;
        if (literalLength > (LZ4Codec.RUN_MASK - 1)) {
            dest[destPos++] = (byte) (LZ4Codec.RUN_MASK << LZ4Codec.ML_BITS);
            destPos = encodeLength(dest, destPos, literalLength
                    - LZ4Codec.RUN_MASK);

        } else {
            dest[destPos++] = (byte) (literalLength << LZ4Codec.ML_BITS);
        }

        System.arraycopy(src, anchor, dest, destPos, literalLength);
        destPos += literalLength;

        return destPos;
    }

    private void initializeCompression() {
        if (posHashTable == null) {
            posHashTable = new int[HASH_TABLE_SIZE];
        }

        Arrays.fill(posHashTable, -LZ4Codec.MAX_DISTANCE);
    }

    private static final int encodeLength(byte[] dest, int destPos, int length) {
        if (length  < 0) {
            throw new IllegalArgumentException("length ÇÕ 0 à»è„Ç≈Ç»ÇØÇÍÇŒÇ»ÇËÇ‹ÇπÇÒÅB");
        }

        while (length > 254) {
            length -= 255;
            dest[destPos++] = (byte) 255; // TODO íËêîêÈåæÇ∑ÇÈ
        }

        return destPos;
    }
}