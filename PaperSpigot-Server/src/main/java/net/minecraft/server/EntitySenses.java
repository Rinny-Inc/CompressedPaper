package net.minecraft.server;

import java.util.BitSet;

public class EntitySenses {
	private final EntityInsentient entity;
	// Rinny
	private final BitSet seenEntities;
    private final BitSet unseenEntities;

    public EntitySenses(EntityInsentient entity) {
        this.entity = entity;
        this.seenEntities = new BitSet();
        this.unseenEntities = new BitSet();
    }

    public void clear() {
        this.seenEntities.clear();
        this.unseenEntities.clear();
    }

    public boolean canSee(Entity target) {
        final int id = target.getId();

        if (seenEntities.get(id)) {
            return true;
        }
        if (unseenEntities.get(id)) {
            return false;
        }

        boolean canSee = entity.hasLineOfSight(target);
        if (canSee) {
            seenEntities.set(id);
        } else {
            unseenEntities.set(id);
        }
        return canSee;
    }
    // Rinny
    
    
	/*private final Set<Entity> seenEntities;
	private final Set<Entity> unseenEntities;

	public EntitySenses(EntityInsentient paramEntityInsentient) {
		this.entity = paramEntityInsentient;
		this.seenEntities = new HashSet<>();
        this.unseenEntities = new HashSet<>();
	}

	public void a() {
		this.seenEntities.clear();
		this.unseenEntities.clear();
	}

	public boolean canSee(Entity paramEntity) {
		if (this.seenEntities.contains(paramEntity)) {
			return true;
		}
		if (this.unseenEntities.contains(paramEntity)) {
			return false;
		}
		final boolean bool = this.entity.hasLineOfSight(paramEntity);
		if (bool) {
			this.seenEntities.add(paramEntity);
		} else {
			this.unseenEntities.add(paramEntity);
		}
		return bool;
	}*/
}
