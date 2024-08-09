package me.rockyhawk.evolutiongenerators.listeners;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;

public class OnItemEvents implements Listener {
    private final EvolutionGenerators plugin;
    public OnItemEvents(EvolutionGenerators pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void onItemDeath(ItemDespawnEvent e){
        if (isItemEgen(e.getEntity().getItemStack())) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (isItemEgen(e.getItem().getItemStack())) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onItemMerge(ItemMergeEvent e) {
        try {
            if (isItemEgen(e.getEntity().getItemStack())) {
                e.setCancelled(true);
            }
        }catch(Exception ignore){
            //skip
        }
    }

    @EventHandler
    public void gravityBlockFall(EntityChangeBlockEvent e){
        if(e.isCancelled()){
            return;
        }
        if(e.getEntityType() == EntityType.FALLING_BLOCK) {
            if (plugin.isGenerator(e.getBlock().getRelative(BlockFace.DOWN).getLocation())) {
                e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), new ItemStack(e.getTo()));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void liquidSpread(BlockFromToEvent e){
        if(plugin.isGenerator(e.getToBlock().getRelative(BlockFace.DOWN).getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void spawnerSpawn(SpawnerSpawnEvent e) {
        if(plugin.isGenerator(e.getSpawner().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void pistonPush(BlockPistonExtendEvent e) {
        for(Block blo : e.getBlocks()){
            if(plugin.isGenerator(blo.getLocation())){
                e.setCancelled(true);
            }
        }
    }

    //returns true if item is an egen item
    public boolean isItemEgen(ItemStack itm){
        return plugin.nbt.hasNBT(itm,"EvolutionStationary");
    }
}
