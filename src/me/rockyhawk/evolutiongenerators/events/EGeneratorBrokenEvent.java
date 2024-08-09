package me.rockyhawk.evolutiongenerators.events;

import me.rockyhawk.evolutiongenerators.generator.EGenerator;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EGeneratorBrokenEvent extends Event implements Cancellable {
    private final Player p;
    private boolean isCancelled;
    private final EGenerator generator;

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public EGeneratorBrokenEvent(Player player, EGenerator gen) {
        this.p = player;
        this.generator = gen;
    }

    public Player getPlayer(){
        return this.p;
    }

    public EGenerator getGenerator(){
        return this.generator;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
