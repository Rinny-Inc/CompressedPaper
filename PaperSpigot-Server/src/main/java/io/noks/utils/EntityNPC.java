package io.noks.utils;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;

import net.minecraft.server.DamageSource;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerInteractManager;
import net.minecraft.server.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;

public class EntityNPC extends EntityPlayer {
	public EntityNPC(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
		super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
		this.collidesWithEntities = false;
		new DummyPlayerConnection((MinecraftServer) ((CraftServer) Bukkit.getServer()).getHandle().getServer(), new DummyNetworkManager(), this);
	}
	
	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
		return false;
	}
}
