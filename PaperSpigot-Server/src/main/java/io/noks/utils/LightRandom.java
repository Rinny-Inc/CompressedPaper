package io.noks.utils;

import java.util.Random;

public class LightRandom extends Random {
    public static LightRNG light = new LightRNG();
    private static final long serialVersionUID = 1L;
    
    public int next(final int bits) {
        return LightRandom.light.next(bits);
    }
    
    @Override
    public void nextBytes(final byte[] bytes) {
        LightRandom.light.nextBytes(bytes);
    }
    
    @Override
    public int nextInt() {
        return LightRandom.light.nextInt();
    }
    
    @Override
    public int nextInt(final int n) {
        return LightRandom.light.nextInt(n);
    }
    
    @Override
    public long nextLong() {
        return LightRandom.light.nextLong();
    }
    
    @Override
    public boolean nextBoolean() {
        return LightRandom.light.nextBoolean();
    }
    
    @Override
    public float nextFloat() {
        return LightRandom.light.nextFloat();
    }
    
    @Override
    public double nextDouble() {
        return LightRandom.light.nextDouble();
    }
}
