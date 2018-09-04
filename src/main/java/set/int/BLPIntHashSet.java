import java.util.Arrays;


/**
 * An implementation of <a href="https://doi.org/10.1093/comjnl/17.2.135">
 * Amble and Knuth's bidirectional linear probing
 * table</a>, with a simplified insert algorithm from <a
 * href="https://pdfs.semanticscholar.org/6d6c/ca94c57d408c0b1164d6ff7faea25635fedb.pdf">
 * A Concurrent Bidirectional Linear Probing Algorithm</a>, and an original
 * (but trivial) tombstone-free deletion algorithm. The keys (there are
 * no stored values) must be 32-bit integers, which are permuted to form
 * the hash codes (i.e., the "hash function" is reversible). This obviates
 * the need to separately store hash codes or rehash the keys to perform
 * operations which use hash codes.
 *
 * @author tdbaker
 */
public class BLPIntHashSet implements IntSet {

    private int[] arr;
    private int size = 0;

    public BLPIntHashSet(int maxEntries, double loadFactor) {
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
        return (lookupByHash(hash) != -1);
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
        if (bucket != -1) {
            return false;
        }
        bucket = findPreferredBucket(hash);
        if (!isEmpty(bucket)) {
            // If we are at the beginning of the array, then we can only probe to the
            // right. Similarly, if we are at the end of the array, then we can only
            // probe to the left. Otherwise, if the hash occupying the preferred bucket
            // is smaller than our lookup hash, it means the chain is "too far to the
            // right", so we look for an empty bucket to the left and swap it into the
            // insertion point of our lookup hash, moving the whole chain one space
            // to the left. Similar logic applies if the hash occupying the preferred
            // bucket is larger than our lookup hash.
            boolean probeLeft = (bucket > 0 &&
                    isHashLesser(this.arr[bucket], hash)) ||
                bucket == this.arr.length - 1;
            bucket = getEmptyBucketForInsert(hash, bucket, probeLeft, false);
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
        if (bucket == -1) {
            return false;
        }
        MOVE_DIRECTION moveDirection;
        if (bucket == 0) {
            moveDirection = MOVE_DIRECTION.LEFT;
        } else if (bucket == this.arr.length - 1) {
            moveDirection = MOVE_DIRECTION.RIGHT;
        } else {
            moveDirection = getMoveDirection(bucket);
        }
        int endBucket;
        int currBucket = bucket;
        switch (moveDirection) {
            case NONE:
                // if the deleted value is the only entry with its preferred bucket, it can be zeroed out
                this.arr[bucket] = 0;
                break;
            case LEFT:
                // any chain of buckets residing to the right of their preferred buckets can be moved left
                endBucket = findMoveBoundaryToRight(bucket);
                while (currBucket < endBucket) {
                    this.arr[currBucket] = this.arr[currBucket + 1];
                    currBucket += 1;
                }
                // only necessary when endBucket == this.arr.length - 1
                this.arr[endBucket] = 0;
                break;
            case RIGHT:
                // any chain of buckets residing to the left of their preferred buckets can be moved right
                endBucket = findMoveBoundaryToLeft(bucket);
                while (currBucket > endBucket) {
                    this.arr[currBucket] = this.arr[currBucket - 1];
                    currBucket -= 1;
                }
                // only necessary when endBucket == 0
                this.arr[endBucket] = 0;
                break;
        }
        --this.size;
        return true;
    }

    /**
     * Remove all elements from the table.
     */
    public void clear() {
        Arrays.fill(this.arr, 0);
    }

    private static boolean isHashGreater(int hash1, int hash2) {
        return (Integer.compareUnsigned(hash1, hash2) > 0);
    }

    private static boolean isHashLesser(int hash1, int hash2) {
        return (Integer.compareUnsigned(hash1, hash2) < 0);
    }

    private static boolean isHashGreaterOrEqual(int hash1, int hash2) {
        return (Integer.compareUnsigned(hash1, hash2) >= 0);
    }

    private static boolean isHashLesserOrEqual(int hash1, int hash2) {
        return (Integer.compareUnsigned(hash1, hash2) <= 0);
    }

    // https://github.com/skeeto/hash-prospector#two-round-functions
    private static int hash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= 0x7feb352d;
        x ^= x >>> 15;
        x *= 0x846ca68b;
        x ^= x >>> 16;
        return x;
    }

    private static int unhash(int x) {
        assert x != 0;
        x ^= x >>> 16;
        x *= 0x43021123;
        x ^= x >>> 15 ^ x >>> 30;
        x *= 0x1d69e2a5;
        x ^= x >>> 16;
        return x;
    }

    private boolean isEmpty(int bucket) {
        return (this.arr[bucket] == 0);
    }

    private int contents(int bucket) {
        return isEmpty(bucket) ? 0 : unhash(this.arr[bucket]);
    }

    // https://github.com/lemire/fastrange
    private int findPreferredBucket(int hash) {
        if (hash == 0) {
            return -1;
        }
        return (int) ((Integer.toUnsignedLong(hash) * Integer.toUnsignedLong(this.arr.length)) >>> 32);
    }

    private int lookupByHash(int hash) {
        int bucket = findPreferredBucket(hash);
        if (isHashLesser(this.arr[bucket], hash)) {
            while (bucket < this.arr.length - 1 &&
                   !isEmpty(bucket + 1) &&
                   isHashLesserOrEqual(this.arr[bucket + 1], hash)) {
                bucket += 1;
            }
        } else if (isHashGreater(this.arr[bucket], hash)) {
            while (bucket > 0 &&
                   !isEmpty(bucket - 1) &&
                   isHashGreaterOrEqual(this.arr[bucket - 1], hash)) {
                bucket -= 1;
            }
        }
        if (this.arr[bucket] == hash) {
            return bucket;
        }
        return -1;
    }

    // Any bucket to the left of its preferred bucket can be shifted right,
    // since its preferred bucket will be shifted along with it, ensuring
    // it is still reachable from its preferred bucket.
    private int findMoveBoundaryToLeft(int startBucket) {
        int bucket = startBucket;
        while (bucket > 0 &&
               !isEmpty(bucket) &&
               bucket - 1 < findPreferredBucket(this.arr[bucket - 1])) {
            bucket -= 1;
        }
        return bucket;
    }

    // Any bucket to the right of its preferred bucket can be shifted left,
    // since its preferred bucket will be shifted along with it, ensuring
    // it is still reachable from its preferred bucket.
    private int findMoveBoundaryToRight(int startBucket) {
        int bucket = startBucket;
        while (bucket < this.arr.length - 1 &&
               !isEmpty(bucket) &&
               bucket + 1 > findPreferredBucket(this.arr[bucket + 1])) {
            bucket += 1;
        }
        return bucket;
    }

    private static enum MOVE_DIRECTION {
        LEFT,
        RIGHT,
        NONE,
    }

    private MOVE_DIRECTION getMoveDirection(int bucket) {
        assert bucket > 0 && bucket < this.arr.length - 1;
        int prevBucket = bucket - 1;
        int nextBucket = bucket + 1;
        int preferredBucket = findPreferredBucket(this.arr[bucket]);
        int leftPreferredBucket = !isEmpty(prevBucket) ? findPreferredBucket(this.arr[prevBucket]) : -1;
        int rightPreferredBucket = !isEmpty(nextBucket) ? findPreferredBucket(this.arr[nextBucket]) : -1;
        if (bucket == preferredBucket) {
            // if this is the only entry in the deleted entry's chain, just zero out the deleted entry
            if (leftPreferredBucket != preferredBucket && rightPreferredBucket != preferredBucket) {
                return MOVE_DIRECTION.NONE;
            // if the deleted entry's chain extends only to the left, move the chain to the right
            } else if (leftPreferredBucket == preferredBucket && rightPreferredBucket != preferredBucket) {
                return MOVE_DIRECTION.RIGHT;
            // if the deleted entry's chain extends only to the right, move the chain to the left
            } else if (leftPreferredBucket != preferredBucket && rightPreferredBucket == preferredBucket) {
                return MOVE_DIRECTION.LEFT;
            // if the deleted entry's chain extends in both directions,
            // move the "closer" neighboring hash value into the deleted bucket
            } else {
                int prevHashDiff = unsignedAbsDiff(this.arr[bucket], this.arr[prevBucket]);
                int nextHashDiff = unsignedAbsDiff(this.arr[bucket], this.arr[nextBucket]);
                assert isHashGreater(prevHashDiff, 0) && isHashGreater(nextHashDiff, 0);
                return isHashGreater(prevHashDiff, nextHashDiff) ? MOVE_DIRECTION.LEFT : MOVE_DIRECTION.RIGHT;
            }
        } else if (bucket < preferredBucket) {
            return MOVE_DIRECTION.RIGHT;
        } else {  // bucket > preferredBucket
            return MOVE_DIRECTION.LEFT;
        }
    }

    private static int unsignedAbsDiff(int a, int b) {
        if (isHashGreater(a, b)) {
            return a - b;
        } else {
            return b - a;
        }
    }

    private int findFirstEmptyBucketToLeft(int startBucket) {
        assert startBucket > 0;
        int bucket = startBucket;
        while (bucket > 0 && !isEmpty(bucket)) {
            bucket -= 1;
        }
        if (isEmpty(bucket)) {
            return bucket;
        }
        return -1;
    }

    private int findFirstEmptyBucketToRight(int startBucket) {
        assert startBucket < this.arr.length - 1;
        int bucket = startBucket;
        while (bucket < this.arr.length - 1 && !isEmpty(bucket)) {
            bucket += 1;
        }
        if (isEmpty(bucket)) {
            return bucket;
        }
        return -1;
    }

    private int moveEmptyBucketLeftToInsertionPoint(int startBucket, int hash) {
        assert startBucket > 0;
        assert isEmpty(startBucket);
        int bucket = startBucket;
        while (bucket > 0 &&
               !isEmpty(bucket - 1) &&
               isHashGreater(this.arr[bucket - 1], hash)) {
            this.arr[bucket] = this.arr[bucket - 1];
            bucket -= 1;
        }
        return bucket;
    }

    private int moveEmptyBucketRightToInsertionPoint(int startBucket, int hash) {
        assert startBucket < this.arr.length - 1;
        assert isEmpty(startBucket);
        int bucket = startBucket;
        while (bucket < this.arr.length - 1 &&
               !isEmpty(bucket + 1) &&
               isHashLesser(this.arr[bucket + 1], hash)) {
            this.arr[bucket] = this.arr[bucket + 1];
            bucket += 1;
        }
        return bucket;
    }

    private int getEmptyBucketForInsert(int hash, int startBucket, boolean probeLeft, boolean prevProbeFailed) {
        int bucket = startBucket;
        int emptyBucket;
        if (probeLeft) {
            emptyBucket = findFirstEmptyBucketToLeft(bucket);
            if (emptyBucket == -1) {
                if (prevProbeFailed) {
                    throw new RuntimeException("Couldn't insert into table");
                }
                return getEmptyBucketForInsert(hash, bucket, false, true);
            }
            bucket = moveEmptyBucketRightToInsertionPoint(emptyBucket, hash);
        } else {
            emptyBucket = findFirstEmptyBucketToRight(bucket);
            if (emptyBucket == -1) {
                if (prevProbeFailed) {
                    throw new RuntimeException("Couldn't insert into table");
                }
                return getEmptyBucketForInsert(hash, bucket, true, true);
            }
            bucket = moveEmptyBucketLeftToInsertionPoint(emptyBucket, hash);
        }
        return bucket;
    }

    private void dump() {
        for (int i = 0; i < this.arr.length; ++i) {
            System.out.format("%d\t%d\t%s\t%d\n", i, contents(i), Integer.toUnsignedString(this.arr[i]), findPreferredBucket(this.arr[i]));
        }
    }

    public static void main(String[] args) {
        int numEntries = Integer.parseInt(args[0]);
        double loadFactor = Double.parseDouble(args[1]);
        BLPIntHashSet set = new BLPIntHashSet(numEntries, loadFactor);
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
}
