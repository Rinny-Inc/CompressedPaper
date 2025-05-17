package net.minecraft.server;

public class BlockFenceGate extends BlockDirectional {
	public BlockFenceGate() {
		super(Material.WOOD);
		a(CreativeModeTab.d);
	}

	public boolean canPlace(World paramWorld, int paramInt1, int paramInt2, int paramInt3) {
		if (!paramWorld.getType(paramInt1, paramInt2 - 1, paramInt3).getMaterial().isBuildable()) return false;
		return super.canPlace(paramWorld, paramInt1, paramInt2, paramInt3);
	}

	public AxisAlignedBB a(World paramWorld, int paramInt1, int paramInt2, int paramInt3) {
		int i = paramWorld.getData(paramInt1, paramInt2, paramInt3);
		if (b(i)) return null;
		if (i == 2 || i == 0) return AxisAlignedBB.a(paramInt1, paramInt2, (paramInt3 + 0.375F), (paramInt1 + 1), (paramInt2 + 1.5F), (paramInt3 + 0.625F));
		return AxisAlignedBB.a((paramInt1 + 0.375F), paramInt2, paramInt3, (paramInt1 + 0.625F), (paramInt2 + 1.5F), (paramInt3 + 1));
	}

	public void updateShape(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3) {
		int i = l(paramIBlockAccess.getData(paramInt1, paramInt2, paramInt3));
		if (i == 2 || i == 0) {
			a(0.0F, 0.0F, 0.375F, 1.0F, 1.0F, 0.625F);
		} else {
			a(0.375F, 0.0F, 0.0F, 0.625F, 1.0F, 1.0F);
		}
	}

	public boolean c() {
		return false;
	}

	public boolean d() {
		return false;
	}

	public boolean b(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3) {
		return b(paramIBlockAccess.getData(paramInt1, paramInt2, paramInt3));
	}

	public int b() {
		return 21;
	}

	public void postPlace(World paramWorld, int paramInt1, int paramInt2, int paramInt3, EntityLiving paramEntityLiving, ItemStack paramItemStack) {
		int i = (MathHelper.floor((paramEntityLiving.yaw * 4.0F / 360.0F) + 0.5D) & 0x3) % 4;
		paramWorld.setData(paramInt1, paramInt2, paramInt3, i, 2);
	}

	public boolean interact(World paramWorld, int paramInt1, int paramInt2, int paramInt3, EntityHuman paramEntityHuman, int paramInt4, float paramFloat1, float paramFloat2, float paramFloat3) {
		int i = paramWorld.getData(paramInt1, paramInt2, paramInt3);
		if (b(i)) {
			paramWorld.setData(paramInt1, paramInt2, paramInt3, i & 0xFFFFFFFB, 2);
		} else {
			int j = (MathHelper.floor((paramEntityHuman.yaw * 4.0F / 360.0F) + 0.5D) & 0x3) % 4;
			int k = l(i);
			if (k == (j + 2) % 4) i = j; 
			paramWorld.setData(paramInt1, paramInt2, paramInt3, i | 0x4, 2);
		}
		paramWorld.a(paramEntityHuman, 2001, paramInt1, paramInt2, paramInt3, 0); // 1003
		return true;
	}

	public void doPhysics(World paramWorld, int paramInt1, int paramInt2, int paramInt3, Block paramBlock) {
		if (paramWorld.isStatic) return;
		int i = paramWorld.getData(paramInt1, paramInt2, paramInt3);
		boolean bool = paramWorld.isBlockIndirectlyPowered(paramInt1, paramInt2, paramInt3);
		if (bool || paramBlock.isPowerSource()) {
			if (bool && !b(i)) {
				paramWorld.setData(paramInt1, paramInt2, paramInt3, i | 0x4, 2);
				paramWorld.a((EntityHuman) null, 1003, paramInt1, paramInt2, paramInt3, 0);
			} else if (!bool && b(i)) {
				paramWorld.setData(paramInt1, paramInt2, paramInt3, i & 0xFFFFFFFB, 2);
				paramWorld.a((EntityHuman) null, 1003, paramInt1, paramInt2, paramInt3, 0);
			}
		}
	}

	public static boolean b(int paramInt) {
		return ((paramInt & 0x4) != 0);
	}
}
