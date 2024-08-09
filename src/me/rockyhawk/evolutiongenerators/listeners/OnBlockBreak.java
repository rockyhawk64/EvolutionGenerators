package me.rockyhawk.evolutiongenerators.listeners;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import me.rockyhawk.evolutiongenerators.events.EGeneratorBrokenEvent;
import me.rockyhawk.evolutiongenerators.generator.EGenerator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;


public class OnBlockBreak implements Listener {
    private final EvolutionGenerators plugin;
    public OnBlockBreak(EvolutionGenerators pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.isCancelled()){
            return;
        }

        EGenerator gen = getGenerator(e.getBlock());
        if(gen != null){
            if (gen.isLocked() && !e.getPlayer().getUniqueId().equals(gen.getOwner())) {
                if (e.getPlayer().hasPermission("egenerators.admin")) {
                    e.getPlayer().sendMessage(plugin.color(plugin.tag + plugin.config.getString("lock.override")));
                } else {
                    e.getPlayer().sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                    e.setCancelled(true);
                    return;
                }
            }

            e.setDropItems(false);
            EGeneratorBrokenEvent brokeEvent = new EGeneratorBrokenEvent(e.getPlayer(),gen);
            Bukkit.getPluginManager().callEvent(brokeEvent);
            if(!brokeEvent.isCancelled()){
                gen.delete(e.getPlayer().getGameMode() != GameMode.CREATIVE);
            }
        }
    }
    @EventHandler
    public void onBlockDespawn(LeavesDecayEvent e){
        EGenerator gen = getGenerator(e.getBlock());
        if(gen != null){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockExplode(EntityExplodeEvent e) {
        if(e.isCancelled()){
            return;
        }

        for(Block blo : e.blockList()){
            if(getGenerator(blo) != null){
                if(getGenerator(blo).isLocked()){
                    e.setCancelled(true);
                    return;
                }
                EGeneratorBrokenEvent brokeEvent = new EGeneratorBrokenEvent(null,getGenerator(blo));
                Bukkit.getPluginManager().callEvent(brokeEvent);
                if(!brokeEvent.isCancelled()){
                    blo.getDrops().clear();
                    getGenerator(blo).delete(true);
                }
            }
        }
    }
    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        if(getGenerator(e.getBlock()) != null){
            e.setCancelled(true);
        }
    }

    public EGenerator getGenerator(Block blo){
        if(plugin.isGenerator(blo.getLocation())){
            return plugin.generatorsList.get(blo.getLocation());
        }
        return null;
    }
}
