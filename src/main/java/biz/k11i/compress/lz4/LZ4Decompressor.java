package biz.k11i.compress.lz4;

public interface LZ4Decompressor {
    int decompress(byte[] src, byte[] dest);

    int decompress(byte[] src, int srcSize, byte[] dest);
}
