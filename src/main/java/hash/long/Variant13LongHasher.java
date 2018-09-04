public class Variant13LongHasher implements LongHasher {
    // Variant 13 of Murmur3 64-bit finalizer (http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html)
    public long hash(long x) {
        assert x != 0;
        x ^= x >>> 30;
        x *= 0xbf58476d1ce4e5b9L;
        x ^= x >>> 27;
        x *= 0x94d049bb133111ebL;
        x ^= x >>> 31;
        return x;
    }

    public long unhash(long x) {
        assert x != 0;
        x ^= x >>> 31 ^ x >>> 62;
        x *= 0x319642b2d24d8ec3L;
        x ^= x >>> 27 ^ x >>> 54;
        x *= 0x96de1b173f119089;
        x ^= x >>> 30 ^ x >>> 60;
        return x;
    }
}
