package hash.int;

public class IdentityIntHasher implements IntHasher {
    @Override
    public int hash(int x) {
        return x;
    }

    @Override
    public int unhash(int x) {
        return x;
    }
}
