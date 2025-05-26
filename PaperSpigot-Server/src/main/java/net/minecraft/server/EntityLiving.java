package net.minecraft.server;

// CraftBukkit start
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityRegainHealthEvent;
// CraftBukkit end

import com.google.common.base.Function;

import io.noks.interfaces.ICollect;

public abstract class EntityLiving extends Entity {

    private static final UUID b = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final AttributeModifier c = (new AttributeModifier(b, "Sprinting speed boost", 0.30000001192092896D, 2)).a(false);
    private AttributeMapBase d; public AttributeMapBase attributeMapBase() { return this.d; }
    public CombatTracker combatTracker = new CombatTracker(this); // CraftBukkit - private -> public, remove final
    public final Map<Integer, MobEffect> effects = new ConcurrentHashMap<Integer, MobEffect>(); // CraftBukkit - protected -> public
    private final ItemStack[] g = new ItemStack[5];
    public boolean at;
    public int au;
    public int av;
    public float aw;
    public int hurtTicks;
    public int ay;
    public float az;
    public int deathTicks;
    public int attackTicks;
    public float aC;
    public float aD;
    public float aE;
    public float aF;
    public float aG;
    public int maxNoDamageTicks = 20;
    public float aI;
    public float aJ;
    public float aK;
    public float aL;
    public float aM;
    public float aN;
    public float aO;
    public float aP;
    public float aQ = 0.02F;
    public EntityHuman killer; // CraftBukkit - protected -> public
    protected int lastDamageByPlayerTime;
    protected boolean aT;
    protected int aU;
    protected float aV;
    protected float aW;
    protected float aX;
    protected float aY;
    protected float aZ;
    protected int ba;
    public float lastDamage; // CraftBukkit - protected -> public
    protected boolean bc;
    public float bd;
    public float be;
    protected float bf;
    protected int bg;
    protected double bh;
    protected double bi;
    protected double bj;
    protected double bk;
    protected double bl;
    public boolean updateEffects = true; // CraftBukkit - private -> public
    public EntityLiving lastDamager; // CraftBukkit - private -> public
    private int bm;
    private EntityLiving bn;
    private int bo;
    private float bp;
    private int bq;
    private float br;
    // CraftBukkit start
    public int expToDrop;
    public int maxAirTicks = 300;
    public List<org.bukkit.inventory.ItemStack> drops = null;
    // CraftBukkit end
    private boolean extraKnockback;
    // Spigot start
    public void inactiveTick()
    {
        super.inactiveTick();
        ++this.aU; // Above all the floats
    }
    // Spigot end

    public EntityLiving(World world) {
        super(world);
        this.aD();
        // CraftBukkit - setHealth(getMaxHealth()) inlined and simplified to skip the instanceof check for EntityPlayer, as getBukkitEntity() is not initialized in constructor
        this.datawatcher.watch(6, (float) this.getAttributeInstance(GenericAttributes.maxHealth).getValue());
        this.k = true;
        this.aL = (float) (Math.random() + 1.0D) * 0.01F;
        this.setPosition(this.locX, this.locY, this.locZ);
        this.aK = (float) Math.random() * 12398.0F;
        this.yaw = (float) (Math.random() * 3.1415927410125732D * 2.0D);
        this.aO = this.yaw;
        this.W = 0.5F;
    }

    protected void c() {
        this.datawatcher.a(7, Integer.valueOf(0));
        this.datawatcher.a(8, Byte.valueOf((byte) 0));
        this.datawatcher.a(9, Byte.valueOf((byte) 0));
        this.datawatcher.a(6, Float.valueOf(1.0F));
    }

    protected void aD() {
        this.getAttributeMap().b(GenericAttributes.maxHealth);
        this.getAttributeMap().b(GenericAttributes.c);
        this.getAttributeMap().b(GenericAttributes.d);
        if (!this.bk()) {
            this.getAttributeInstance(GenericAttributes.d).setValue(0.10000000149011612D);
        }
    }

    protected void a(double d0, boolean flag) {
        if (!this.M()) {
            this.N();
        }

        if (flag && this.fallDistance > 0.0F) {
            int i = MathHelper.floor(this.locX);
            int j = MathHelper.floor(this.locY - 0.20000000298023224D - (double) this.height);
            int k = MathHelper.floor(this.locZ);
            Block block = this.world.getType(i, j, k);

            if (block.getMaterial() == Material.AIR) {
                int l = this.world.getType(i, j - 1, k).b();

                if (l == 11 || l == 32 || l == 21) {
                    block = this.world.getType(i, j - 1, k);
                }
            } else if (!this.world.isStatic && this.fallDistance > 3.0F) {
                // CraftBukkit start - supply player as argument in particles for visibility API to work
                /*if (this instanceof EntityPlayer) {
                    this.world.a((EntityHuman) this, 2006, i, j, k, MathHelper.f(this.fallDistance - 3.0F));
                    ((EntityPlayer) this).playerConnection.sendPacket(new PacketPlayOutWorldEvent(2006, i, j, k, MathHelper.f(this.fallDistance - 3.0F), false));
                } else {
                    this.world.triggerEffect(2006, i, j, k, MathHelper.f(this.fallDistance - 3.0F));
                }*/
            	((WorldServer)this.world).getTracker().broadcastFallDamageParticles(this, block, this.world.getData(i, j, k));
                // CraftBukkit end
            }

            block.a(this.world, i, j, k, this, this.fallDistance);
        }

        super.a(d0, flag);
    }

    public boolean aE() {
        return false;
    }

    public void C() {
        this.aC = this.aD;
        super.C();
        if (this.isAlive() && this.inBlock()) {
            this.damageEntity(DamageSource.STUCK, 1.0F);
        }

        if (this.isFireproof() || this.world.isStatic) {
            this.extinguish();
        }

        boolean flag = this instanceof EntityHuman && ((EntityHuman) this).abilities.isInvulnerable;

        if (this.isAlive() && this.a(Material.WATER)) {
            if (!this.aE() && !this.hasEffect(MobEffectList.WATER_BREATHING.id) && !flag) {
                this.setAirTicks(this.j(this.getAirTicks()));
                if (this.getAirTicks() == -20) {
                    this.setAirTicks(0);

                    for (int i = 0; i < 8; ++i) {
                        float f = this.random.nextFloat() - this.random.nextFloat();
                        float f1 = this.random.nextFloat() - this.random.nextFloat();
                        float f2 = this.random.nextFloat() - this.random.nextFloat();

                        this.world.addParticle("bubble", this.locX + (double) f, this.locY + (double) f1, this.locZ + (double) f2, this.motX, this.motY, this.motZ);
                    }

                    this.damageEntity(DamageSource.DROWN, 2.0F);
                }
            }

            if (!this.world.isStatic && this.am() && this.vehicle instanceof EntityLiving) {
                this.mount((Entity) null);
            }
        } else {
            // CraftBukkit start - Only set if needed to work around a DataWatcher inefficiency
            if (this.getAirTicks() != 300) {
                this.setAirTicks(maxAirTicks);
            }
            // CraftBukkit end
        }

        if (this.isAlive() && this.L()) {
            this.extinguish();
        }

        this.aI = this.aJ;
        if (this.attackTicks > 0) {
            --this.attackTicks;
        }

        if (this.hurtTicks > 0) {
            --this.hurtTicks;
        }

        // Rinny - Handle EntityPlayer instead of doing 2 checks
        /*if (this.noDamageTicks > 0 && !(this instanceof EntityPlayer)) {
            --this.noDamageTicks;
        }*/
        if (this.noDamageTicks > 0) {
            --this.noDamageTicks;
        }
        // Rinny

        if (this.getHealth() <= 0.0F) {
            this.aF();
        }

        if (this.lastDamageByPlayerTime > 0) {
            --this.lastDamageByPlayerTime;
        } else {
            this.killer = null;
        }

        if (this.bn != null && !this.bn.isAlive()) {
            this.bn = null;
        }

        if (this.lastDamager != null) {
            if (!this.lastDamager.isAlive() || this.ticksLived - this.bm > 100) {
                this.b((EntityLiving) null);
            }
        }

        this.aO();
        this.aY = this.aX;
        this.aN = this.aM;
        this.aP = this.aO;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
    }

    // CraftBukkit start
    public int getExpReward() {
        int exp = this.getExpValue(this.killer);

        if (!this.world.isStatic && (this.lastDamageByPlayerTime > 0 || this.alwaysGivesExp()) && this.aG() && this.world.getGameRules().getBoolean("doMobLoot")) {
            return exp;
        }
        return 0;
    }
    // CraftBukkit end

    public boolean isBaby() {
        return false;
    }

    protected void aF() {
        ++this.deathTicks;
        if (this.deathTicks >= 20 && !this.dead) { // CraftBukkit - (this.deathTicks == 20) -> (this.deathTicks >= 20 && !this.dead)
            int i;

            // CraftBukkit start - Update getExpReward() above if the removed if() changes!
            i = this.expToDrop;
            while (i > 0) {
                final int j = EntityExperienceOrb.getOrbValue(i);

                i -= j;
                this.world.addEntity(new EntityExperienceOrb(this.world, this.locX, this.locY, this.locZ, j));
            }
            this.expToDrop = 0;
            // CraftBukkit end
            this.die();
            for (i = 0; i < 20; ++i) {
            	final double d0 = this.random.nextGaussian() * 0.02D;
            	final double d1 = this.random.nextGaussian() * 0.02D;
            	final double d2 = this.random.nextGaussian() * 0.02D;

                this.world.addParticle("explode", this.locX + (double) (this.random.nextFloat() * this.width * 2.0F) - (double) this.width, this.locY + (double) (this.random.nextFloat() * this.length), this.locZ + (double) (this.random.nextFloat() * this.width * 2.0F) - (double) this.width, d0, d1, d2);
            }
        }
    }

    protected boolean aG() {
        return !this.isBaby();
    }

    protected int j(int i) {
    	final int j = EnchantmentManager.getOxygenEnchantmentLevel(this);

        return j > 0 && this.random.nextInt(j + 1) > 0 ? i : i - 1;
    }

    protected int getExpValue(EntityHuman entityhuman) {
        return 0;
    }

    protected boolean alwaysGivesExp() {
        return false;
    }

    public Random aI() {
        return this.random;
    }

    public EntityLiving getLastDamager() {
        return this.lastDamager;
    }

    public int aK() {
        return this.bm;
    }

    public void b(EntityLiving entityliving) {
        this.lastDamager = entityliving;
        this.bm = this.ticksLived;
    }

    public EntityLiving aL() {
        return this.bn;
    }

    public int aM() {
        return this.bo;
    }

    public void l(Entity entity) {
        this.bn = (entity instanceof EntityLiving ? (EntityLiving) entity : null);
        this.bo = this.ticksLived;
    }

    public int aN() {
        return this.aU;
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setFloat("HealF", this.getHealth());
        nbttagcompound.setShort("Health", (short) ((int) Math.ceil((double) this.getHealth())));
        nbttagcompound.setShort("HurtTime", (short) this.hurtTicks);
        nbttagcompound.setShort("DeathTime", (short) this.deathTicks);
        nbttagcompound.setShort("AttackTime", (short) this.attackTicks);
        nbttagcompound.setFloat("AbsorptionAmount", this.getAbsorptionHearts());
        ItemStack[] aitemstack = this.getEquipment();
        int i = aitemstack.length;

        int j;
        ItemStack itemstack;

        for (j = 0; j < i; ++j) {
            itemstack = aitemstack[j];
            if (itemstack != null) {
                this.d.a(itemstack.D());
            }
        }

        nbttagcompound.set("Attributes", GenericAttributes.a(this.getAttributeMap()));
        aitemstack = this.getEquipment();
        i = aitemstack.length;

        for (j = 0; j < i; ++j) {
            itemstack = aitemstack[j];
            if (itemstack != null) {
                this.d.b(itemstack.D());
            }
        }

        if (!this.effects.isEmpty()) {
        	final NBTTagList nbttaglist = new NBTTagList();
        	final Iterator<MobEffect> iterator = this.effects.values().iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = iterator.next();

                nbttaglist.add(mobeffect.a(new NBTTagCompound()));
            }

            nbttagcompound.set("ActiveEffects", nbttaglist);
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.setAbsorptionHearts(nbttagcompound.getFloat("AbsorptionAmount"));
        if (nbttagcompound.hasKeyOfType("Attributes", 9) && this.world != null && !this.world.isStatic) {
            GenericAttributes.a(this.getAttributeMap(), nbttagcompound.getList("Attributes", 10));
        }

        if (nbttagcompound.hasKeyOfType("ActiveEffects", 9)) {
        	final NBTTagList nbttaglist = nbttagcompound.getList("ActiveEffects", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.get(i);
                MobEffect mobeffect = MobEffect.b(nbttagcompound1);

                if (mobeffect != null) {
                    this.effects.put(Integer.valueOf(mobeffect.getEffectId()), mobeffect);
                }
            }
        }

        // CraftBukkit start
        if (nbttagcompound.hasKey("Bukkit.MaxHealth")) {
        	final NBTBase nbtbase = nbttagcompound.get("Bukkit.MaxHealth");
            if (nbtbase.getTypeId() == 5) {
                this.getAttributeInstance(GenericAttributes.maxHealth).setValue((double) ((NBTTagFloat) nbtbase).c());
            } else if (nbtbase.getTypeId() == 3) {
                this.getAttributeInstance(GenericAttributes.maxHealth).setValue((double) ((NBTTagInt) nbtbase).d());
            }
        }
        // CraftBukkit end

        if (nbttagcompound.hasKeyOfType("HealF", 99)) {
            this.setHealth(nbttagcompound.getFloat("HealF"));
        } else {
        	final NBTBase nbtbase = nbttagcompound.get("Health");

            if (nbtbase == null) {
                this.setHealth(this.getMaxHealth());
            } else if (nbtbase.getTypeId() == 5) {
                this.setHealth(((NBTTagFloat) nbtbase).h());
            } else if (nbtbase.getTypeId() == 2) {
                this.setHealth((float) ((NBTTagShort) nbtbase).e());
            }
        }

        this.hurtTicks = nbttagcompound.getShort("HurtTime");
        this.deathTicks = nbttagcompound.getShort("DeathTime");
        this.attackTicks = nbttagcompound.getShort("AttackTime");
    }

    protected void aO() {
    	final Iterator<Integer> iterator = this.effects.keySet().iterator();

        while (iterator.hasNext()) {
            Integer integer = iterator.next();
            MobEffect mobeffect = this.effects.get(integer);

            if (!mobeffect.tick(this)) {
                if (!this.world.isStatic) {
                    iterator.remove();
                    this.b(mobeffect);
                }
            } else if (mobeffect.getDuration() % 600 == 0) {
                this.a(mobeffect, false);
            }
        }

        int i;

        if (this.updateEffects) {
            if (!this.world.isStatic) {
                if (this.effects.isEmpty()) {
                    this.datawatcher.watch(8, (byte) 0);
                    this.datawatcher.watch(7, 0);
                    this.setInvisible(false);
                } else {
                    i = PotionBrewer.a(this.effects.values());
                    this.datawatcher.watch(8, (byte) (PotionBrewer.b(this.effects.values()) ? 1 : 0));
                    this.datawatcher.watch(7, i);
                    this.setInvisible(this.hasEffect(MobEffectList.INVISIBILITY.id));
                }
            }

            this.updateEffects = false;
        }

        i = this.datawatcher.getInt(7);
        final boolean flag = this.datawatcher.getByte(8) > 0;

        if (i > 0) {
            boolean flag1 = (!this.isInvisible() ? this.random.nextBoolean() : this.random.nextInt(15) == 0);

            if (flag) {
                flag1 &= this.random.nextInt(5) == 0;
            }

            if (flag1 && i > 0) {
            	final double d0 = (double) (i >> 16 & 255) / 255.0D;
            	final double d1 = (double) (i >> 8 & 255) / 255.0D;
            	final double d2 = (double) (i >> 0 & 255) / 255.0D;

                this.world.addParticle(flag ? "mobSpellAmbient" : "mobSpell", this.locX + (this.random.nextDouble() - 0.5D) * (double) this.width, this.locY + this.random.nextDouble() * (double) this.length - (double) this.height, this.locZ + (this.random.nextDouble() - 0.5D) * (double) this.width, d0, d1, d2);
            }
        }
    }
    
    // Rinny - moved from Entity
    @Override
    protected void E() {
    	if (!this.fireProof) {
            this.damageEntity(DamageSource.LAVA, 4);

            // CraftBukkit start - Fallen in lava TODO: this event spams!
            if (this.fireTicks <= 0) {
                // not on fire yet
                // TODO: shouldn't be sending null for the block.
                final org.bukkit.block.Block damager = null; // ((WorldServer) this.l).getWorld().getBlockAt(i, j, k);
                final org.bukkit.entity.Entity damagee = this.getBukkitEntity();
                final EntityCombustEvent combustEvent = new org.bukkit.event.entity.EntityCombustByBlockEvent(damager, damagee, 15);
                this.world.getServer().getPluginManager().callEvent(combustEvent);

                if (!combustEvent.isCancelled()) {
                    this.setOnFire(combustEvent.getDuration());
                    // now on fire
                }
            } else {
                // This will be called every single tick the entity is in lava, so don't throw an event
                this.setOnFire(15);
            }
        }
    }

    public void removeAllEffects() {
    	final Iterator<Integer> iterator = this.effects.keySet().iterator();

        while (iterator.hasNext()) {
            Integer integer = iterator.next();
            MobEffect mobeffect = this.effects.get(integer);

            if (!this.world.isStatic) {
                iterator.remove();
                this.b(mobeffect);
            }
        }
    }

    public Collection<MobEffect> getEffects() {
        return this.effects.values();
    }

    public boolean hasEffect(int i) {
        // CraftBukkit - Add size check for efficiency
        return !this.effects.isEmpty() && this.effects.containsKey(i);
    }

    public boolean hasEffect(MobEffectList mobeffectlist) {
        // CraftBukkit - Add size check for efficiency
        return !this.effects.isEmpty() && this.effects.containsKey((int)mobeffectlist.id);
    }

    public MobEffect getEffect(MobEffectList mobeffectlist) {
        return this.effects.get((int)mobeffectlist.id);
    }

    public void addEffect(MobEffect mobeffect) {
        if (!this.d(mobeffect)) {
        	return;
        }
        /*if (this.effects.containsKey(Integer.valueOf(mobeffect.getEffectId()))) {
        	((MobEffect) this.effects.get(Integer.valueOf(mobeffect.getEffectId()))).a(mobeffect);
        	this.a((MobEffect) this.effects.get(Integer.valueOf(mobeffect.getEffectId())), true);
    	} else {
        	this.effects.put(Integer.valueOf(mobeffect.getEffectId()), mobeffect);
        	this.a(mobeffect);
    	}*/
        // Rinny
        int id = mobeffect.getEffectId();
        MobEffect existing = this.effects.get(id);

        if (existing != null) {
            existing.a(mobeffect);
            this.a(existing, true);
            return;
        }
        this.effects.put(id, mobeffect);
        this.a(mobeffect);
        // Rinny
    }

    public boolean d(MobEffect mobeffect) {
        if (this.getMonsterType() == EnumMonsterType.UNDEAD) {
        	final int i = mobeffect.getEffectId();

            if (i == MobEffectList.REGENERATION.id || i == MobEffectList.POISON.id) {
                return false;
            }
        }

        return true;
    }

    public boolean aR() {
        return this.getMonsterType() == EnumMonsterType.UNDEAD;
    }

    public void removeEffect(int i) {
    	final MobEffect mobeffect = (MobEffect) this.effects.remove(Integer.valueOf(i));

        if (mobeffect != null) {
            this.b(mobeffect);
        }
    }

    protected void a(MobEffect mobeffect) {
        this.updateEffects = true;
        if (!this.world.isStatic) {
            MobEffectList.byId[mobeffect.getEffectId()].b(this, this.getAttributeMap(), mobeffect.getAmplifier());
        }
    }

    protected void a(MobEffect mobeffect, boolean flag) {
        this.updateEffects = true;
        if (flag && !this.world.isStatic) {
            MobEffectList.byId[mobeffect.getEffectId()].a(this, this.getAttributeMap(), mobeffect.getAmplifier());
            MobEffectList.byId[mobeffect.getEffectId()].b(this, this.getAttributeMap(), mobeffect.getAmplifier());
        }
    }

    protected void b(MobEffect mobeffect) {
        this.updateEffects = true;
        if (!this.world.isStatic) {
            MobEffectList.byId[mobeffect.getEffectId()].a(this, this.getAttributeMap(), mobeffect.getAmplifier());
        }
    }

    // CraftBukkit start - Delegate so we can handle providing a reason for health being regained
    public void heal(float f) {
        heal(f, EntityRegainHealthEvent.RegainReason.CUSTOM);
    }

    public void heal(float f, EntityRegainHealthEvent.RegainReason regainReason) {
    	final float f1 = this.getHealth();

        if (f1 > 0.0F) {
        	final EntityRegainHealthEvent event = new EntityRegainHealthEvent(this.getBukkitEntity(), f, regainReason);
            this.world.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                this.setHealth((float) (this.getHealth() + event.getAmount()));
            }
        }
    }

    public final float getHealth() {
        // CraftBukkit start - Use unscaled health
        if (this instanceof EntityPlayer) {
            return (float) ((EntityPlayer) this).getBukkitEntity().getHealth();
        }
        // CraftBukkit end
        return this.datawatcher.getFloat(6);
    }

    public void setHealth(float f) {
        // CraftBukkit start - Handle scaled health
        if (this instanceof EntityPlayer) {
        	final org.bukkit.craftbukkit.entity.CraftPlayer player = ((EntityPlayer) this).getBukkitEntity();
        	final float health = (float) (f < 0.0F ? 0.0D : (f > player.getMaxHealth() ? player.getMaxHealth() : f));
            // Squeeze
            player.setRealHealth(health);
            this.datawatcher.watch(6, Float.valueOf(player.getScaledHealth()));
            return;
        }
        // CraftBukkit end
        this.datawatcher.watch(6, Float.valueOf(MathHelper.a(f, 0.0F, this.getMaxHealth())));
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable()) {
            return false;
        } else if (this.world.isStatic) {
            return false;
        } else {
            this.aU = 0;
            if (this.getHealth() <= 0.0F) {
                return false;
            } else if (damagesource.o() && this.hasEffect(MobEffectList.FIRE_RESISTANCE)) {
                return false;
            } else {
                // CraftBukkit - Moved into d(DamageSource, float)
                /*if (false && (damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK) && this.getEquipment(4) != null) {
                    this.getEquipment(4).damage((int) (f * 4.0F + this.random.nextFloat() * f * 2.0F), this);
                    f *= 0.75F;
                }*/

                this.aF = 1.5F;
                boolean flag = true;

	            if (this.noDamageTicks > (this.maxNoDamageTicks / 2.0F)) {
	                if (f <= this.lastDamage) {
	                    return false;
	                }
	
	                // CraftBukkit start
	                if (!this.d(damagesource, f - this.lastDamage)) {
	                    return false;
	                }
	                // CraftBukkit end
	                this.lastDamage = f;
	                flag = false;
	            } else {
	                // CraftBukkit start
	            	final float previousHealth = this.getHealth();
	                if (!this.d(damagesource, f)) {
	                    return false;
	                }
	                this.lastDamage = f;
	                this.aw = previousHealth;
	                this.noDamageTicks = this.maxNoDamageTicks;
	                    
	                 // CraftBukkit end
	                this.hurtTicks = this.ay = 10;
	            }

                this.az = 0.0F;
                final Entity entity = damagesource.getEntity();

                if (entity != null) {
                    if (entity instanceof EntityLiving) {
                        this.b((EntityLiving) entity);
                    }

                    if (entity instanceof EntityHuman) {
                        this.lastDamageByPlayerTime = 100;
                        this.killer = (EntityHuman) entity;
                    } else if (entity instanceof EntityWolf entitywolf) { // Rinny
                        if (entitywolf.isTamed()) {
                            this.lastDamageByPlayerTime = 100;
                            this.killer = null;
                        }
                    }
                }

                // PaperSpigot start - Disable explosion knockback
                boolean knockbackCancelled = false;
                if (flag && !(knockbackCancelled = world.paperSpigotConfig.disableExplosionKnockback && damagesource.isExplosion() && this instanceof EntityHuman)) {
                // PaperSpigot end
                    this.world.broadcastEntityEffect(this, (byte) 2);
                    if (damagesource != DamageSource.DROWN) {
                        this.Q();
                    }

                    if (entity != null) {
                        double d0 = entity.locX - this.locX;
                        double d1 = entity.locZ - this.locZ;
                        
	                    while (d0 * d0 + d1 * d1 < 1.0E-4D) {
	                        d0 = (Math.random() - Math.random()) * 0.01D;
	                        d1 = (Math.random() - Math.random()) * 0.01D;
	                    }
	
	                    this.az = (float) (Math.atan2(d1, d0) * 180.0D / 3.1415927410125732D) - this.yaw;
	                    this.a(d0, d1);
                    } else {
                        this.az = (float) ((int) (Math.random() * 2.0D) * 180);
                    }
                }

                if (knockbackCancelled) this.world.broadcastEntityEffect(this, (byte) 2); // PaperSpigot - Disable explosion knockback

                // Rinny
                String s = this.getHealth() <= 0.0F ? this.aU() : this.aT();
                
                if (flag && s != null) {
                    this.makeSound(s, this.bf(), this.bg());
                }
                
                if (s == this.aU()) {
                	this.die(damagesource);
                }
                // Rinny
                
                /*if (this.getHealth() <= 0.0F) {
                    s = this.aU();
                    if (flag && s != null) {
                        this.makeSound(s, this.bf(), this.bg());
                    }

                    this.die(damagesource);
                } else {
                    s = this.aT();
                    if (flag && s != null) {
                        this.makeSound(s, this.bf(), this.bg());
                    } 
                }*/

                return true;
            }
        }
    }

    public void a(ItemStack itemstack) {
        this.makeSound("random.break", 0.8F, 0.8F + this.world.random.nextFloat() * 0.4F);
        
        final float pitchRad = -this.pitch * (float)Math.PI / 180.0F; // Rinny - precompute
        final float yawRad = -this.yaw * (float)Math.PI / 180.0F; // Rinny - precompute
        for (int i = 0; i < 5; ++i) {
        	final Vec3D vec3d = Vec3D.a(((double) this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
        	
            vec3d.a(pitchRad);
            vec3d.b(yawRad);
            Vec3D vec3d1 = Vec3D.a(((double) this.random.nextFloat() - 0.5D) * 0.3D, (double) (-this.random.nextFloat()) * 0.6D - 0.3D, 0.6D);

            vec3d1.a(pitchRad);
            vec3d1.b(yawRad);
            vec3d1 = vec3d1.add(this.locX, this.locY + (double) this.getHeadHeight(), this.locZ);
            this.world.addParticle("iconcrack_" + Item.getId(itemstack.getItem()), vec3d1.a, vec3d1.b, vec3d1.c, vec3d.a, vec3d.b + 0.05D, vec3d.c);
        }
    }

    public void die(DamageSource damagesource) { // TODO: shortcut entity dying
    	if (this.dead) return; // Rinny - Don't kill the player twice
    	final Entity entity = damagesource.getEntity();
    	final EntityLiving entityliving = this.aX();

        if (this.ba >= 0 && entityliving != null) {
            entityliving.b(this, this.ba);
        }

        if (entity != null) {
            entity.a(this);
        }
        this.aT = true;
    	this.aW().g();
        if (!this.world.isStatic) {
            int i = 0;

            if (entity instanceof EntityHuman) {
                i = EnchantmentManager.getBonusMonsterLootEnchantmentLevel((EntityLiving) entity);
            }

            if (this.aG() && this.world.getGameRules().getBoolean("doMobLoot")) {
                this.drops = new ArrayList<org.bukkit.inventory.ItemStack>(); // CraftBukkit - Setup drop capture
                final boolean b = this.lastDamageByPlayerTime > 0; // Rinny - store it instead of checking it 3 times 
                
                this.dropDeathLoot(b, i);
                this.dropEquipment(b, i);
                if (b) {
                    int j = this.random.nextInt(200) - i;

                    if (j < 5) {
                        this.getRareDrop(j <= 0 ? 1 : 0);
                    }
                }

                // CraftBukkit start - Call death event
                CraftEventFactory.callEntityDeathEvent(this, this.drops);
                this.drops = null;
            } else {
                CraftEventFactory.callEntityDeathEvent(this);
                // CraftBukkit end
            }
        }

        this.world.broadcastEntityEffect(this, (byte) 3);
    }

    protected void dropEquipment(boolean flag, int i) {}

    public void a(final double d0, final double d1) { //TODO: shortcut
        if (this.random.nextDouble() < this.getAttributeInstance(GenericAttributes.c).getValue()) {
        	return;
        }
        this.al = true;
            
        final double magnitude = Math.hypot(d0, d1); // Rinny
        //final double magnitude = Math.sqrt(Math.pow(d0, 2) + Math.pow(d1, 2));
        final double verticalKnockbackReduction = getBukkitEntity().getVerticalKnockbackReduction();
        final double horizontalKnockbackReduction = getBukkitEntity().getHorizontalKnockbackReduction();
        final double force = (world.paperSpigotConfig.knockbackHorizontal * (1.0D - horizontalKnockbackReduction));
        final double height = (world.paperSpigotConfig.knockbackHeight * (1.0D - verticalKnockbackReduction));
        final double friction = world.paperSpigotConfig.knockbackFriction;
            
        this.motX /= friction;
        this.motY /= friction;
        this.motZ /= friction;
            
        this.motX -= d0 / magnitude * force;
        this.motY = MathHelper.limit(this.motY + height, 0.05D, world.paperSpigotConfig.knockbackVerticalLimit); //4000000059604645
        this.motZ -= d1 / magnitude * force;
    }

    protected String aT() {
        return "game.neutral.hurt";
    }

    protected String aU() {
        return "game.neutral.die";
    }

    protected void getRareDrop(int i) {}

    protected void dropDeathLoot(boolean flag, int i) {}

    public boolean h_() {
    	final int i = MathHelper.floor(this.locX);
    	final int j = MathHelper.floor(this.boundingBox.b);
    	final int k = MathHelper.floor(this.locZ);
    	final Block block = this.world.getType(i, j, k);

        return block == Blocks.LADDER || block == Blocks.VINE;
    }

    public boolean isAlive() {
        return !this.dead && this.getHealth() > 0.0F;
    }

    protected void b(float f) {
        super.b(f);
        final MobEffect mobeffect = this.getEffect(MobEffectList.JUMP);
        final float f1 = mobeffect != null ? (float) (mobeffect.getAmplifier() + 1) : 0.0F;
        final int i = MathHelper.f(f - 3.0F - f1);

        if (i > 0) {
            // CraftBukkit start
            if (!this.damageEntity(DamageSource.FALL, (float) i)) {
                return;
            }
            // CraftBukkit end
            this.makeSound(this.o(i), 1.0F, 1.0F);
            // this.damageEntity(DamageSource.FALL, (float) i); // CraftBukkit - moved up
            final int j = MathHelper.floor(this.locX);
            final int k = MathHelper.floor(this.locY - 0.20000000298023224D - (double) this.height);
            final int l = MathHelper.floor(this.locZ);
            final Block block = this.world.getType(j, k, l);

            if (block.getMaterial() != Material.AIR) {
            	final StepSound stepsound = block.stepSound;

                this.makeSound(stepsound.getStepSound(), stepsound.getVolume1() * 0.5F, stepsound.getVolume2() * 0.75F);
            }
        }
    }

    protected String o(int i) {
        return i > 4 ? "game.neutral.hurt.fall.big" : "game.neutral.hurt.fall.small";
    }

    public int aV() {
        int i = 0;
        final ItemStack[] aitemstack = this.getEquipment();
        final int j = aitemstack.length;

        for (int k = 0; k < j; ++k) {
            ItemStack itemstack = aitemstack[k];

            if (itemstack != null && itemstack.getItem() instanceof ItemArmor) {
                int l = ((ItemArmor) itemstack.getItem()).c;

                i += l;
            }
        }

        return i;
    }

    protected void damageArmor(float f) {}

    protected float applyArmorModifier(DamageSource damagesource, float f) {
        if (!damagesource.ignoresArmor()) {
        	final int i = 25 - this.aV();
        	final float f1 = f * (float) i;

            // this.damageArmor(f); // CraftBukkit - Moved into d(DamageSource, float)
            f = f1 / 25.0F;
        }

        return f;
    }

    protected float applyMagicModifier(DamageSource damagesource, float f) {
        if (damagesource.isStarvation()) {
            return f;
        }
        /*if (this instanceof EntityZombie) {
            f = f;
        }*/

        /*int i;
        int j;
        float f1;
        // CraftBukkit - Moved to d(DamageSource, float)
        if (false && this.hasEffect(MobEffectList.RESISTANCE) && damagesource != DamageSource.OUT_OF_WORLD) {
            i = (this.getEffect(MobEffectList.RESISTANCE).getAmplifier() + 1) * 5;
            j = 25 - i;
            f1 = f * (float) j;
            f = f1 / 25.0F;
        }*/

        if (f <= 0.0F) {
            return 0.0F;
        }
        int i = EnchantmentManager.a(this.getEquipment(), damagesource);
        if (i > 20) {
            i = 20;
        }
        if (i > 0 && i <= 20) {
        	int j = 25 - i;
        	float f1 = f * (float) j;
            f = f1 / 25.0F;
        }
        return f;
    }

    // CraftBukkit start
    /*protected boolean d(final DamageSource damagesource, float f) { // void -> boolean, add final
        if (!this.isInvulnerable()) {
            final boolean human = this instanceof EntityHuman;
            final float originalDamage = f;
            final Function<Double, Double> hardHat = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    if ((damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK) && EntityLiving.this.getEquipment(4) != null) {
                        return -(f - (f * 0.75F));
                    }
                    return -0.0;
                }
            };
            final float hardHatModifier = hardHat.apply((double) f).floatValue();
            f += hardHatModifier;

            final Function<Double, Double> blocking = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    if (human) {
                        if (!damagesource.ignoresArmor() && ((EntityHuman) EntityLiving.this).isBlocking() && f > 0.0F) {
                            return -(f - ((1.0F + f) * EntityLiving.this.world.paperSpigotConfig.playerBlockingDamageMultiplier));
                        }
                    }
                    return -0.0;
                }
            };
            final float blockingModifier = blocking.apply((double) f).floatValue();
            f += blockingModifier;

            final Function<Double, Double> armor = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -(f - EntityLiving.this.applyArmorModifier(damagesource, f.floatValue()));
                }
            };
            final float armorModifier = armor.apply((double) f).floatValue();
            f += armorModifier;

            final Function<Double, Double> resistance = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    if (!damagesource.isStarvation() && EntityLiving.this.hasEffect(MobEffectList.RESISTANCE) && damagesource != DamageSource.OUT_OF_WORLD) {
                        int i = (EntityLiving.this.getEffect(MobEffectList.RESISTANCE).getAmplifier() + 1) * 5;
                        int j = 25 - i;
                        float f1 = f.floatValue() * (float) j;
                        return -(f - (f1 / 25.0F));
                    }
                    return -0.0;
                }
            };
            final float resistanceModifier = resistance.apply((double) f).floatValue();
            f += resistanceModifier;

            final Function<Double, Double> magic = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -(f - EntityLiving.this.applyMagicModifier(damagesource, f.floatValue()));
                }
            };
            final float magicModifier = magic.apply((double) f).floatValue();
            f += magicModifier;

            final Function<Double, Double> absorption = new Function<Double, Double>() {
                @Override
                public Double apply(Double f) {
                    return -(Math.max(f - Math.max(f - EntityLiving.this.getAbsorptionHearts(), 0.0F), 0.0F));
                }
            };
            float absorptionModifier = absorption.apply((double) f).floatValue();

            final EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent(this, damagesource, originalDamage, hardHatModifier, blockingModifier, armorModifier, resistanceModifier, magicModifier, absorptionModifier, hardHat, blocking, armor, resistance, magic, absorption);
            if (event.isCancelled()) {
                return false;
            }

            f = (float) event.getFinalDamage();

            // Apply damage to helmet
            if ((damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK) && this.getEquipment(4) != null) {
                this.getEquipment(4).damage((int) (event.getDamage() * 4.0F + this.random.nextFloat() * event.getDamage() * 2.0F), this);
            }

            // Apply damage to armor
            if (!damagesource.ignoresArmor()) {
                float armorDamage = (float) (event.getDamage() + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.HARD_HAT));
                this.damageArmor(armorDamage);
            }

            absorptionModifier = (float) -event.getDamage(DamageModifier.ABSORPTION);
            this.setAbsorptionHearts(Math.max(this.getAbsorptionHearts() - absorptionModifier, 0.0F));
            if (f != 0.0F) {
                if (human) {
                    ((EntityHuman) this).applyExhaustion(damagesource.getExhaustionCost());
                }
                // CraftBukkit end
                final float f2 = this.getHealth();

                this.setHealth(f2 - f);
                this.aW().a(damagesource, f2, f);
                // CraftBukkit start
                if (human) {
                    return true;
                }
                // CraftBukkit end
                this.setAbsorptionHearts(this.getAbsorptionHearts() - f);
            }
            return true; // CraftBukkit
        }
        return false; // CraftBukkit
    }*/
    
    protected boolean d(final DamageSource damagesource, float f) {
        if (this.isInvulnerable()) {
            return false;
        }
        final boolean human = this instanceof EntityHuman;
        final float originalDamage = f;

        // Hard hat modifier
        final Function<Double, Double> hardHat = (Double d) -> {
            if ((damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK) && this.getEquipment(4) != null) {
                return -(d - (d * 0.75F));
            }
            return -0.0;
        };
        float hardHatModifier = hardHat.apply((double) f).floatValue();
        f += hardHatModifier;

        // Blocking modifier
        final Function<Double, Double> blocking = (Double d) -> {
            if (human && !damagesource.ignoresArmor() && ((EntityHuman) this).isBlocking() && d > 0.0F) {
                return -(d - ((1.0F + d) * this.world.paperSpigotConfig.playerBlockingDamageMultiplier));
            }
            return -0.0;
        };
        float blockingModifier = blocking.apply((double) f).floatValue();
        f += blockingModifier;

        // Armor modifier
        final Function<Double, Double> armor = (Double d) -> -(d - this.applyArmorModifier(damagesource, d.floatValue()));
        float armorModifier = armor.apply((double) f).floatValue();
        f += armorModifier;

        // Resistance modifier
        final Function<Double, Double> resistance = (Double d) -> {
            if (!damagesource.isStarvation() && this.hasEffect(MobEffectList.RESISTANCE) && damagesource != DamageSource.OUT_OF_WORLD) {
                int i = (this.getEffect(MobEffectList.RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f1 = d.floatValue() * (float) j;
                return -(d - (f1 / 25.0F));
            }
            return -0.0;
        };
        float resistanceModifier = resistance.apply((double) f).floatValue();
        f += resistanceModifier;

        // Magic modifier
        final Function<Double, Double> magic = (Double d) -> -(d - this.applyMagicModifier(damagesource, d.floatValue()));
        float magicModifier = magic.apply((double) f).floatValue();
        f += magicModifier;

        // Absorption modifier
        final Function<Double, Double> absorption = (Double d) -> -(Math.max(d - Math.max(d - this.getAbsorptionHearts(), 0.0F), 0.0F));
        float absorptionModifier = absorption.apply((double) f).floatValue();

        // Handle event and cancellation
        final EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent(this, damagesource, originalDamage, hardHatModifier, blockingModifier, armorModifier, resistanceModifier, magicModifier, absorptionModifier, hardHat, blocking, armor, resistance, magic, absorption);
        if (event.isCancelled()) {
            return false;
        }

        f = (float) event.getFinalDamage();

        // Apply damage to helmet
        if ((damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK) && this.getEquipment(4) != null) {
            this.getEquipment(4).damage((int) (event.getDamage() * 4.0F + this.random.nextFloat() * event.getDamage() * 2.0F), this);
        }

        // Apply damage to armor
        if (!damagesource.ignoresArmor()) {
            float armorDamage = (float) (event.getDamage() + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.HARD_HAT));
            this.damageArmor(armorDamage);
        }

        absorptionModifier = (float) -event.getDamage(DamageModifier.ABSORPTION);
        this.setAbsorptionHearts(Math.max(this.getAbsorptionHearts() - absorptionModifier, 0.0F));

        // Apply health and exhaustion changes
        if (f != 0.0F) {
            if (human) {
                ((EntityHuman) this).applyExhaustion(damagesource.getExhaustionCost());
            }

            float currentHealth = this.getHealth();
            this.setHealth(currentHealth - f);
            this.aW().a(damagesource, currentHealth, f);

            if (!human) {
                this.setAbsorptionHearts(this.getAbsorptionHearts() - f);
            }
        }

        return true;
    }

    public CombatTracker aW() {
        return this.combatTracker;
    }

    public EntityLiving aX() {
        return (EntityLiving) (this.combatTracker.c() != null ? this.combatTracker.c() : (this.killer != null ? this.killer : (this.lastDamager != null ? this.lastDamager : null)));
    }

    public final float getMaxHealth() {
        return (float) this.getAttributeInstance(GenericAttributes.maxHealth).getValue();
    }

    public final int aZ() {
        return this.datawatcher.getByte(9);
    }

    public final void p(int i) {
        this.datawatcher.watch(9, Byte.valueOf((byte) i));
    }

    private int j() {
        return this.hasEffect(MobEffectList.FASTER_DIG) ? 6 - (1 + this.getEffect(MobEffectList.FASTER_DIG).getAmplifier()) * 1 : (this.hasEffect(MobEffectList.SLOWER_DIG) ? 6 + (1 + this.getEffect(MobEffectList.SLOWER_DIG).getAmplifier()) * 2 : 6);
    }

    public void ba() {
        if (!this.at || this.au >= this.j() / 2 || this.au < 0) {
            this.au = -1;
            this.at = true;
            if (this.world instanceof WorldServer) {
                ((WorldServer) this.world).getTracker().a((Entity) this, new PacketPlayOutAnimation(this, 0));
            }
        }
    }

    protected void G() {
        this.damageEntity(DamageSource.OUT_OF_WORLD, 4.0F);
    }

    protected void bb() {
    	final int i = this.j();

        if (this.at) {
            ++this.au;
            if (this.au >= i) {
                this.au = 0;
                this.at = false;
            }
        } else {
            this.au = 0;
        }

        this.aD = (float) this.au / (float) i;
    }

    public AttributeInstance getAttributeInstance(IAttribute iattribute) {
        return this.getAttributeMap().a(iattribute);
    }

    public AttributeMapBase getAttributeMap() {
        if (this.d == null) {
            this.d = new AttributeMapServer();
        }

        return this.d;
    }

    public EnumMonsterType getMonsterType() {
        return EnumMonsterType.UNDEFINED;
    }

    public abstract ItemStack be();

    public abstract ItemStack getEquipment(int i);

    public abstract void setEquipment(int i, ItemStack itemstack);

    public void setSprinting(boolean flag) {
        super.setSprinting(flag);
        final AttributeInstance attributeinstance = this.getAttributeInstance(GenericAttributes.d);

        if (attributeinstance.a(b) != null) {
            attributeinstance.b(c);
        }

        if (flag) {
            attributeinstance.a(c);
        }
    }

    public abstract ItemStack[] getEquipment();

    protected float bf() {
        return 1.0F;
    }

    protected float bg() {
        return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
    }

    protected boolean bh() {
        return this.getHealth() <= 0.0F;
    }

    public void enderTeleportTo(double d0, double d1, double d2) {
        this.setPositionRotation(d0, d1, d2, this.yaw, this.pitch);
    }

    public void m(Entity entity) {
        double d0 = entity.locX;
        double d1 = entity.boundingBox.b + (double) entity.length;
        double d2 = entity.locZ;
        final byte b0 = 1;

        for (int i = -b0; i <= b0; ++i) {
            for (int j = -b0; j < b0; ++j) {
                if (i != 0 || j != 0) {
                    int k = (int) (this.locX + (double) i);
                    int l = (int) (this.locZ + (double) j);
                    AxisAlignedBB axisalignedbb = this.boundingBox.c((double) i, 1.0D, (double) j);

                    if (this.world.a(axisalignedbb).isEmpty()) {
                        if (World.a((IBlockAccess) this.world, k, (int) this.locY, l)) {
                            this.enderTeleportTo(this.locX + (double) i, this.locY + 1.0D, this.locZ + (double) j);
                            return;
                        }

                        if (World.a((IBlockAccess) this.world, k, (int) this.locY - 1, l) || this.world.getType(k, (int) this.locY - 1, l).getMaterial() == Material.WATER) {
                            d0 = this.locX + (double) i;
                            d1 = this.locY + 1.0D;
                            d2 = this.locZ + (double) j;
                        }
                    }
                }
            }
        }

        this.enderTeleportTo(d0, d1, d2);
    }

    protected long lastJumpTime = 0L; // Reaper start - arrow velocity fix
    protected void bj() {
    	// Reaper start - arrow velocity fix // TODO
    	boolean canCrit = true;
    	if (this.world.paperSpigotConfig.fixArrowVelocity) {
	    	final long time = System.nanoTime();
	    	if (this instanceof EntityPlayer) {
	    		canCrit = false;
	    		if (time - this.lastJumpTime > (long)(0.250e9)) {
	    			this.lastJumpTime = time;
	    			canCrit = true;
	    		}
	    	}
    	}
    	// Reaper end - arrow velocity fix
        this.motY = 0.41999998688697815D;
        if (this.hasEffect(MobEffectList.JUMP)) {
            this.motY += (double) ((float) (this.getEffect(MobEffectList.JUMP).getAmplifier() + 1) * 0.1F);
        }

        if (canCrit) // Reaper - arrow velocity fix
        if (this.isSprinting()) {
            float f = this.yaw * 0.017453292F;

            this.motX -= (double) (MathHelper.sin(f) * 0.2F);
            this.motZ += (double) (MathHelper.cos(f) * 0.2F);
        }

        this.al = true;
    }

    public void e(float f, float f1) {
        double d0;

        if (this.M() && (!(this instanceof EntityHuman) || !((EntityHuman) this).abilities.isFlying)) {
            d0 = this.locY;
            this.a(f, f1, this.bk() ? 0.04F : 0.02F);
            this.move(this.motX, this.motY, this.motZ);
            this.motX *= 0.800000011920929D;
            this.motY *= 0.800000011920929D;
            this.motZ *= 0.800000011920929D;
            this.motY -= 0.02D;
            if (this.positionChanged && this.c(this.motX, this.motY + 0.6000000238418579D - this.locY + d0, this.motZ)) {
                this.motY = 0.30000001192092896D;
            }
        } else if (this.P() && (!(this instanceof EntityHuman) || !((EntityHuman) this).abilities.isFlying)) {
            d0 = this.locY;
            this.a(f, f1, 0.02F);
            this.move(this.motX, this.motY, this.motZ);
            this.motX *= 0.5D;
            this.motY *= 0.5D;
            this.motZ *= 0.5D;
            this.motY -= 0.02D;
            if (this.positionChanged && this.c(this.motX, this.motY + 0.6000000238418579D - this.locY + d0, this.motZ)) {
                this.motY = 0.30000001192092896D;
            }
        } else {
            float f2 = (!this.onGround ? 0.91F : this.world.getType(MathHelper.floor(this.locX), MathHelper.floor(this.boundingBox.b) - 1, MathHelper.floor(this.locZ)).frictionFactor * 0.91F);
            final float f3 = 0.16277136F / (f2 * f2 * f2);
            final float f4 = (this.onGround ? this.bl() * f3 : this.aQ);
            this.a(f, f1, f4);
            f2 = (!this.onGround ? 0.91F : this.world.getType(MathHelper.floor(this.locX), MathHelper.floor(this.boundingBox.b) - 1, MathHelper.floor(this.locZ)).frictionFactor * 0.91F);

            if (this.h_()) {
            	final float f5 = 0.15F;
            	
            	// PaperSpigot 1.8 - faster computation
            	this.motX = MathHelper.a(this.motX, -f5, f5);
            	this.motZ = MathHelper.a(this.motZ, -f5, f5);
            	// PaperSpigot 1.8 end
            	
                this.fallDistance = 0.0F;
                if (this.motY < -0.15D) {
                    this.motY = -0.15D;
                }

                final boolean flag = this.isSneaking() && this instanceof EntityHuman;
                if (flag && this.motY < 0.0D) {
                    this.motY = 0.0D;
                }
            }

            this.move(this.motX, this.motY, this.motZ);
            if (this.positionChanged && this.h_()) {
                this.motY = 0.2D;
            }

            if (this.world.isStatic && (!this.world.isLoaded((int) this.locX, 0, (int) this.locZ) || !this.world.getChunkAtWorldCoords((int) this.locX, (int) this.locZ).d)) {
                this.motY = (this.locY > 0.0D ? -0.1D : 0.0D);
            } else {
                this.motY -= 0.08D;
            }

            this.motY *= 0.9800000190734863D;
            this.motX *= (double) f2;
            this.motZ *= (double) f2;
        }

        this.aE = this.aF;
        d0 = this.locX - this.lastX;
        final double d1 = this.locZ - this.lastZ;
        final float f6 = Math.min((float)Math.sqrt(d0 * d0 + d1 * d1) * 4.0F, 1.0F);
        this.aF += (f6 - this.aF) * 0.4F;
        this.aG += this.aF;
    }

    protected boolean bk() {
        return false;
    }

    public float bl() {
        return this.bk() ? this.bp : 0.1F;
    }

    public void i(float f) {
        this.bp = f;
    }

    public boolean n(Entity entity) {
        this.l(entity);
        return false;
    }

    public boolean isSleeping() {
        return false;
    }

    public void tick() {
        super.tick();
        if (!this.world.isStatic) {
            final int i = this.aZ();

            if (i > 0) {
                if (this.av <= 0) {
                    this.av = 20 * (30 - i);
                }

                --this.av;
                if (this.av <= 0) {
                    this.p(i - 1);
                }
            }

            for (int j = 0; j < 5; ++j) {
                ItemStack itemstack = this.g[j];
                ItemStack itemstack1 = this.getEquipment(j);

                if (!ItemStack.matches(itemstack1, itemstack)) {
                    ((WorldServer) this.world).getTracker().a((Entity) this, new PacketPlayOutEntityEquipment(this.getId(), j, itemstack1));
                    if (itemstack != null) {
                        this.d.a(itemstack.D());
                    }

                    if (itemstack1 != null) {
                        this.d.b(itemstack1.D());
                    }

                    this.g[j] = itemstack1 == null ? null : itemstack1.cloneItemStack();
                }
            }

            if (this.ticksLived % 20 == 0) {
                this.aW().g();
            }
        }

        this.e();
        final double d0 = this.locX - this.lastX;
        final double d1 = this.locZ - this.lastZ;
        final float f = (float) (d0 * d0 + d1 * d1);
        float f1 = this.aM;
        float f2 = 0.0F;

        this.aV = this.aW;
        float f3 = 0.0F;

        if (f > 0.0025000002F) {
            f3 = 1.0F;
            f2 = (float) Math.sqrt((double) f) * 3.0F;
            // CraftBukkit - Math -> TrigMath
            f1 = (float) org.bukkit.craftbukkit.TrigMath.atan2(d1, d0) * 180.0F / 3.1415927F - 90.0F;
        }

        if (this.aD > 0.0F) {
            f1 = this.yaw;
        }

        if (!this.onGround) {
            f3 = 0.0F;
        }

        this.aW += (f3 - this.aW) * 0.3F;
        f2 = this.f(f1, f2);

        while (this.yaw - this.lastYaw < -180.0F) {
            this.lastYaw -= 360.0F;
        }

        while (this.yaw - this.lastYaw >= 180.0F) {
            this.lastYaw += 360.0F;
        }

        while (this.aM - this.aN < -180.0F) {
            this.aN -= 360.0F;
        }

        while (this.aM - this.aN >= 180.0F) {
            this.aN += 360.0F;
        }

        while (this.pitch - this.lastPitch < -180.0F) {
            this.lastPitch -= 360.0F;
        }

        while (this.pitch - this.lastPitch >= 180.0F) {
            this.lastPitch += 360.0F;
        }

        while (this.aO - this.aP < -180.0F) {
            this.aP -= 360.0F;
        }

        while (this.aO - this.aP >= 180.0F) {
            this.aP += 360.0F;
        }

        this.aX += f2;
    }

    protected float f(float f, float f1) {
    	final float f2 = MathHelper.g(f - this.aM);

        this.aM += f2 * 0.3F;
        float f3 = MathHelper.a(MathHelper.g(this.yaw - this.aM), -75.0f, 75.0f); // Rinny - faster computation
        final boolean flag = f3 < -90.0F || f3 >= 90.0F;

        /*
        if (f3 < -75.0F) {
            f3 = -75.0F;
        }

        if (f3 >= 75.0F) {
            f3 = 75.0F;
        }*/

        this.aM = this.yaw - f3;
        if (f3 * f3 > 2500.0F) {
            this.aM += f3 * 0.2F;
        }

        if (flag) {
            f1 *= -1.0F;
        }

        return f1;
    }

    public void e() {
        if (this.bq > 0) {
            --this.bq;
        }

        if (this.bg > 0) {
        	final double d0 = this.locX + (this.bh - this.locX) / (double) this.bg;
        	final double d1 = this.locY + (this.bi - this.locY) / (double) this.bg;
        	final double d2 = this.locZ + (this.bj - this.locZ) / (double) this.bg;
        	final double d3 = MathHelper.g(this.bk - (double) this.yaw);

            this.yaw = (float) ((double) this.yaw + d3 / (double) this.bg);
            this.pitch = (float) ((double) this.pitch + (this.bl - (double) this.pitch) / (double) this.bg);
            --this.bg;
            this.setPosition(d0, d1, d2);
            this.b(this.yaw, this.pitch);
        } else if (!this.br()) {
            this.motX *= 0.98D;
            this.motY *= 0.98D;
            this.motZ *= 0.98D;
        }

        if (Math.abs(this.motX) < 0.005D) {
            this.motX = 0.0D;
        }

        if (Math.abs(this.motY) < 0.005D) {
            this.motY = 0.0D;
        }

        if (Math.abs(this.motZ) < 0.005D) {
            this.motZ = 0.0D;
        }

        if (this.bh()) {
            this.bc = false;
            this.bd = 0.0F;
            this.be = 0.0F;
            this.bf = 0.0F;
        } else if (this.br()) {
            if (this.bk()) {
                this.bn();
            } else {
                this.bq();
                this.aO = this.yaw;
            }
        }
        if (this.bc) {
            if (!this.M() && !this.P()) {
                if (this.onGround && this.bq == 0) {
                    this.bj();
                    this.bq = 10;
                }
            } else {
                this.motY += 0.03999999910593033D;
            }
        } else {
            this.bq = 0;
        }

        this.bd *= 0.98F;
        this.be *= 0.98F;
        this.bf *= 0.9F;
        this.e(this.bd, this.be);
        if (!this.world.isStatic) {
            this.bo();
        }
    }

    protected void bn() {}

    protected void bo() {
    	final List<Entity> list = this.world.getEntities(this, this.boundingBox.grow(0.20000000298023224D, 0.0D, 0.20000000298023224D));

        if (this.R() && list != null && !list.isEmpty()) { // Spigot: Add this.R() condition
            numCollisions -= world.spigotConfig.maxCollisionsPerEntity; // Spigot
            for (int i = 0; i < list.size(); ++i) {
                if (numCollisions > world.spigotConfig.maxCollisionsPerEntity) { // Spigot
                	break; 
                } 
                Entity entity = (Entity) list.get(i);

                // TODO better check now?
                // CraftBukkit start - Only handle mob (non-player) collisions every other tick
                if ((this.ticksLived & 1) == 0 && entity instanceof EntityLiving && !(this instanceof EntityPlayer)) { // Rinny - use bitwise instead of modulus + change order
                    continue;
                }
                // CraftBukkit end

                if (entity.S()) {
                    entity.numCollisions++; // Spigot
                    numCollisions++; // Spigot
                    this.o(entity);
                }
            }
            numCollisions = 0; // Spigot
        }
    }

    protected void o(Entity entity) {
        entity.collide(this);
    }

    public void ab() {
        super.ab();
        this.aV = this.aW;
        this.aW = 0.0F;
        this.fallDistance = 0.0F;
    }

    protected void bp() {}

    protected void bq() {
        ++this.aU;
    }

    public void f(boolean flag) {
        this.bc = flag;
    }

    public void receive(Entity entity) {
        if (!entity.dead && !this.world.isStatic && entity instanceof ICollect) {
            final EntityTracker entitytracker = ((WorldServer) this.world).getTracker();
            entitytracker.a(entity, new PacketPlayOutCollect(entity.getId(), this.getId()));
        }
    }

    public boolean hasLineOfSight(Entity entity) {
    	final Vec3D vec = Vec3D.a(this.locX, this.locY + (double) this.getHeadHeight(), this.locZ);
    	
    	if (entity instanceof EntityPlayer) {
        	final double parts = entity.getHeadHeight() / 4;
        	final Vec3D vec3 = Vec3D.a(entity.locX, entity.locY, entity.locZ);
        	
        	return this.world.rayTrace(vec, vec3.add(0.0D, (parts * 3), 0.0D)) == null || this.world.rayTrace(vec, vec3.add(0.0D, (parts * 4), 0.0D)) == null || this.world.rayTrace(vec, vec3.add(0.0D, (parts * 2), 0.0D)) == null || this.world.rayTrace(vec, vec3.add(0.0D, (parts * 1), 0.0D)) == null;
    	}
        return this.world.rayTrace(vec, Vec3D.a(entity.locX, entity.locY + (double) this.getHeadHeight(), entity.locZ)) == null;
    }

    public Vec3D ag() {
        return this.j(1.0F);
    }

    public Vec3D j(float f) {
        float f1;
        float f2;
        float f3;
        float f4;

        if (f == 1.0F) {
            f1 = MathHelper.cos(-this.yaw * 0.017453292F - 3.1415927F);
            f2 = MathHelper.sin(-this.yaw * 0.017453292F - 3.1415927F);
            f3 = -MathHelper.cos(-this.pitch * 0.017453292F);
            f4 = MathHelper.sin(-this.pitch * 0.017453292F);
            return Vec3D.a((double) (f2 * f3), (double) f4, (double) (f1 * f3));
        } else {
            f1 = this.lastPitch + (this.pitch - this.lastPitch) * f;
            f2 = this.lastYaw + (this.yaw - this.lastYaw) * f;
            f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
            f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
            final float f5 = -MathHelper.cos(-f1 * 0.017453292F);
            final float f6 = MathHelper.sin(-f1 * 0.017453292F);

            return Vec3D.a((double) (f4 * f5), (double) f6, (double) (f3 * f5));
        }
    }

    public boolean br() {
        return !this.world.isStatic;
    }

    public boolean R() {
        return !this.dead;
    }

    public boolean S() {
        return !this.dead;
    }

    public float getHeadHeight() {
        return this.length * 0.85F;
    }

    protected void Q() {
        this.velocityChanged = this.random.nextDouble() >= this.getAttributeInstance(GenericAttributes.c).getValue();
    }

    public float getHeadRotation() {
        return this.aO;
    }

    public float getAbsorptionHearts() {
        return this.br;
    }

    public void setAbsorptionHearts(float f) {
        if (f < 0.0F) {
            f = 0.0F;
        }

        this.br = f;
    }

    public ScoreboardTeamBase getScoreboardTeam() {
        return null;
    }

    public boolean c(EntityLiving entityliving) {
        return this.a(entityliving.getScoreboardTeam());
    }

    public boolean a(ScoreboardTeamBase scoreboardteambase) {
        return this.getScoreboardTeam() != null ? this.getScoreboardTeam().isAlly(scoreboardteambase) : false;
    }

    public void bu() {}

    public void bv() {}
    
    public void setExtraKnockback(boolean bool) {
    	this.extraKnockback = bool;
    }
    
    public boolean hasExtraKnockback() {
    	return this.extraKnockback;
    }
}
