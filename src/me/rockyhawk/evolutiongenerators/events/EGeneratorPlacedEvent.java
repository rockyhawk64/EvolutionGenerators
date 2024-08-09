package me.rockyhawk.evolutiongenerators.events;

import me.rockyhawk.evolutiongenerators.generator.EGeneratorLoader;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EGeneratorPlacedEvent extends Event implements Cancellable {
    private final Player p;
    private boolean isCancelled;
    private final EGeneratorLoader loader;

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public EGeneratorPlacedEvent(Player player, EGeneratorLoader gen) {
        this.p = player;
        this.loader = gen;
    }

    public Player getPlayer(){
        return this.p;
    }

    public Block getBlock(){
        return this.loader.block;
    }

    public EGeneratorLoader getGenerator(){
        return this.loader;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
