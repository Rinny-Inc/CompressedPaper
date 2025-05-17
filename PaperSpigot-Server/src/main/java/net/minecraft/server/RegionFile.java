package net.minecraft.server;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class RegionFile {

    private static final byte[] a = new byte[4096];
    private final File file;
    private RandomAccessFile accessFile;
    private final int[] d = new int[1024]; // TODO: unify d and e
    private final int[] e = new int[1024]; // TODO: this copies d (up)
    private ArrayList<Boolean> f;
    private int g;
    private long h;

    public RegionFile(File file1) {
        this.file = file1;
        this.g = 0;

        try {
            if (file1.exists()) {
                this.h = file1.lastModified();
            }

            this.accessFile = new RandomAccessFile(file1, "rw");
            int i;

            if (this.accessFile.length() < 4096L) {
            	this.accessFile.write(RegionFile.a);
            	this.accessFile.write(RegionFile.a);

                this.g += 8192;
            }

            if ((this.accessFile.length() & 4095L) != 0L) {
                for (i = 0; (long) i < (this.accessFile.length() & 4095L); ++i) {
                    this.accessFile.write(0);
                }
            }

            i = (int) this.accessFile.length() / 4096;
            this.f = new ArrayList<Boolean>(i);

            int j;

            for (j = 0; j < i; ++j) {
                this.f.add(true);
            }

            this.f.set(0, false);
            this.f.set(1, false);
            this.accessFile.seek(0L);

            int k;

            for (j = 0; j < 1024; ++j) {
                k = this.accessFile.readInt();
                this.d[j] = k;
                if (k != 0 && (k >> 8) + (k & 255) <= this.f.size()) {
                    for (int l = 0; l < (k & 255); ++l) {
                        this.f.set((k >> 8) + l, false);
                    }
                }
            }

            for (j = 0; j < 1024; ++j) {
                k = this.accessFile.readInt();
                this.e[j] = k;
            }
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    // CraftBukkit start - This is a copy (sort of) of the method below it, make sure they stay in sync
    public synchronized boolean chunkExists(int i, int j) {
        if (this.d(i, j)) {
            return false;
        } else {
            try {
                int k = this.e(i, j);

                if (k == 0) {
                    return false;
                } else {
                    int l = k >> 8;
                    int i1 = k & 255;

                    if (l + i1 > this.f.size()) {
                        return false;
                    }

                    this.accessFile.seek((long) (l * 4096));
                    int j1 = this.accessFile.readInt();

                    if (j1 > 4096 * i1 || j1 <= 0) {
                        return false;
                    }

                    byte b0 = this.accessFile.readByte();
                    if (b0 == 1 || b0 == 2) {
                        return true;
                    }
                }
            } catch (IOException ioexception) {
                return false;
            }
        }

        return false;
    }
    // CraftBukkit end

    public synchronized DataInputStream a(int i, int j) {
        if (this.d(i, j)) {
            return null;
        } else {
            try {
                int k = this.e(i, j);

                if (k == 0) {
                    return null;
                } else {
                    int l = k >> 8;
                    int i1 = k & 255;

                    if (l + i1 > this.f.size()) {
                        return null;
                    } else {
                        this.accessFile.seek((long) (l * 4096));
                        int j1 = this.accessFile.readInt();

                        if (j1 > 4096 * i1) {
                            return null;
                        } else if (j1 <= 0) {
                            return null;
                        } else {
                            byte b0 = this.accessFile.readByte();
                            byte[] abyte;

                            if (b0 == 1 || b0 == 2) {
                                abyte = new byte[j1 - 1];
                                this.accessFile.read(abyte);
                                final DataInputStream input = b0 == 1 ? new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte)))) : new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(abyte))));
                                return input;
                            } else {
                                return null;
                            }
                        }
                    }
                }
            } catch (IOException ioexception) {
                return null;
            }
        }
    }
    
    public DataOutputStream b(int i, int j) { // PAIL: getChunkOutputStream
    	// PAIL: isInvalidRegion
    	return this.d(i, j) ? null : new DataOutputStream(new java.io.BufferedOutputStream(new DeflaterOutputStream(new ChunkBuffer(this, i, j))));
	}

    protected synchronized void a(int i, int j, byte[] abyte, int k) {
        try {
            int l = this.e(i, j);
            int i1 = l >> 8;
            int j1 = l & 255;
            int k1 = (k + 5) / 4096 + 1;

            if (k1 >= 256) {
                return;
            }

            if (i1 != 0 && j1 == k1) {
                this.a(i1, abyte, k);
            } else {
                int l1;

                for (l1 = 0; l1 < j1; ++l1) {
                    this.f.set(i1 + l1, true);
                }

                l1 = this.f.indexOf(true);
                int i2 = 0;
                int j2;

                if (l1 != -1) {
                    for (j2 = l1; j2 < this.f.size(); ++j2) {
                        if (i2 != 0) {
                            if (this.f.get(j2).booleanValue()) {
                                ++i2;
                            } else {
                                i2 = 0;
                            }
                        } else if (this.f.get(j2).booleanValue()) {
                            l1 = j2;
                            i2 = 1;
                        }

                        if (i2 >= k1) {
                            break;
                        }
                    }
                }

                if (i2 >= k1) {
                    i1 = l1;
                    this.a(i, j, l1 << 8 | k1);

                    for (j2 = 0; j2 < k1; ++j2) {
                        this.f.set(i1 + j2, false);
                    }

                    this.a(i1, abyte, k);
                } else {
                    this.accessFile.seek(this.accessFile.length());
                    i1 = this.f.size();

                    for (j2 = 0; j2 < k1; ++j2) {
                        this.accessFile.write(a);
                        this.f.add(false);
                    }

                    this.g += 4096 * k1;
                    this.a(i1, abyte, k);
                    this.a(i, j, i1 << 8 | k1);
                }
            }

            this.b(i, j, (int) (MinecraftServer.ar() / 1000L));
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    private void a(int i, byte[] abyte, int j) throws IOException { // CraftBukkit - added throws
        this.accessFile.seek((long) (i * 4096));
        this.accessFile.writeInt(j + 1);
        this.accessFile.writeByte(2);
        this.accessFile.write(abyte, 0, j);
    }

    private boolean d(int i, int j) {
        return i < 0 || i >= 32 || j < 0 || j >= 32;
    }

    private int e(int i, int j) {
        return this.d[i + j * 32];
    }

    public boolean c(int i, int j) {
        return this.e(i, j) != 0;
    }

    private void a(int i, int j, int k) throws IOException { // CraftBukkit - added throws
        this.d[i + j * 32] = k;
        this.accessFile.seek((long) ((i + j * 32) * 4));
        this.accessFile.writeInt(k);
    }

    private void b(int i, int j, int k) throws IOException { // CraftBukkit - added throws
        this.e[i + j * 32] = k;
        this.accessFile.seek((long) (4096 + (i + j * 32) * 4));
        this.accessFile.writeInt(k);
    }

    public void c() throws IOException { // CraftBukkit - added throws
        if (this.accessFile != null) {
            this.accessFile.close();
        }
    }
}
