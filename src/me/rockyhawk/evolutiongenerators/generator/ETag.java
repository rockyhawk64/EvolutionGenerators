package me.rockyhawk.evolutiongenerators.generator;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import me.rockyhawk.evolutiongenerators.method.NBTEditor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ETag {
    EvolutionGenerators plugin = JavaPlugin.getPlugin(EvolutionGenerators.class);

    public final Location getLocation;
    private final EGenerator generator;
    public Item ent;

    public final String layout;

    public ETag(EGenerator gen) {
        this.getLocation = new Location(gen.getBlock().getWorld(), gen.getBlock().getX() + 0.5, gen.getBlock().getY() + 1, gen.getBlock().getZ() + 0.5);
        ItemStack item = plugin.nbt.addNBT(gen.getItem().getItemStack(), "EvolutionStationary", "EvolutionStationary");
        if(gen.getConfig().isSet("layout")){
            this.layout = gen.getConfig().getString("layout");
        }else{
            this.layout = plugin.config.getString("title.layout");
        }
        this.generator = gen;

        this.ent = gen.getBlock().getWorld().dropItem(getLocation, item);
        this.ent.setVelocity(new Vector(0, 0, 0));
        this.ent.setCustomName(plugin.rename(layout,gen.getAmount(),gen.getMaxAmount(),generator.getPercentage()));
        this.ent.setCustomNameVisible(true);
    }

    public void respawnEntity(){
        ItemStack item = plugin.nbt.addNBT(generator.getItem().getItemStack(), "EvolutionStationary", "EvolutionStationary");
        this.ent.remove();
        this.ent = generator.getBlock().getWorld().dropItem(getLocation, item);
        this.ent.setVelocity(new Vector(0, 0, 0));
        this.ent.setCustomName(plugin.rename(layout,generator.getAmount(),generator.getMaxAmount(),generator.getPercentage()));
        this.ent.setCustomNameVisible(true);
    }
}
