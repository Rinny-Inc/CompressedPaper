package net.minecraft.server;

import java.util.ArrayList;

import com.sathonay.interfaces.IEntitySpecificSpawnPacket;

public class EntityPainting extends EntityHanging implements IEntitySpecificSpawnPacket {
    public EnumArt art;

    public EntityPainting(World world) {
        super(world);
        this.art = EnumArt.values()[this.random.nextInt(EnumArt.values().length)]; // CraftBukkit - generate a non-null painting
    }

    public EntityPainting(World world, int i, int j, int k, int l) {
        super(world, i, j, k, l);
        final ArrayList arraylist = new ArrayList();
        final EnumArt[] aenumart = EnumArt.values();
        final int i1 = aenumart.length;

        for (int j1 = 0; j1 < i1; ++j1) {
            EnumArt enumart = aenumart[j1];

            this.art = enumart;
            this.setDirection(l);
            if (this.survives()) {
                arraylist.add(enumart);
            }
        }

        if (!arraylist.isEmpty()) {
            this.art = (EnumArt) arraylist.get(this.random.nextInt(arraylist.size()));
        }

        this.setDirection(l);
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setString("Motive", this.art.B);
        super.b(nbttagcompound);
    }

    public void a(NBTTagCompound nbttagcompound) {
    	final String s = nbttagcompound.getString("Motive");
    	final EnumArt[] aenumart = EnumArt.values();
    	final int i = aenumart.length;

        for (int j = 0; j < i; ++j) {
            EnumArt enumart = aenumart[j];

            if (enumart.B.equals(s)) {
                this.art = enumart;
            }
        }

        if (this.art == null) {
            this.art = EnumArt.KEBAB;
        }

        super.a(nbttagcompound);
    }

    public int f() {
        return this.art.C;
    }

    public int i() {
        return this.art.D;
    }

    public void b(Entity entity) {
        if (entity instanceof EntityHuman entityhuman) { // Rinny
            if (entityhuman.abilities.canInstantlyBuild) {
                return;
            }
        }

        this.a(new ItemStack(Items.PAINTING), 0.0F);
    }
    
    @Override
    public Packet createSpecificSpawnPacket() {
        return new PacketPlayOutSpawnEntityPainting(this);
    }
}
