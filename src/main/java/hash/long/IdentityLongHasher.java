package hash.long;

public class IdentityLongHasher implements LongHasher {
    @Override
    public long hash(long x) {
        return x;
    }

    @Override
    public long unhash(long x) {
        return x;
    }
}
