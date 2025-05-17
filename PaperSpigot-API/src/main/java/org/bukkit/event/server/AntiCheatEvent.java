package org.bukkit.event.server;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class AntiCheatEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();
	private String msg;
	private Type type;

	public enum Type {
		ANTI_KB,
		FLY,
		REACH;
	}

	public AntiCheatEvent(Player player, Type type, String msg) {
		super(player);
		this.type = type;
		this.msg = msg;
	}

	public String getMsg() {
		return this.msg;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Type getType() {
		return this.type;
	}
}
