package net.minecraft.server;

public class NibbleArray {
    public final byte[] a; // data
    private final int yBitShift;
    private final int zBitShift;

    public NibbleArray(int i, int j) {
        this.a = new byte[i >> 1];
        this.yBitShift = j;
        this.zBitShift = j + 4;
    }

    public NibbleArray(byte[] abyte, int i) {
        this.a = abyte;
        this.yBitShift = i;
        this.zBitShift = i + 4;
    }

    public int a(int i, int j, int k) {
        int l = j << this.zBitShift | k << this.yBitShift | i;
        int i1 = l >> 1;
        int j1 = l & 1;

        return j1 == 0 ? this.a[i1] & 15 : this.a[i1] >> 4 & 15;
    }

    public void a(int i, int j, int k, int l) {
        int i1 = j << this.zBitShift | k << this.yBitShift | i;
        int j1 = i1 >> 1;
        int k1 = i1 & 1;

        if (k1 == 0) {
            this.a[j1] = (byte) (this.a[j1] & 240 | l & 15);
        } else {
            this.a[j1] = (byte) (this.a[j1] & 15 | (l & 15) << 4);
        }
    }
}
