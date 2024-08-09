package me.rockyhawk.evolutiongenerators;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.evolutiongenerators.commands.EGenCommand;
import me.rockyhawk.evolutiongenerators.commands.GearsCommand;
import me.rockyhawk.evolutiongenerators.completetabs.EgenTabComplete;
import me.rockyhawk.evolutiongenerators.completetabs.GearsTabComplete;
import me.rockyhawk.evolutiongenerators.gears.EconomyManager;
import me.rockyhawk.evolutiongenerators.generator.EGenerator;
import me.rockyhawk.evolutiongenerators.generator.EGeneratorLoader;
import me.rockyhawk.evolutiongenerators.generator.InventoryHandler;
import me.rockyhawk.evolutiongenerators.listeners.*;
import me.rockyhawk.evolutiongenerators.method.ItemDetector;
import me.rockyhawk.evolutiongenerators.method.NBTEditor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.logging.Level;

public class EvolutionGenerators extends JavaPlugin{
    //variables
    public YamlConfiguration config;
    public YamlConfiguration gears;
    public YamlConfiguration items;
    public String tag;

    //get custom classes
    public InventoryHandler invHandle = new InventoryHandler(this);
    public ItemDetector itemDetector = new ItemDetector(this);
    public EconomyManager gearsManager = new EconomyManager(this);
    public NBTEditor nbt = new NBTEditor(this);

    //generator list
    public HashMap<Location, EGenerator> generatorsList = new HashMap<>();

    @Override
    public void onEnable() {
        //load class resources
        Objects.requireNonNull(this.getCommand("gears")).setExecutor(new GearsCommand(this));
        Objects.requireNonNull(this.getCommand("gears")).setTabCompleter(new GearsTabComplete(this));
        Objects.requireNonNull(this.getCommand("egen")).setExecutor(new EGenCommand(this));
        Objects.requireNonNull(this.getCommand("egen")).setTabCompleter(new EgenTabComplete(this));
        this.getServer().getPluginManager().registerEvents(new OnBlockPlace(this), this);
        this.getServer().getPluginManager().registerEvents(new OnBlockBreak(this), this);
        this.getServer().getPluginManager().registerEvents(new OnPlayerInteract(this), this);
        this.getServer().getPluginManager().registerEvents(new OnItemEvents(this), this);
        this.getServer().getPluginManager().registerEvents(invHandle, this);

        //get config defaults
        this.config = loadConfig(new File(this.getDataFolder() + File.separator + "config.yml"),false);
        this.items = loadConfig(new File(this.getDataFolder() + File.separator + "items.yml"),false);
        this.gears = loadConfig(new File(this.getDataFolder() + File.separator + "gears.dat"),true);
        loadConfig(new File(this.getDataFolder() + File.separator + "GUI.yml"),false);
        loadConfig(new File(this.getDataFolder() + File.separator + "generators.dat"),true);

        //load plugin
        reloadPlugin();
        loadGenerators();

        //finish enabling plugin
        Bukkit.getConsoleSender().sendMessage( "[EvolutionGenerators]" + " Plugin Enabled!");

        //check config version
        if(config.isSet("config.version")){
            String latestConfigVersion = null;
            try {
                latestConfigVersion = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("config.yml"))).getString("config.version");
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE,"[Evolution Generators] Could not check config version!");
            }

            if(!config.getString("config.version").equalsIgnoreCase(latestConfigVersion)){
                Bukkit.getLogger().log(Level.SEVERE,"[Evolution Generators] Config version does not match the latest version!");
            }
        }
    }

    @Override
    public void onDisable() {
        saveGeneratorConfig(false);
        try {
            gears.save(new File(this.getDataFolder() + File.separator + "gears.dat"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void reloadPlugin(){
        this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));
        this.items = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "items.yml"));

        tag = config.getString("format.tag") + " ";
        gearsManager.reloadEconomy();

        invHandle.generatorSlots.clear();
        Panel newPanel = new Panel(new File(getDataFolder() + File.separator + "GUI.yml"),"EvolutionGenerators_Main");
        for(String slotTemp : newPanel.getConfig().getConfigurationSection("item").getKeys(false)) {
            //if the slot is for a generated item
            if (newPanel.getConfig().getString("item." + slotTemp + ".material").split("\\s")[0].equalsIgnoreCase("GENERATOR_ITEM")) {
                invHandle.generatorSlots.put(Integer.parseInt(slotTemp),Integer.parseInt(newPanel.getConfig().getString("item." + slotTemp + ".material").split("\\s")[1]));
                continue;
            }
            //if the slot contains the command to upgrade the generator
            if (newPanel.getConfig().getString("item." + slotTemp + ".material").split("\\s")[0].equalsIgnoreCase("GENERATOR_UPGRADE")) {
                invHandle.generatorUpgradeSlot = Integer.parseInt(slotTemp);
            }
        }
    }

    public void loadGenerators(){
        generatorsList.clear();
        YamlConfiguration generators = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "generators.dat"));
        new BukkitRunnable(){
            @Override
            public void run(){
                try {
                    for (String genName : generators.getConfigurationSection("generators").getKeys(false)) {
                        ConfigurationSection temp = generators.getConfigurationSection("generators." + genName);

                        World gWorld = Bukkit.getWorld(genName.split("_")[0].replaceAll("%dash%", "_"));
                        int x = Integer.parseInt(genName.split("_")[1]);
                        int y = Integer.parseInt(genName.split("_")[2]);
                        int z = Integer.parseInt(genName.split("_")[3]);
                        Location loc = new Location(gWorld, x, y, z);

                        //make generator loader
                        EGeneratorLoader gen = new EGeneratorLoader(temp.getString("ID"), loc.getBlock(), UUID.fromString(temp.getString("owner")));
                        gen.setItemAmount(temp.getInt("amount"));
                        gen.setPercentage(temp.getInt("percent"));
                        gen.setLevel(temp.getInt("level"));
                        gen.setLocked(temp.getBoolean("locked"));

                        //make generator
                        gen.makeGenerator();
                    }
                }catch(NullPointerException ignore){
                    this.cancel();
                    //empty list
                }
            }
        }.runTask(this);
    }

    public void saveGeneratorConfig(boolean reload){
        YamlConfiguration generators = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "generators.dat"));
        generators.set("generators", null);
        for(Map.Entry<Location,EGenerator> tempGen : new HashSet<>(generatorsList.entrySet())){
            generators.set("generators." + tempGen.getValue().getName() + ".ID", tempGen.getValue().getID());
            generators.set("generators." + tempGen.getValue().getName() + ".amount", tempGen.getValue().getAmount());
            generators.set("generators." + tempGen.getValue().getName() + ".owner", tempGen.getValue().getOwner().toString());
            generators.set("generators." + tempGen.getValue().getName() + ".percent", tempGen.getValue().getPercentage());
            generators.set("generators." + tempGen.getValue().getName() + ".level", tempGen.getValue().getUpgrades().generatorLevel);
            generators.set("generators." + tempGen.getValue().getName() + ".locked", tempGen.getValue().isLocked());
            tempGen.getValue().delete(false);
        }
        try {
            generators.save(new File(this.getDataFolder() + File.separator + "generators.dat"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        if(reload){
            //load the panels again
            loadGenerators();
        }
    }

    //this will load config files from internal resources
    public YamlConfiguration loadConfig(File configFile, boolean copyDefaults){
        YamlConfiguration tmp = null;
        if (!configFile.exists()) {
            //generate a new config file from internal resources
            try {
                FileConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource(configFile.getName())));
                configFileConfiguration.save(configFile);
                tmp = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + configFile.getName()));
            } catch (IOException var11) {
                Bukkit.getConsoleSender().sendMessage("[EvolutionGenerators]" + ChatColor.RED + " WARNING: Could not save the config file:" + configFile.getName());
            }
        }else if(copyDefaults){
            //check if the config file has any missing elements
            try {
                tmp = YamlConfiguration.loadConfiguration(configFile);
                YamlConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource(configFile.getName())));
                tmp.addDefaults(configFileConfiguration);
                tmp.options().copyDefaults(true);
                tmp.save(new File(this.getDataFolder() + File.separator + configFile.getName()));
            } catch (IOException var10) {
                Bukkit.getConsoleSender().sendMessage("[EvolutionGenerators]" + ChatColor.RED + " WARNING: Could not save the config file:" + configFile.getName());
            }
        }
        return tmp;
    }

    //If you don't want to use PAPI
    public String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    //Send help message to command sender
    public void helpMessage(CommandSender sender){
        String tag = config.getString("format.tag");
        if(sender.hasPermission("egenerators.help")){
            sender.sendMessage(color(tag));
            sender.sendMessage(ChatColor.GREEN + "/egen help " + ChatColor.GRAY + "Show list of commands");
            if(sender.hasPermission("egenerators.version")) {
                sender.sendMessage(ChatColor.GREEN + "/egen version " + ChatColor.GRAY + "Show the current plugin version");
            }
            if(sender.hasPermission("egenerators.reload")) {
                sender.sendMessage(ChatColor.GREEN + "/egen reload " + ChatColor.GRAY + "Reloads the plugin config");
                sender.sendMessage(ChatColor.GREEN + "/egen restart " + ChatColor.GRAY + "Reloads config and reloads generators");
            }
            if(sender.hasPermission("egenerators.list")) {
                sender.sendMessage(ChatColor.GREEN + "/egen list " + ChatColor.GRAY + "Get a list of your generators");
            }
            if(sender.hasPermission("egenerators.list.others")) {
                sender.sendMessage(ChatColor.GREEN + "/egen list <player> " + ChatColor.GRAY + "Get a list of another players generators");
            }
            if(sender.hasPermission("egenerators.item")) {
                sender.sendMessage(ChatColor.GREEN + "/egen item <generator type> [player] " + ChatColor.GRAY + "Give player a generator");
            }
            if(sender.hasPermission("egenerators.admin")) {
                sender.sendMessage(ChatColor.GREEN + "/egen place <generator type> <x> <y> <z> <world> [player] " + ChatColor.GRAY + "Force place a generator in a location");
            }
            if(sender.hasPermission("egenerators.lock.on")) {
                sender.sendMessage(ChatColor.GREEN + "/egen lock " + ChatColor.GRAY + "Lock target generator looked at");
            }
            if(sender.hasPermission("egenerators.lock.off")) {
                sender.sendMessage(ChatColor.GREEN + "/egen unlock " + ChatColor.GRAY + "Unlock target generator looked at");
            }
            if(sender.hasPermission("egenerators.gears.balance")) {
                sender.sendMessage(ChatColor.GREEN + "/gears " + ChatColor.GRAY + "How many gears you currently have");
            }
            if(sender.hasPermission("egenerators.gears.balance.other")) {
                sender.sendMessage(ChatColor.GREEN + "/gears <player> " + ChatColor.GRAY + "How many Gears someone else has");
            }
            if(sender.hasPermission("egenerators.admin")) {
                sender.sendMessage(ChatColor.GREEN + "/gears give <player> <amount> " + ChatColor.GRAY + "Give Gears to a specific player");
                sender.sendMessage(ChatColor.GREEN + "/gears remove <player> <amount> " + ChatColor.GRAY + "Remove Gears from a specific player");
            }
        }else{
            sender.sendMessage(color(tag + config.getString("format.noperms")));
        }
    }

    public String rename(String in, int amount, int max, int percentage){
        String out = in;
        out = out.replaceAll("%percentage%", String.valueOf(percentage));
        out = out.replaceAll("%contents%", String.valueOf(amount));
        out = out.replaceAll("%max%", String.valueOf(max));
        out = color(out);
        return out;
    }

    public boolean isGenerator(Location blo){
        return generatorsList.containsKey(blo);
    }

    //checks for visual aspects of an item to see if they are the same
    public boolean isIdentical(ItemStack one, ItemStack two){
        //check material
        if (one.getType() != two.getType()) {
            return false;
        }
        //check for name
        try {
            if (!one.getItemMeta().getDisplayName().equals(two.getItemMeta().getDisplayName())) {
                if(one.getItemMeta().hasDisplayName()) {
                    return false;
                }
            }
        }catch(Exception ignore){}
        //check for lore
        try {
            if (!one.getItemMeta().getLore().equals(two.getItemMeta().getLore())) {
                if(one.getItemMeta().hasLore()) {
                    return false;
                }
            }
        }catch(Exception ignore){}
        //check for enchantments
        if(one.getEnchantments() == two.getEnchantments()){
            return one.getEnchantments().isEmpty();
        }
        return true;
    }

    public Reader getReaderFromStream(InputStream initialStream) throws IOException {
        //this reads the encrypted resource files in the jar file
        byte[] buffer = IOUtils.toByteArray(initialStream);
        return new CharSequenceReader(new String(buffer));
    }
}