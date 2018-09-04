public class PhiLongHasher implements LongHasher {
    // https://raw.githubusercontent.com/vigna/fastutil/master/src/it/unimi/dsi/fastutil/HashCommon.java
    private static final long LONG_PHI = 0x9e3779b97f4a7c15l;
    private static final long INV_LONG_PHI = 0xf1de83e19937733dL;

    public long hash(long x) {
        assert x != 0;
        x *= LONG_PHI;
        x ^= x >>> 32;
        x ^= x >>> 16;
        return x;
    }

    public long unhash(long x) {
        assert x != 0;
        x ^= x >>> 32;
        x ^= x >>> 16;
        x ^= x >>> 32;
        x *= INV_LONG_PHI;
        return x;
    }
}
