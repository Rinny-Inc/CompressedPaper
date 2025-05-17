package net.minecraft.server;

import net.minecraft.util.com.google.common.collect.Multimap;

public class ItemSword extends Item {
    private float damage;
    private final EnumToolMaterial b;
  
    public ItemSword(EnumToolMaterial paramEnumToolMaterial) {
        this.b = paramEnumToolMaterial;
        this.maxStackSize = 1;
        setMaxDurability(paramEnumToolMaterial.a());
        a(CreativeModeTab.j);
        this.damage = 4.0F + paramEnumToolMaterial.c();
    }
  
    public float i() {
        return this.b.c();
    }
  
    public float getDestroySpeed(ItemStack paramItemStack, Block paramBlock) {
        if (paramBlock == Blocks.WEB) {
            return 15.0F; 
        }
        Material material = paramBlock.getMaterial();
        if (material == Material.PLANT || material == Material.REPLACEABLE_PLANT || material == Material.CORAL || material == Material.LEAVES || material == Material.PUMPKIN) {
            return 1.5F; 
        }
        return 1.0F;
    }
  
    public boolean a(ItemStack paramItemStack, EntityLiving paramEntityLiving1, EntityLiving paramEntityLiving2) {
        paramItemStack.damage(1, paramEntityLiving2);
        return true;
    }
  
    public boolean a(ItemStack paramItemStack, World paramWorld, Block paramBlock, int paramInt1, int paramInt2, int paramInt3, EntityLiving paramEntityLiving) {
        if (paramBlock.f(paramWorld, paramInt1, paramInt2, paramInt3) != 0.0D) {
            paramItemStack.damage(2, paramEntityLiving); 
        }
        return true;
    }
  
    public EnumAnimation d(ItemStack paramItemStack) {
        return EnumAnimation.BLOCK;
    }
  
    public int d_(ItemStack paramItemStack) {
        return 72000;
    }
  
    public ItemStack a(ItemStack paramItemStack, World paramWorld, EntityHuman paramEntityHuman) {
        paramEntityHuman.a(paramItemStack, d_(paramItemStack));
        return paramItemStack;
    }
  
    public boolean canDestroySpecialBlock(Block paramBlock) {
        return (paramBlock == Blocks.WEB);
    }
  
    public int c() {
        return this.b.e();
    }
  
    public String j() {
        return this.b.toString();
    }
  
    public boolean a(ItemStack paramItemStack1, ItemStack paramItemStack2) {
        if (this.b.f() == paramItemStack2.getItem()) {
            return true; 
        }
        return super.a(paramItemStack1, paramItemStack2);
    }
  
    public Multimap k() {
        Multimap multimap = super.k();
        multimap.put(GenericAttributes.e.getName(), new AttributeModifier(f, "Weapon modifier", this.damage, 0));
        return multimap;
    }
}