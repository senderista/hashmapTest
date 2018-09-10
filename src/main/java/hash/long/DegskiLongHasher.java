package hash.long;

public class DegskiLongHasher implements LongHasher {
    // https://gist.github.com/degski/6e2069d6035ae04d5d6f64981c995ec2
    public long hash(long x) {
        assert x != 0;
        x ^= x >>> 32;
        x *= 0xD6E8FEB86659FD93L;
        x ^= x >>> 32;
        x *= 0xD6E8FEB86659FD93L;
        x ^= x >>> 32;
        return x;
    }

    public long unhash(long x) {
        assert x != 0;
        x ^= x >>> 32;
        x *= 0xCFEE444D8B59A89B;
        x ^= x >>> 32;
        x *= 0xCFEE444D8B59A89B;
        x ^= x >>> 32;
        return x;
    }
}