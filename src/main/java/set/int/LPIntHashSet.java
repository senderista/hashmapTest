package set.int;

import java.util.Arrays;


/**
 * An implementation of a simple linear probing hash table, with a
 * tombstone-free deletion algorithm taken from _Algorithm Design and
 * Applications_, Section 6.3.3. (Note that because we do not use tombstones,
 * we cannot use the maximum insertion probe length to bound the probe length
 * of unsuccessful lookups.) The keys (there are no stored values) must be
 * 32-bit integers, which are permuted to form the hash codes (i.e., the
 * "hash function" is reversible). This obviates the need to separately store
 * hash codes or rehash the keys to perform operations which use hash codes.
 *
 * @author tdbaker
 */
public class LPIntHashSet implements IntSet {

    protected int[] arr;
    protected int size = 0;

    public LPIntHashSet(int maxEntries, double loadFactor) {
        assert maxEntries > 0;
        assert loadFactor > 0 && loadFactor <= 1.0;
        int arrSize = (int) (maxEntries / loadFactor);
        this.arr = new int[arrSize];
    }

    /**
     * Query the size of the table's backing array.
     *
     * @return the size of the backing array
     */
    public int capacity() {
        return this.arr.length;
    }

    /**
     * Query the number of elements in the table.
     *
     * @return the number of elements in the table
     */
    public int size() {
        assert this.size >= 0;
        return this.size;
    }

    /**
     * Query the table for a value.
     *
     * @param value the 32-bit integer to query the table for
     * @return {@code true} if {@code value} is present in the table, {@code false} otherwise
     */
    public boolean contains(int value) {
        int hash = hash(value);
        int bucket = lookupByHash(hash);
        if (bucket == -1 || isEmpty(bucket)) {
            return false;
        }
        return true;
    }

    /**
     * Add an element to the table.
     *
     * @param element the 32-bit integer to add to the table
     * @return {@code false} if {@code element} was already present in the table, {@code true} otherwise
     */
    public boolean add(int element) {
        int hash = hash(element);
        int bucket = lookupByHash(hash);
        if (bucket == -1) {
            // table full
            throw new RuntimeException("Couldn't insert into table");
        }
        if (!isEmpty(bucket)) {
            return false;
        }
        this.arr[bucket] = hash;
        ++this.size;
        return true;
    }

    /**
     * Remove an element from the table.
     *
     * @param value the 32-bit integer to remove from the table
     * @return {@code false} if {@code value} was not present in the table, {@code true} otherwise
     */
    public boolean remove(int value) {
        int hash = hash(value);
        int bucket = lookupByHash(hash);
        if (bucket == -1 || isEmpty(bucket)) {
            return false;
        }
        this.arr[bucket] = 0;
        shift(bucket);
        --this.size;
        return true;
    }

    /**
     * Remove all elements from the table.
     */
    public void clear() {
        Arrays.fill(this.arr, 0);
    }

    protected boolean isEmpty(int bucket) {
        return (this.arr[bucket] == 0);
    }

    protected int contents(int bucket) {
        return isEmpty(bucket) ? 0 : unhash(this.arr[bucket]);
    }

    // https://github.com/lemire/fastrange
    protected int findPreferredBucket(int hash) {
        if (hash == 0) {
            return -1;
        }
        return (int) ((Integer.toUnsignedLong(hash) * Integer.toUnsignedLong(this.arr.length)) >>> 32);
    }

    protected int wrap(int pos) {
        if (pos < 0) {
            return this.arr.length + pos;
        }
        if (pos > this.arr.length - 1) {
            return pos - this.arr.length;
        }
        return pos;
    }

    // https://github.com/skeeto/hash-prospector#two-round-functions
    protected int hash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= 0x7feb352d;
        x ^= x >>> 15;
        x *= 0x846ca68b;
        x ^= x >>> 16;
        return x;
    }

    protected int unhash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= 0x43021123;
        x ^= x >>> 15 ^ x >>> 30;
        x *= 0x1d69e2a5;
        x ^= x >>> 16;
        return x;
    }

    protected int lookupByHash(int hash) {
        int bucket = findPreferredBucket(hash);
        int probeLength = 0;
        while (!isEmpty(bucket) && this.arr[bucket] != hash) {
            if (probeLength == this.arr.length) {
                return -1;
            }
            bucket = wrap(bucket + 1);
            ++probeLength;
        }
        return bucket;
    }

    // uses pseudocode from _Algorithm Design and Applications_, Section 6.3.3
    protected void shift(int startBucket) {
        int dst = startBucket;
        int shift = 1;
        int src = wrap(dst + shift);
        while (!isEmpty(src)) {
             int preferredBucket = findPreferredBucket(this.arr[src]);
             // we can only move a key if its destination can be reached from its preferred bucket
             boolean reachable;
             if (src <= dst) {
                reachable = (preferredBucket <= dst && preferredBucket > src);
             } else {
                reachable = (preferredBucket <= dst || preferredBucket > src);
             }
             if (reachable) {
                this.arr[dst] = this.arr[src];  // fill the hole
                this.arr[src] = 0;  // move the hole
                dst = wrap(dst + shift);
                shift = 1;
             } else {
                ++shift;
             }
             src = wrap(dst + shift);
        }
    }

    protected void dump() {
        for (int i = 0; i < this.arr.length; ++i) {
            System.out.format("%d\t%d\t%s\t%d\n", i, contents(i), Integer.toUnsignedString(this.arr[i]), findPreferredBucket(this.arr[i]));
        }
    }

    protected static void test(String[] args, Class cls) {
        int numEntries = Integer.parseInt(args[0]);
        double loadFactor = Double.parseDouble(args[1]);
        Constructor constructor = cls.getConstructor(int.class, double.class);
        IntSet set = (IntSet) constructor.newInstance(numEntries, loadFactor);
        System.out.println("Array size: " + set.capacity());
        for (int i = 0; i < numEntries; ++i) {
            assert set.add(i + 1) : i + 1;
        }
        // set.dump();
        System.out.println(set.size());
        assert set.size() == numEntries;
        for (int i = 0; i < numEntries; ++i) {
            assert set.contains(i + 1) : i + 1;
        }
        for (int i = 0; i < numEntries; ++i) {
            assert set.remove(i + 1) : i + 1;
        }
        System.out.println(set.size());
        assert set.size() == 0;
        for (int i = 0; i < numEntries; ++i) {
            assert !set.contains(i + 1) : i + 1;
        }
    }

    public static void main(String[] args) {
        test(args, LPIntHashSet.class);
    }
}
