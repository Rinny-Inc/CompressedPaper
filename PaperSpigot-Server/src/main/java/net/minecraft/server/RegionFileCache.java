package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RegionFileCache {

    public static final Map a = new HashMap(); // CraftBukkit - private -> public

    public static synchronized RegionFile a(File file, int i, int j) {
    	return a(file, i, j, true);
    }
    public static synchronized RegionFile a(File file, int i, int j, boolean create) {
    	File file1 = new File(file, "region");
        File file2 = new File(file1, "r." + (i >> 5) + "." + (j >> 5) + ".mca");
        RegionFile regionfile = (RegionFile) a.get(file2);

        if (regionfile != null) {
            return regionfile;
        } else {
        	if (!create && !file2.exists()) { // PaperSpigot
        		return null; 
        	} 
            if (!file1.exists()) {
                file1.mkdirs();
            }

            if (a.size() >= 256) {
                a();
            }

            RegionFile regionfile1 = new RegionFile(file2);

            a.put(file2, regionfile1);
            return regionfile1;
        }
    }

    public static synchronized void a() {
        Iterator iterator = a.values().iterator();

        while (iterator.hasNext()) {
            RegionFile regionfile = (RegionFile) iterator.next();

            try {
                if (regionfile != null) {
                    regionfile.c();
                }
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }
        }

        a.clear();
    }

    public static DataInputStream c(File file1, int i, int j) {
        RegionFile regionfile = a(file1, i, j);

        return regionfile.a(i & 31, j & 31);
    }

    public static DataOutputStream d(File file1, int i, int j) {
        RegionFile regionfile = a(file1, i, j);

        return regionfile.b(i & 31, j & 31);
    }
}
