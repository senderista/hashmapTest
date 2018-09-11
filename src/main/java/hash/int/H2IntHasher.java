package hash.int;

public class H2IntHasher implements IntHasher {
    // https://github.com/h2database/h2database
    @Override
    public int hash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= 0x45d9f3b;
        x ^= x >>> 16;
        x *= 0x45d9f3b;
        x ^= x >>> 16;
        return x;
    }

    @Override
    public int unhash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= 0x119de1f3;
        x ^= x >>> 16;
        x *= 0x119de1f3;
        x ^= x >>> 16;
        return x;
    }
}
