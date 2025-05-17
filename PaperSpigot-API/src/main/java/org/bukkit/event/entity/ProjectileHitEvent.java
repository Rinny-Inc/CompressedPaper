package org.bukkit.event.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.HandlerList;

import io.noks.enums.BodyParts;

/**
 * Called when a projectile hits an object
 */
public class ProjectileHitEvent extends EntityEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Entity hitEntity;
    private final BodyParts hittedPart;

	public ProjectileHitEvent(Projectile projectile) { 
    	this(projectile, null); 
    }
    
	public ProjectileHitEvent(Projectile projectile, Entity hitEntity) {
		super(projectile);
		this.hitEntity = hitEntity;
		this.hittedPart = null;
	}
	
	public ProjectileHitEvent(Projectile projectile, Player hitPlayer, BodyParts hittedPart) {
		super(projectile);
		this.hitEntity = hitPlayer;
		this.hittedPart = hittedPart;
	}

    @Override
    public Projectile getEntity() {
        return (Projectile) entity;
    }
    
    public Entity getHitEntity() { 
    	return this.hitEntity; 
    }
    
    public BodyParts getHitPart() {
    	return this.hittedPart;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
