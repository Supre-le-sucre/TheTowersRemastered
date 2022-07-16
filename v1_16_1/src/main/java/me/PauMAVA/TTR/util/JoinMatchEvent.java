package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.match.TTRMatch;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class JoinMatchEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private TTRMatch match;
    private boolean isCancelled;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public JoinMatchEvent(Player player, TTRMatch match) {
        this.player = player;
        this.match = match;
        this.isCancelled = false;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public Player getPlayer() {
        return this.player;
    }

    public TTRMatch getMatch() {
        return this.match;
    }


}
