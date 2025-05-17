package io.noks.utils;

public interface StatefulRandomness extends RandomnessSource {
    long getState();
    
    void setState(final long p0);
}
