public class WangLongHasher implements LongHasher {
    // https://naml.us/post/inverse-of-a-hash-function/
    public long hash(long x) {
      x = (~x) + (x << 21); // x = (x << 21) - x - 1;
      x = x ^ (x >>> 24);
      x = (x + (x << 3)) + (x << 8); // x * 265
      x = x ^ (x >>> 14);
      x = (x + (x << 2)) + (x << 4); // x * 21
      x = x ^ (x >>> 28);
      x = x + (x << 31);
      return x;
    }

    public long unhash(long x) {
      long tmp;

      // Invert x = x + (x << 31)
      tmp = x - (x << 31);
      x = x - (tmp << 31);

      // Invert x = x ^ (x >> 28)
      tmp = x ^ x >>> 28;
      x = x ^ tmp >>> 28;

      // Invert x *= 21
      x *= 14933078535860113213L;

      // Invert x = x ^ (x >> 14)
      tmp = x ^ x >>> 14;
      tmp = x ^ tmp >>> 14;
      tmp = x ^ tmp >>> 14;
      x = x ^ tmp >>> 14;

      // Invert x *= 265
      x *= 15244667743933553977L;

      // Invert x = x ^ (x >> 24)
      tmp = x ^ x >>> 24;
      x = x ^ tmp >>> 24;

      // Invert x = (~x) + (x << 21)
      tmp = ~x;
      tmp = ~(x - (tmp << 21));
      tmp = ~(x - (tmp << 21));
      x = ~(x - (tmp << 21));

      return x;
    }
}
