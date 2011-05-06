package biz.k11i.compress.lz4;


public class LZ4Codec {
    static final int MIN_MATCH = 4;

    static final int MAX_DISTANCE_LOG = 16;
    static final int MAX_DISTANCE = 1 << MAX_DISTANCE_LOG;

    static final int ML_BITS = 4;
    static final int ML_MASK = (1 << ML_BITS) - 1;
    static final int RUN_BITS = 8 - ML_BITS;
    static final int RUN_MASK = (1 << RUN_BITS) - 1;

    public static LZ4Compressor createCompressor() {
        return new LZ4CompressorImpl();
    }

    public static LZ4Decompressor createDecompressor() {
        return null;
    }
}
