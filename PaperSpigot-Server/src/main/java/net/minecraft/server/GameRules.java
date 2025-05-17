package net.minecraft.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameRules {
	private Map<String, GameRuleValue> a = new HashMap<>();

	public GameRules() {
		a("doFireTick", "true");
		a("mobGriefing", "true");
		a("keepInventory", "false");
		a("doMobSpawning", "true");
		a("doMobLoot", "true");
		a("doTileDrops", "true");
		a("commandBlockOutput", "true");
		a("naturalRegeneration", "true");
		a("doDaylightCycle", "true");
		a("doLeavesDecay", "true"); // Rinny
	}

	public void a(String paramString1, String paramString2) {
		this.a.put(paramString1, new GameRuleValue(paramString2));
	}

	public void set(String paramString1, String paramString2) {
		GameRuleValue gameRuleValue = (GameRuleValue) this.a.get(paramString1);
		if (gameRuleValue != null) {
			gameRuleValue.a(paramString2);
		} else {
			a(paramString1, paramString2);
		}
	}

	public String get(String paramString) {
		GameRuleValue gameRuleValue = (GameRuleValue) this.a.get(paramString);
		if (gameRuleValue != null)
			return gameRuleValue.a();
		return "";
	}

	public boolean getBoolean(String paramString) {
		GameRuleValue gameRuleValue = (GameRuleValue) this.a.get(paramString);
		if (gameRuleValue != null)
			return gameRuleValue.b();
		return false;
	}

	public NBTTagCompound a() {
		NBTTagCompound nBTTagCompound = new NBTTagCompound();
		for (String str : this.a.keySet()) {
			GameRuleValue gameRuleValue = (GameRuleValue) this.a.get(str);
			nBTTagCompound.setString(str, gameRuleValue.a());
		}
		return nBTTagCompound;
	}

	public void a(NBTTagCompound paramNBTTagCompound) {
		Set<String> set = paramNBTTagCompound.c();
		for (String str1 : set) {
			String str2 = str1;
			String str3 = paramNBTTagCompound.getString(str1);
			set(str2, str3);
		}
	}

	public String[] getGameRules() {
		return (String[]) this.a.keySet().toArray((Object[]) new String[0]);
	}

	public boolean contains(String paramString) {
		return this.a.containsKey(paramString);
	}
}
