package biz.k11i.compress.lz4;

public class LZ4Demo {

    /**
     * @param args
     */
    public static void main(String[] args) {
        LZ4Compressor comp = LZ4Codec.createCompressor();

        byte[] src = new byte[2048];
        byte[] dest = new byte[2048];

        for (int i = 0; i < 256; i++) {
            src[i] = (byte) i;
        }
        
        for (int i = 256; i < 700; i++) {
            src[i] = 30;
        }

        comp.compress(src, 1024, dest);
    }
}
