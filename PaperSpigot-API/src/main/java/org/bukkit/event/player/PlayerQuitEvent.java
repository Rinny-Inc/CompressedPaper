package org.bukkit.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.Nullable;

/**
 * Called when a player leaves a server
 */
public class PlayerQuitEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private String quitMessage;
    private final String disconnectReason;
    
    public PlayerQuitEvent(Player who, String quitMessage, @Nullable String disconnectReason) {
    	super(who);
    	this.quitMessage = quitMessage;
    	this.disconnectReason = disconnectReason;
	}

    public PlayerQuitEvent(final Player who, final String quitMessage) {
    	this(who, quitMessage, null);
    }
    
    /**
     * Gets the exact reason the player disconnected from the server
     *
     * @return string disconnect reason
     */
	public String getDisconnectReason() {
		return disconnectReason;
	}

    /**
     * Gets the quit message to send to all online players
     *
     * @return string quit message
     */
    public String getQuitMessage() {
        return quitMessage;
    }

    /**
     * Sets the quit message to send to all online players
     *
     * @param quitMessage quit message
     */
    public void setQuitMessage(String quitMessage) {
        this.quitMessage = quitMessage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
