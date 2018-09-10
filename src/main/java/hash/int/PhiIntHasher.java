package hash.int;

public class PhiIntHasher implements IntHasher {
    // https://raw.githubusercontent.com/vigna/fastutil/master/src/it/unimi/dsi/fastutil/HashCommon.java
    private static final int INT_PHI = 0x9e3779b9;
    private static final int INV_INT_PHI = 0x144cbc89;

    public static int hash(int x) {
        assert x != 0;
        x *= INT_PHI;
        x ^= x >>> 16;
        return x;
    }

    public static int unhash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= INV_INT_PHI;
        return x;
    }
}
