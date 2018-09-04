public class H2IntHasher implements IntHasher {
    // https://github.com/h2database/h2database
    public static int hash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= 0x45d9f3b;
        x ^= x >>> 16;
        x *= 0x45d9f3b;
        x ^= x >>> 16;
        return x;
    }

    public static int unhash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= 0x119de1f3;
        x ^= x >>> 16;
        x *= 0x119de1f3;
        x ^= x >>> 16;
        return x;
    }
}
