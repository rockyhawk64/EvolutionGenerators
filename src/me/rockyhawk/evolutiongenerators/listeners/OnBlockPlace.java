package me.rockyhawk.evolutiongenerators.listeners;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import me.rockyhawk.evolutiongenerators.events.EGeneratorPlacedEvent;
import me.rockyhawk.evolutiongenerators.generator.EGenerator;
import me.rockyhawk.evolutiongenerators.generator.EGeneratorLoader;
import me.rockyhawk.evolutiongenerators.generator.ESpawnerItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class OnBlockPlace implements Listener{
    private final EvolutionGenerators plugin;
    public OnBlockPlace(EvolutionGenerators pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if(plugin.nbt.hasNBT(e.getItemInHand(), "EvolutionGenerators")){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onGeneratorPlace(PlayerInteractEvent e){
        if(e.isCancelled() || e.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }

        if(e.getClickedBlock() == null || e.getItem() == null){
            return;
        }

        Block block = e.getClickedBlock().getRelative(e.getBlockFace());

        if(!e.getPlayer().isSneaking() && plugin.isGenerator(e.getClickedBlock().getLocation())){
            e.setUseItemInHand(Event.Result.DENY);
            return;
        }

        if(plugin.isGenerator(block.getRelative(BlockFace.DOWN).getLocation())){
            e.setUseItemInHand(Event.Result.DENY);
            return;
        }

        if(!e.getPlayer().hasPermission("egenerators.use")){
            e.getPlayer().sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
            return;
        }
        ESpawnerItem temp;
        for (String genName : plugin.config.getConfigurationSection("generators").getKeys(false)) {
            temp = new ESpawnerItem(genName,1,e.getPlayer().getUniqueId());
            if(temp.isSimilar(e.getItem())){
                //ensure air is above and a generator is not below
                if(!isAir(block.getRelative(BlockFace.UP).getType())){
                    e.setCancelled(true);
                    return;
                }

                //ensure player has not reached limit for generators
                if(plugin.config.getInt("limit.player-generators") != -1){
                    int count = 0;
                    for(EGenerator gen : plugin.generatorsList.values()){
                        if(gen.getOwner().equals(e.getPlayer().getUniqueId())) {
                            count += 1;
                        }
                    }
                    if(count >= plugin.config.getInt("limit.player-generators")){
                        e.getPlayer().sendMessage(plugin.color(plugin.tag + plugin.config.getString("limit.player-message")));
                        e.setCancelled(true);
                        return;
                    }
                }
                //chunk limit for generators
                if(plugin.config.getInt("limit.chunk-generators") != -1){
                    int count = 0;
                    for(EGenerator gen : plugin.generatorsList.values()){
                        if(gen.getBlock().getChunk().equals(block.getChunk())) {
                            count += 1;
                        }
                    }
                    if(count >= plugin.config.getInt("limit.chunk-generators")){
                        e.getPlayer().sendMessage(plugin.color(plugin.tag + plugin.config.getString("limit.chunk-message")));
                        e.setCancelled(true);
                        return;
                    }
                }

                int level = Integer.parseInt(plugin.nbt.getNBT(e.getItem(), "EvolutionGenerators").split("_")[1]);
                EGeneratorLoader loader = new EGeneratorLoader(genName, block, e.getPlayer().getUniqueId());
                loader.setLocked(plugin.config.getBoolean("config.lock-when-placed"));
                loader.setItemAmount(0);
                loader.setPercentage(0);
                loader.setLevel(level);

                EGeneratorPlacedEvent placedEvent = new EGeneratorPlacedEvent(e.getPlayer(),loader);
                Bukkit.getPluginManager().callEvent(placedEvent);
                if(placedEvent.isCancelled()){
                    return;
                }
                if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    e.getItem().setAmount(e.getItem().getAmount()-1);
                }
                loader.makeGenerator();
                return;
            }
        }
    }

    boolean isAir(Material mat){
        return mat == Material.AIR || mat == Material.CAVE_AIR || mat == Material.VOID_AIR;
    }
}
