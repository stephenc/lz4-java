package biz.k11i.compress.lz4;

public interface LZ4Compressor {
    int compress(byte[] src, byte[] dest);

    int compress(byte[] src, int srcSize, byte[] dest);
}
