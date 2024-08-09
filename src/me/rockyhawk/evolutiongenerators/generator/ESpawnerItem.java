package me.rockyhawk.evolutiongenerators.generator;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ESpawnerItem {
    private final ItemStack item;
    private final String ID;

    EvolutionGenerators plugin = JavaPlugin.getPlugin(EvolutionGenerators.class);

    public ESpawnerItem(String id, int level, UUID p){
        ID = id;
        item = new EItemStack(ID, p, level, false).getItemStack();
    }

    public boolean isSimilar(ItemStack itm){
        if(plugin.nbt.hasNBT(itm, "EvolutionGenerators")) {
            return plugin.nbt.getNBT(itm, "EvolutionGenerators").split("_")[0].equals(ID);
        }
        return false;
    }

    public ItemStack getItemStack(){
        return this.item;
    }
}
