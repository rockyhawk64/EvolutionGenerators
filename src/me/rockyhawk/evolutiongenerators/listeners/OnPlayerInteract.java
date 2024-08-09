package me.rockyhawk.evolutiongenerators.listeners;

import me.rockyhawk.commandpanels.api.PanelOpenedEvent;
import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import me.rockyhawk.evolutiongenerators.events.EGeneratorOpenedEvent;
import me.rockyhawk.evolutiongenerators.generator.EGenerator;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class OnPlayerInteract implements Listener {
    private final EvolutionGenerators plugin;
    public OnPlayerInteract(EvolutionGenerators pl) {
        this.plugin = pl;
    }
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerClickGen(PlayerInteractEvent e) {
        if(e.isCancelled()){
            return;
        }
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            //for opening a generator
            if (plugin.isGenerator(e.getClickedBlock().getLocation())) {
                //if placing a block
                if(e.getItem() != null){
                    if(e.getItem().getType().isBlock() && e.getPlayer().isSneaking()){
                        return;
                    }
                }

                if(!e.getPlayer().hasPermission("egenerators.use")){
                    e.getPlayer().sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                    return;
                }

                EGenerator gen = plugin.generatorsList.get(e.getClickedBlock().getLocation());
                e.setCancelled(true);

                if (gen.isLocked() && !e.getPlayer().getUniqueId().equals(gen.getOwner())) {
                    if (e.getPlayer().hasPermission("egenerators.admin")) {
                        e.getPlayer().sendMessage(plugin.color(plugin.tag + plugin.config.getString("lock.override")));
                    } else {
                        e.getPlayer().sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                        return;
                    }
                }

                EGeneratorOpenedEvent openedEvent = new EGeneratorOpenedEvent(e.getPlayer(), plugin.generatorsList.get(e.getClickedBlock().getLocation()));
                Bukkit.getPluginManager().callEvent(openedEvent);
                if (openedEvent.isCancelled()) {
                    return;
                }

                plugin.invHandle.open(gen, e.getPlayer());
            }
        }
    }
}
