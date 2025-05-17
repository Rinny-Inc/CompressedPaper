package org.bukkit.event.server;

import java.util.Date;

import org.bukkit.event.HandlerList;

public class ServerDateChangeEvent extends ServerEvent {
	private static final HandlerList handlers = new HandlerList();
	private Date date;
	
	public ServerDateChangeEvent(Date date) {
		this.date = date;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	@Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
