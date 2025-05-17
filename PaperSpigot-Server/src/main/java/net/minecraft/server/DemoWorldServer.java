package net.minecraft.server;

public class DemoWorldServer extends WorldServer {
	private static final long J = "North Carolina".hashCode();

	public static final WorldSettings a = (new WorldSettings(J, EnumGamemode.SURVIVAL, true, false, WorldType.NORMAL)).a();

	public DemoWorldServer(MinecraftServer paramMinecraftServer, IDataManager paramIDataManager, String paramString, int paramInt) {
		//super(paramMinecraftServer, paramIDataManager, paramString, paramInt, a);
		super(paramMinecraftServer, paramIDataManager, paramString, paramInt, a, null, null);
	}
}
