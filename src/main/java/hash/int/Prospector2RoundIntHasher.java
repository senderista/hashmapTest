package hash.int;

public class Prospector2RoundIntHasher implements IntHasher {
    // https://github.com/skeeto/hash-prospector#two-round-functions
    public int hash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= 0x7feb352d;
        x ^= x >>> 15;
        x *= 0x846ca68b;
        x ^= x >>> 16;
        return x;
    }

    public int unhash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= 0x43021123;
        x ^= x >>> 15 ^ x >>> 30;
        x *= 0x1d69e2a5;
        x ^= x >>> 16;
        return x;
    }
}
