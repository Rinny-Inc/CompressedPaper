package io.noks.utils;

import java.io.Serializable;

public interface RandomnessSource extends Serializable {
    int next(final int p0);
    
    long nextLong();
    
    RandomnessSource copy();
}
