package biz.k11i.compress.lz4;

public class LZ4DecompressorImpl implements LZ4Decompressor {

    @Override
    public int decompress(byte[] src, byte[] dest) {
        return decompress(src, src.length, dest);
    }

    @Override
    public int decompress(byte[] src, int srcSize, byte[] dest) {
        int srcPos = 0;
        int destPos = 0;

        int runCode = 0;
        int literalLength = 0;
        int matchLength = 0;
        
        int distance = 0;
        int copyPos = 0;
        
        while (srcPos < srcSize) {
            // TODO int •ÏŠ·
            
            // get runlength
            runCode = src[srcPos++];

            // copy literals
            literalLength = (runCode >> LZ4Codec.ML_BITS);
            if (literalLength == LZ4Codec.RUN_MASK) {
                while (src[srcPos] == 255) {
                    literalLength += 255;
                    srcPos++;
                }

                literalLength += src[srcPos++];
            }

            System.arraycopy(src, srcPos, dest, destPos, literalLength);
            destPos += literalLength;
            
            // check EOF
            if (srcPos >= srcSize) {
                break;
            }
            
            // get distance
            distance = (src[srcPos] << 8) | src[srcPos + 1]; // TODO int •ÏŠ·
            srcPos += 2;
            
            copyPos = destPos - distance;
            
            // get match length
            matchLength = runCode & LZ4Codec.ML_MASK;
            if (matchLength == LZ4Codec.ML_MASK) {
                while (src[srcPos] == 255) {
                    matchLength += 255;
                    srcPos++;
                }
                
                matchLength += src[srcPos++];
            }
            matchLength += LZ4Codec.MIN_MATCH;
            
            // copy repeated sequence
            while (matchLength-- > 0) {
                dest[destPos++] = dest[copyPos++];
            }
        }
        
        return destPos;
    }
}
