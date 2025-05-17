package net.minecraft.server;

public class BlockJukeBox extends BlockContainer {
	protected BlockJukeBox() {
		super(Material.WOOD);
		a(CreativeModeTab.c);
	}

	public boolean interact(World paramWorld, int paramInt1, int paramInt2, int paramInt3, EntityHuman paramEntityHuman, int paramInt4, float paramFloat1, float paramFloat2, float paramFloat3) {
		if (paramWorld.getData(paramInt1, paramInt2, paramInt3) == 0) return false;
		dropRecord(paramWorld, paramInt1, paramInt2, paramInt3);
		return true;
	}

	public void b(World paramWorld, int paramInt1, int paramInt2, int paramInt3, ItemStack paramItemStack) {
		if (paramWorld.isStatic)
			return;
		TileEntityRecordPlayer tileEntityRecordPlayer = (TileEntityRecordPlayer) paramWorld.getTileEntity(paramInt1,
				paramInt2, paramInt3);
		if (tileEntityRecordPlayer == null)
			return;
		tileEntityRecordPlayer.setRecord(paramItemStack.cloneItemStack());
		paramWorld.setData(paramInt1, paramInt2, paramInt3, 1, 2);
	}

	public void dropRecord(World paramWorld, int paramInt1, int paramInt2, int paramInt3) {
		if (paramWorld.isStatic)
			return;
		TileEntityRecordPlayer tileEntityRecordPlayer = (TileEntityRecordPlayer) paramWorld.getTileEntity(paramInt1,
				paramInt2, paramInt3);
		if (tileEntityRecordPlayer == null)
			return;
		ItemStack itemStack1 = tileEntityRecordPlayer.getRecord();
		if (itemStack1 == null)
			return;
		paramWorld.triggerEffect(1005, paramInt1, paramInt2, paramInt3, 0);
		paramWorld.a((String) null, paramInt1, paramInt2, paramInt3);
		tileEntityRecordPlayer.setRecord(null);
		paramWorld.setData(paramInt1, paramInt2, paramInt3, 0, 2);
		float f = 0.7F;
		double d1 = (paramWorld.random.nextFloat() * f) + (1.0F - f) * 0.5D;
		double d2 = (paramWorld.random.nextFloat() * f) + (1.0F - f) * 0.2D + 0.6D;
		double d3 = (paramWorld.random.nextFloat() * f) + (1.0F - f) * 0.5D;
		ItemStack itemStack2 = itemStack1.cloneItemStack();
		EntityItem entityItem = new EntityItem(paramWorld, paramInt1 + d1, paramInt2 + d2, paramInt3 + d3, itemStack2);
		entityItem.pickupDelay = 10;
		paramWorld.addEntity(entityItem);
	}

	public void remove(World paramWorld, int paramInt1, int paramInt2, int paramInt3, Block paramBlock, int paramInt4) {
		dropRecord(paramWorld, paramInt1, paramInt2, paramInt3);
		super.remove(paramWorld, paramInt1, paramInt2, paramInt3, paramBlock, paramInt4);
	}

	public void dropNaturally(World paramWorld, int paramInt1, int paramInt2, int paramInt3, int paramInt4, float paramFloat, int paramInt5) {
		if (paramWorld.isStatic) return;
		super.dropNaturally(paramWorld, paramInt1, paramInt2, paramInt3, paramInt4, paramFloat, 0);
	}

	public TileEntity a(World paramWorld, int paramInt) {
		return new TileEntityRecordPlayer();
	}

	public boolean isComplexRedstone() {
		return true;
	}

	public int g(World paramWorld, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
		ItemStack itemStack = ((TileEntityRecordPlayer) paramWorld.getTileEntity(paramInt1, paramInt2, paramInt3)).getRecord();
		return (itemStack == null) ? 0 : (Item.getId(itemStack.getItem()) + 1 - Item.getId(Items.RECORD_1));
	}
}
