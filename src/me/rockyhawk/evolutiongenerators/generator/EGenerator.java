package me.rockyhawk.evolutiongenerators.generator;

import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.evolutiongenerators.gears.EconomyManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class EGenerator {
    EvolutionGenerators plugin = JavaPlugin.getPlugin(EvolutionGenerators.class);

    private final Block block;
    private final UUID owner;
    private final ConfigurationSection config;

    private final String ID;
    private final String configName;
    private int itemAmount;
    private BigDecimal percentage;
    private boolean locked;
    public GeneratorUpgrades upgradeLevel;

    public BukkitTask load;

    private final EItemStack genItem;
    private ETag tag;

    public EGenerator(String generator, Block loc, int amount, int percent, int level, boolean lock, UUID uuid){
        //load generator chunk if not loaded
        loc.getChunk().setForceLoaded(true);

        //assign variables
        this.config = plugin.config.getConfigurationSection("generators." + generator);
        this.itemAmount = amount;
        this.locked = lock;
        this.upgradeLevel = new GeneratorUpgrades(this,level);
        this.percentage = new BigDecimal(percent);
        this.block = loc;
        this.genItem = new EItemStack(generator, uuid, 0, true);
        this.configName = loc.getWorld().getName().replaceAll("_","%dash%") + "_" + loc.getX() + "_" + loc.getY() + "_" + loc.getZ();
        this.owner = uuid;
        this.ID = generator;

        this.createTag();
        tag.ent.teleport(tag.getLocation);
        tag.ent.setCustomName(plugin.rename(tag.layout,itemAmount, getMaxAmount(), this.getPercentage()));

        restartRunnable();

        placeBlock();
        plugin.generatorsList.put(loc.getLocation(),this);

        if(config.isSet("sounds.placed")){
            playSound(Sound.valueOf(config.getString("sounds.placed")));
        }
        if(config.isSet("particles.placed")){
            spawnParticle(Particle.valueOf(config.getString("particles.placed")));
        }
    }

    public void restartRunnable(){
        if(load != null){
            load.cancel();
        }
        final EGenerator gen = this;
        this.load = new BukkitRunnable() {
            @Override
            public void run() {
                //do math
                if(isInRange(owner)) {
                    if (itemAmount < getMaxAmount()) {
                        percentage = percentage.add(BigDecimal.valueOf(1));
                        if (percentage.equals(BigDecimal.valueOf(100))) {
                            if(config.contains("sounds.generate")){
                                new BukkitRunnable(){
                                    @Override
                                    public void run(){
                                        playSound(Sound.valueOf(config.getString("sounds.generate")));
                                    }
                                }.runTask(plugin);
                            }
                            if(config.contains("particles.generate")){
                                new BukkitRunnable(){
                                    @Override
                                    public void run(){
                                        spawnParticle(Particle.valueOf(config.getString("particles.generate")));
                                    }
                                }.runTask(plugin);
                            }
                            percentage = new BigDecimal(0);
                            itemAmount += 1;
                            plugin.invHandle.addItem(gen);
                        }
                    } else {
                        percentage = new BigDecimal(0);
                        itemAmount = getMaxAmount();
                    }
                }

                //check if entity is dead
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        if(tag.ent.isDead()){
                            tag.respawnEntity();
                        }
                    }
                }.runTask(plugin);

                //do entity
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        tag.ent.teleport(tag.getLocation);
                        tag.ent.setCustomName(plugin.rename(tag.layout,itemAmount, getMaxAmount(), getPercentage()));
                    }
                }.runTask(plugin);
            }
        }.runTaskTimerAsynchronously(this.plugin, this.upgradeLevel.generatorSpeed(),this.upgradeLevel.generatorSpeed());
    }

    public void delete(boolean dropItem){
        load.cancel();
        tag.ent.remove();
        plugin.invHandle.close(this);
        block.setType(Material.AIR);
        block.getChunk().setForceLoaded(false);
        plugin.generatorsList.remove(block.getLocation());

        if(dropItem) {
            block.getWorld().dropItem(block.getLocation(), new EItemStack(ID, this.owner, upgradeLevel.generatorLevel, false).getItemStack());
            if(this.itemAmount != 0) {
                ItemStack contents = this.genItem.getItemStack();
                contents.setAmount(this.itemAmount);
                block.getWorld().dropItem(block.getLocation(), contents);
            }
        }

        if(config.isSet("sounds.removed")){
            playSound(Sound.valueOf(config.getString("sounds.removed")));
        }
        if(config.isSet("particles.removed")){
            spawnParticle(Particle.valueOf(config.getString("particles.removed")));
        }
    }

    public void openGUI(Player p){
        Panel newPanel = new Panel(new File(plugin.getDataFolder() + File.separator + "GUI.yml"),"EvolutionGenerators_Main");
        ConfigurationSection guiConfig = newPanel.getConfig();
        guiConfig.set("EvolutionGenerators_Location",this.configName);
        guiConfig.set("title", Bukkit.getOfflinePlayer(owner).getName() + "'s" + " Generator");
        guiConfig.set("empty",Material.GLASS_PANE.toString());

        //set generator slots to placeables
        for(Map.Entry<Integer, Integer> slot : plugin.invHandle.generatorSlots.entrySet()){
            if(slot.getValue() <= upgradeLevel.slotLevel) {
                guiConfig.set("item." + slot.getKey() + ".material",Material.AIR.toString());
                List<String> temp = new ArrayList<>();
                temp.add("placeable");
                guiConfig.set("item." + slot.getKey() + ".itemType", temp);
            }else{
                guiConfig.set("item." + slot.getKey() + ".material",Material.GLASS_PANE.toString());
                guiConfig.set("item." + slot.getKey() + ".name","&f");
            }
        }

        //set the generator upgrade item command
        if(config.isSet("upgrades")) {
            List<String> temp = new ArrayList<>();
            if (upgradeLevel.hasNextUpgrade()) {
                temp.add("event=" + " GENERATOR_UPGRADE=" + this.configName);
                guiConfig.set("item." + plugin.invHandle.generatorUpgradeSlot + ".commands", temp);
                guiConfig.set("item." + plugin.invHandle.generatorUpgradeSlot + ".material", Material.GLOWSTONE_DUST.toString());
                guiConfig.set("item." + plugin.invHandle.generatorUpgradeSlot + ".name", "&eUpgrade Generator");
                List<String> lore = new ArrayList<>();
                lore.add("&6Current Level: &f" + upgradeLevel.generatorLevel);

                if(plugin.gearsManager.economy == EconomyManager.EconomyType.GEARS) {
                    lore.add("&6Upgrade Cost: &f" + upgradeLevel.buyPriceNext() + " Gears");
                }else{
                    lore.add("&6Upgrade Cost: &f$" + upgradeLevel.buyPriceNext());
                }

                guiConfig.set("item." + plugin.invHandle.generatorUpgradeSlot + ".lore", lore);
            } else {
                guiConfig.set("item." + plugin.invHandle.generatorUpgradeSlot + ".material", Material.GLOWSTONE_DUST.toString());
                guiConfig.set("item." + plugin.invHandle.generatorUpgradeSlot + ".name", "&eFully Upgraded");
                List<String> lore = new ArrayList<>();
                lore.add("&6Current Level: &f" + upgradeLevel.generatorLevel);
                guiConfig.set("item." + plugin.invHandle.generatorUpgradeSlot + ".lore", lore);
            }
        }else{
            guiConfig.set("item." + plugin.invHandle.generatorUpgradeSlot + ".material", Material.GLASS_PANE.toString());
            guiConfig.set("item." + plugin.invHandle.generatorUpgradeSlot + ".name", "&f");
        }

        //open the gui
        new Panel(guiConfig,"Panel").open(p, PanelPosition.Top);
        ItemStack itm = genItem.getItemStack();
        itm.setAmount(this.itemAmount);

        //place items into generator slots
        ItemStack item = genItem.getItemStack();
        item.setAmount(itemAmount);
        p.getOpenInventory().getTopInventory().addItem(item);
    }

    private boolean isInRange(UUID player){
        if(player == null){
            return true;
        }
        int range;
        if(this.config.isSet("generate-distance")){
            range = this.config.getInt("generate-distance");
        }else{
            range = plugin.config.getInt("config.generate-distance");
        }
        if(range == -1){
            return true;
        }

        if(Bukkit.getPlayer(owner) == null){
            return false;
        }
        Player p = Bukkit.getPlayer(player);
        if(p.getWorld() != this.block.getWorld()){
            return false;
        }

        Chunk pl = p.getLocation().getChunk();
        Chunk ge = block.getLocation().getChunk();
        if(ge.getX() - pl.getX() <= range && ge.getX() - pl.getX() >= -range){
            return ge.getZ() - pl.getZ() <= range && ge.getZ() - pl.getZ() >= -range;
        }

        return false;
    }

    public int getMaxAmount(){
        int size = 0;
        for(Map.Entry<Integer, Integer> slot : plugin.invHandle.generatorSlots.entrySet()){
            if(upgradeLevel.slotLevel >= slot.getValue()){
                size += 1;
            }
        }
        return (size*genItem.getItemStack().getMaxStackSize());
    }

    public void placeBlock(){
        block.setType(Material.matchMaterial(this.config.getString("block")));
    }

    public void createTag(){
        this.tag = new ETag(this);
    }

    public String getName(){
        return this.configName;
    }

    public Block getBlock(){
        return this.block;
    }

    public ConfigurationSection getConfig() {
        return this.config;
    }

    public boolean isLocked(){
        return locked;
    }

    public void setLocked(boolean lock){
        this.locked = lock;
    }

    public int getPercentage() {
        return this.percentage.intValue();
    }

    public int getAmount(){
        return this.itemAmount;
    }
    public void setAmount(int amount){
        this.itemAmount = amount;
    }

    public String getID(){
        return this.ID;
    }

    public EItemStack getItem(){
        return this.genItem;
    }

    public void updateTag(){
        tag.ent.setCustomName(plugin.rename(tag.layout,this.itemAmount,getMaxAmount(),getPercentage()));
    }

    public UUID getOwner(){
        return owner;
    }

    public GeneratorUpgrades getUpgrades(){
        return upgradeLevel;
    }

    public void playSound(Sound sound){
        //play 1 sound
        block.getLocation().getWorld().playSound(tag.getLocation,sound,1F,1F);
    }

    public void spawnParticle(Particle particle){
        //spawn 5 particles
        block.getLocation().getWorld().spawnParticle(particle, tag.getLocation, 5);
    }

    public EGeneratorLoader toLoader(){
        EGeneratorLoader loader = new EGeneratorLoader(ID, block, owner);
        loader.setLevel(upgradeLevel.generatorLevel);
        loader.setPercentage(percentage.intValue());
        loader.setLocked(locked);
        loader.setItemAmount(itemAmount);
        return loader;
    }
}
