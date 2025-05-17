package io.noks.utils;

public class LightRNG implements RandomnessSource, StatefulRandomness {
    /*private static final long DOUBLE_MASK = 9007199254740991L;
    private static final double NORM_53 = 1.1102230246251565E-16;
    private static final long FLOAT_MASK = 16777215L;
    private static final double NORM_24 = 5.9604644775390625E-8;*/
    private static final long serialVersionUID = -374415589203474497L;
    public long state;
    
    public LightRNG() {
        this((long)Math.floor(Math.random() * 9.223372036854776E18));
    }
    
    public LightRNG(final long seed) {
        this.setSeed(seed);
    }
    
    @Override
    public int next(final int bits) {
        return (int)(this.nextLong() & (1L << bits) - 1L);
    }
    
    @Override
    public long nextLong() {
        final long state = this.state - 7046029254386353131L;
        this.state = state;
        long z = state;
        z = (z ^ z >>> 30) * -4658895280553007687L;
        z = (z ^ z >>> 27) * -7723592293110705685L;
        return z ^ z >>> 31;
    }
    
    @Override
    public RandomnessSource copy() {
        return new LightRNG(this.state);
    }
    
    public int nextInt() {
        return (int)this.nextLong();
    }
    
    public int nextInt(final int bound) {
        if (bound <= 0) {
            return 0;
        }
        final int threshold = (Integer.MAX_VALUE - bound + 1) % bound;
        int bits;
        do {
            bits = (int)(this.nextLong() & 0x7FFFFFFFL);
        } while (bits < threshold);
        return bits % bound;
    }
    
    public int nextInt(final int lower, final int upper) {
        if (upper - lower <= 0) {
            throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        }
        return lower + this.nextInt(upper - lower);
    }
    
    public long nextLong(final long bound) {
        if (bound <= 0L) {
            return 0L;
        }
        final long threshold = (Long.MAX_VALUE - bound + 1L) % bound;
        long bits;
        do {
            bits = (this.nextLong() & Long.MAX_VALUE);
        } while (bits < threshold);
        return bits % bound;
    }
    
    public long nextLong(final long lower, final long upper) {
        if (upper - lower <= 0L) {
            throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        }
        return lower + this.nextLong(upper - lower);
    }
    
    public double nextDouble() {
        return (this.nextLong() & 0x1FFFFFFFFFFFFFL) * 1.1102230246251565E-16;
    }
    
    public double nextDouble(final double outer) {
        return this.nextDouble() * outer;
    }
    
    public float nextFloat() {
        return (float)((this.nextLong() & 0xFFFFFFL) * 5.9604644775390625E-8);
    }
    
    public boolean nextBoolean() {
        return (this.nextLong() & 0x1L) != 0x0L;
    }
    
    public void nextBytes(final byte[] bytes) {
        int i = bytes.length;
        int n = 0;
        while (i != 0) {
            n = Math.min(i, 8);
            long bits = this.nextLong();
            while (n-- != 0) {
                bytes[--i] = (byte)bits;
                bits >>= 8;
            }
        }
    }
    
    public void setSeed(final long seed) {
        this.state = seed;
    }
    
    @Override
    public void setState(final long seed) {
        this.state = seed;
    }
    
    @Override
    public long getState() {
        return this.state;
    }
    
    public long skip(final long advance) {
        return this.state += -7046029254386353131L * advance;
    }
    
    @Override
    public String toString() {
        return "LightRNG";
    }
}
