package net.minecraft.server;

import java.util.ArrayDeque;
import java.util.Deque;

/*public class IntCache {
    private static int a = 256;
    private static List<int[]> b = new ArrayList<>();
    private static List<int[]> c = new ArrayList<>();
    private static List<int[]> d = new ArrayList<>();
    private static List<int[]> e = new ArrayList<>();

    public static synchronized int[] a(int i) {
        int[] aint;

        if (i <= 256) {
            if (b.isEmpty()) {
                aint = new int[256];
            } else {
                aint = (int[]) b.remove(b.size() - 1);
            }
            if (c.size() < org.spigotmc.SpigotConfig.intCacheLimit) c.add(aint);
            return aint;
        }
        if (i > a) {
            a = i;
            d.clear();
            e.clear();
            aint = new int[a];
            if (e.size() < org.spigotmc.SpigotConfig.intCacheLimit) e.add(aint);
            return aint;
        }
        if (d.isEmpty()) {
            aint = new int[a];
            if (e.size() < org.spigotmc.SpigotConfig.intCacheLimit) e.add(aint);
            return aint;
        }
        aint = (int[]) d.remove(d.size() - 1);
        if (e.size() < org.spigotmc.SpigotConfig.intCacheLimit) e.add(aint);
        return aint;
    }

    public static synchronized void a() {
        if (!d.isEmpty()) {
            d.remove(d.size() - 1);
        }

        if (!b.isEmpty()) {
            b.remove(b.size() - 1);
        }

        d.addAll(e);
        b.addAll(c);
        e.clear();
        c.clear();
    }

    public static synchronized String b() {
        return "cache: " + d.size() + ", tcache: " + b.size() + ", allocated: " + e.size() + ", tallocated: " + c.size();
    }
}*/

public final class IntCache {
    private static int a = 256;
    private static Deque<int[]> b = new ArrayDeque<int[]>();
    private static Deque<int[]> c = new ArrayDeque<int[]>();
    private static Deque<int[]> d = new ArrayDeque<int[]>();
    private static Deque<int[]> e = new ArrayDeque<int[]>();

    public static synchronized int[] a(int i) {
        int[] aint;

        if (i <= 256) {
        	aint = (b.isEmpty() ? new int[256] : b.removeLast());
        	if (c.size() < org.spigotmc.SpigotConfig.intCacheLimit) c.offer(aint);
        	return aint;
        }
        if (i > a) {
            a = i;
            d.clear();
            e.clear();
            aint = new int[a];
            if (e.size() < org.spigotmc.SpigotConfig.intCacheLimit) e.offer(aint);
            return aint;
        }
        aint = (d.isEmpty() ? new int[a] : d.removeLast());
        if (e.size() < org.spigotmc.SpigotConfig.intCacheLimit) e.offer(aint);
        return aint;
    }

    public static synchronized void a() {
        if (!d.isEmpty()) {
            d.removeLast();
        }
        if (!b.isEmpty()) {
            b.removeLast();
        }

        d.addAll(e);
        b.addAll(c);
        e.clear();
        c.clear();
    }

    public static synchronized String b() {
        return "cache: " + d.size() + ", tcache: " + b.size() + ", allocated: " + e.size() + ", tallocated: " + c.size();
    }
}
