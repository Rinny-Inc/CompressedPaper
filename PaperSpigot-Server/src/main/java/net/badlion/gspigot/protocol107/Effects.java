package net.badlion.gspigot.protocol107;

import net.minecraft.util.gnu.trove.map.TIntIntMap;
import net.minecraft.util.gnu.trove.map.hash.TIntIntHashMap;

public class Effects {
	public static final TIntIntMap MAP = (TIntIntMap) new TIntIntHashMap();

	static {
		MAP.put(1003, 1006);
		MAP.put(1004, 1009);
		MAP.put(1005, 1010);
		MAP.put(1007, 1015);
		MAP.put(1008, 1016);
		MAP.put(1009, 1016);
		MAP.put(1010, 1019);
		MAP.put(1011, 1020);
		MAP.put(1012, 1021);
		MAP.put(1013, 1023);
		MAP.put(1014, 1024);
		MAP.put(1015, 1025);
		MAP.put(1016, 1026);
		MAP.put(1017, 1027);
		MAP.put(1018, 1028);
		MAP.put(1020, 1029);
		MAP.put(1021, 1030);
		MAP.put(1022, 1031);
	}

	public static int convert(int id) {
		if (MAP.containsKey(id)) {
			return MAP.get(id);
		}
		return id;
	}
}
