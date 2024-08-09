package me.rockyhawk.evolutiongenerators.generator;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.logging.Level;

public class EItemStack {
    EvolutionGenerators plugin = JavaPlugin.getPlugin(EvolutionGenerators.class);

    //true if generator makes item, false if item is to place generator
    boolean isGenerated;
    String identification = null;
    UUID owner;
    private ItemStack item = null;
    private final int level;
    ConfigurationSection itemConfig = null;

    public EItemStack(String ID, UUID p, int lvl, boolean isgenerated){
        //get identification
        this.owner = p;
        this.level = lvl;
        this.isGenerated = isgenerated;
        setID(ID);
    }

    public void setID(String ID){
        //ensure the item is not missing from the config
        if(!plugin.itemDetector.checkItemsExist(ID)){
            Bukkit.getLogger().log(Level.SEVERE,"[EvolutionGenerators] Generator " + ID + " is missing an item in items.yml");
            this.item = new ItemStack(Material.STONE);
            return;
        }

        this.identification = ID;
        if(this.isGenerated){
            this.itemConfig = plugin.items.getConfigurationSection("generated-items." + identification);
        }else{
            this.itemConfig = plugin.items.getConfigurationSection("spawner-blocks." + identification);
        }

        //make the item
        buildItem();
    }

    public void buildItem(){
        this.item = CommandPanels.getAPI().makeItem(Bukkit.getPlayer(this.owner),itemConfig);
        if(!this.isGenerated){
            this.item = plugin.nbt.addNBT(this.item,"EvolutionGenerators",identification + "_" + level);
        }
    }

    public ItemStack getItemStack(){
        return new ItemStack(item);
    }
}
