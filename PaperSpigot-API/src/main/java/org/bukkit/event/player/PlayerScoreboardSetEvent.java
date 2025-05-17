package org.bukkit.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerScoreboardSetEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();
	private Scoreboard board;
	
	public PlayerScoreboardSetEvent(Player who, Scoreboard board) {
		super(who);
		this.board = board;
	}
	
	public Scoreboard getScoreboard() {
		return this.board;
	}

	@Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
