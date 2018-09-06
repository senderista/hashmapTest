public class SpeckIntHasher implements IntHasher {
    // 32-bit Speck block cipher requires 64-bit key, default 20 rounds
    private final Speck32Cipher encryptor;
    private final Speck32Cipher decryptor;
    private final byte[] key;
    private final int rounds;

    public SpeckIntHasher(byte[] key, int rounds) {
        this.encryptor = new Speck32Cipher(rounds);
        this.decryptor = new Speck32Cipher(rounds);
        encryptor.init(true, key);
        decryptor.init(false, key);
    }

    @Override
    public int hash(int x) {
        return encryptor.processBlock(x);
    }
    @Override
    public int unhash(int x){
        return decryptor.processBlock(x);
    }
}
