package hash.int;

public class Prospector3RoundIntHasher implements IntHasher {
    // https://github.com/skeeto/hash-prospector#three-round-functions
    @Override
    public int hash(int x) {
        assert x != 0;
        x ^= x >>> 17;
        x *= 0xed5ad4bb;
        x ^= x >>> 11;
        x *= 0xac4c1b51;
        x ^= x >>> 15;
        x *= 0x31848bab;
        x ^= x >>> 14;
        return x;
    }

    @Override
    public int unhash(int x) {
        assert x != 0;
        x ^= x >>> 14 ^ x >>> 28;
        x *= 0x32b21703;
        x ^= x >>> 15 ^ x >>> 30;
        x *= 0x469e0db1;
        x ^= x >>> 11 ^ x >>> 22;
        x *= 0x79a85073;
        x ^= x >>> 17;
        return x;
    }
}
