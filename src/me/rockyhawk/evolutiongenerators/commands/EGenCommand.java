package me.rockyhawk.evolutiongenerators.commands;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import me.rockyhawk.evolutiongenerators.generator.EGenerator;
import me.rockyhawk.evolutiongenerators.generator.ESpawnerItem;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class EGenCommand implements CommandExecutor {
    private final EvolutionGenerators plugin;
    public EGenCommand(EvolutionGenerators pl) {
        this.plugin = pl;
    }
    @EventHandler
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0){
            plugin.helpMessage(sender);
        }
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("list")){
                //list generators of player
                if(!(sender instanceof Player)) {
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.asPlayer")));
                    return true;
                }
                if(sender.hasPermission("egenerators.list")){
                    sender.sendMessage(plugin.color(plugin.tag));
                    Player p = (Player)sender;
                    sendGeneratorList(sender,p.getUniqueId(),1);
                }
            }
            if(args[0].equalsIgnoreCase("help")){
                if(!sender.hasPermission("egenerators.help")){
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                    return true;
                }
                //send help text
                plugin.helpMessage(sender);
                return true;
            }
            if(args[0].equalsIgnoreCase("version")){
                //version Text
                if(sender.hasPermission("egenerators.version")){
                    sender.sendMessage(plugin.color(plugin.tag));
                    sender.sendMessage(ChatColor.GREEN + "Version " + ChatColor.GRAY + plugin.getDescription().getVersion());
                    sender.sendMessage(ChatColor.GREEN + "Developer " + ChatColor.GRAY + "RockyHawk");
                    sender.sendMessage(ChatColor.GREEN + "Command " + ChatColor.GRAY + "/egen");
                }else{
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                }
                return true;
            }
            if(args[0].equalsIgnoreCase("reload")){
                //Reload Text
                if(sender.hasPermission("egenerators.reload")){
                    try {
                        plugin.reloadPlugin();
                    }catch (Exception e){
                        sender.sendMessage(plugin.color(plugin.tag + ChatColor.RED + "Could not Reload."));
                    }
                    sender.sendMessage(plugin.color(plugin.tag + ChatColor.GREEN + "Reloaded."));
                }else{
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                }
                return true;
            }
            if(args[0].equalsIgnoreCase("restart")){
                //reload plus reloading generators
                if(sender.hasPermission("egenerators.reload")){
                    try {
                        plugin.reloadPlugin();
                        plugin.saveGeneratorConfig(true);
                    }catch (Exception e){
                        sender.sendMessage(plugin.color(plugin.tag + ChatColor.RED + "Could not Restart."));
                    }
                    sender.sendMessage(plugin.color(plugin.tag + ChatColor.GREEN + "Restarted."));
                }else{
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                }
                return true;
            }
            if(args[0].equalsIgnoreCase("lock")){
                //lock generator
                if(!(sender instanceof Player)) {
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.asPlayer")));
                    return true;
                }
                if(sender.hasPermission("egenerators.lock.on")){
                    Player p = (Player)sender;
                    Location loc = p.getTargetBlock(null,10).getLocation();
                    if(plugin.isGenerator(loc)){
                        if(plugin.generatorsList.get(loc).isLocked()){
                            sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("lock.already-locked")));
                            return true;
                        }
                        if(plugin.generatorsList.get(loc).getOwner().equals(p.getUniqueId()) || p.hasPermission("egenerators.admin")){
                            plugin.generatorsList.get(loc).setLocked(true);
                            sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("lock.locked")));
                        }else{
                            sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                        }
                        return true;
                    }
                }else{
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                }
                return true;
            }
            if(args[0].equalsIgnoreCase("unlock")){
                //unlock generator
                if(!(sender instanceof Player)) {
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.asPlayer")));
                    return true;
                }
                if(sender.hasPermission("egenerators.lock.off")){
                    Player p = (Player)sender;
                    Location loc = p.getTargetBlock(null,10).getLocation();
                    if(plugin.isGenerator(loc)){
                        if(!plugin.generatorsList.get(loc).isLocked()){
                            sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("lock.already-unlocked")));
                            return true;
                        }
                        if(plugin.generatorsList.get(loc).getOwner().equals(p.getUniqueId()) || p.hasPermission("egenerators.admin")){
                            plugin.generatorsList.get(loc).setLocked(false);
                            sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("lock.unlocked")));
                        }else{
                            sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                        }
                        return true;
                    }
                }else{
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                }
                return true;
            }
        }
        if(args.length == 2){
            if(args[0].equalsIgnoreCase("list")){
                //list generators of player
                if(!(sender instanceof Player)) {
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.asPlayer")));
                    return true;
                }
                if(sender.hasPermission("egenerators.list")){
                    sender.sendMessage(plugin.color(plugin.tag));
                    Player p = (Player)sender;
                    UUID uuid;
                    int page;
                    try{
                        page = Integer.parseInt(args[1]);
                        uuid = p.getUniqueId();
                    }catch (NumberFormatException ignore){
                        uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                        page = 1;
                    }
                    sendGeneratorList(sender,uuid,page);
                }
            }
            if(args[0].equalsIgnoreCase("item")){
                if(!(sender instanceof Player)) {
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.asPlayer")));
                    return true;
                }
                Player p = ((Player) sender).getPlayer();
                //give player the item
                if(sender.hasPermission("egenerators.item")){
                    try {
                        p.getInventory().addItem(new ESpawnerItem(args[1],1,p.getUniqueId()).getItemStack());
                    }catch(Exception itemError){
                        p.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.nogen")));
                    }
                }else{
                    p.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                }
                return true;
            }
        }
        if(args.length == 3){
            if(args[0].equalsIgnoreCase("list")){
                //list generators of player
                if(sender.hasPermission("egenerators.list.others")){
                    OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                    sender.sendMessage(plugin.color(plugin.tag));
                    int page;
                    try{
                        page = Integer.parseInt(args[2]);
                    }catch (NumberFormatException ignore){
                        page = 1;
                    }
                    sendGeneratorList(sender,p.getUniqueId(),page);
                }
            }
            if(args[0].equalsIgnoreCase("item")){
                Player ap = Bukkit.getPlayer(args[2]);
                //give player the item
                if(sender.hasPermission("egenerators.item")){
                    try{
                        ap.getInventory().addItem(new ESpawnerItem(args[1],1,ap.getUniqueId()).getItemStack());
                        sender.sendMessage(plugin.color(plugin.tag + ChatColor.GRAY + "Gave item to " + ChatColor.GREEN + ap.getDisplayName()));
                    }catch(Exception itemError){
                        sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.notPlayer")));
                    }
                }else{
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                }
                return true;
            }
        }
        if(args.length == 6){
            if(args[0].equalsIgnoreCase("place")){
                if(!sender.hasPermission("egenerators.admin")){
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                    return true;
                }
                if(!(sender instanceof Player)) {
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.asPlayer")));
                    return true;
                }
                int x = Integer.parseInt(args[2]);
                int y = Integer.parseInt(args[3]);
                int z = Integer.parseInt(args[4]);
                Location loc = new Location(Bukkit.getWorld(args[5]),x,y,z);
                Player p = (Player)sender;
                new EGenerator(args[1], loc.getBlock(), 0, 0, 1, false, p.getUniqueId());
                p.sendMessage(plugin.color(plugin.tag + "&aGenerator has been placed!"));
            }
        }
        if(args.length == 7){
            if(args[0].equalsIgnoreCase("place")){
                if(!sender.hasPermission("egenerators.admin")){
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                    return true;
                }
                int x = Integer.parseInt(args[2]);
                int y = Integer.parseInt(args[3]);
                int z = Integer.parseInt(args[4]);
                Location loc = new Location(Bukkit.getWorld(args[5]),x,y,z);

                if(plugin.isGenerator(loc)){
                    return true;
                }

                new EGenerator(args[1], loc.getBlock(), 0, 0, 1, false, Bukkit.getOfflinePlayer(args[6]).getUniqueId());
                sender.sendMessage(plugin.color(plugin.tag + "&aGenerator has been placed!"));
            }
        }
        return true;
    }

    public void sendGeneratorList(CommandSender sender, UUID uuid, int page){
        boolean empty = true;
        int count = 0;
        int tempPage = 1;
        for(EGenerator gen : plugin.generatorsList.values()){
            if(gen.getOwner().equals(uuid)) {
                count += 1;
                if(count > 8){
                    count = 0;
                    tempPage += 1;
                }
                if(tempPage < page){
                    continue;
                }else if(tempPage > page){
                    sender.sendMessage(plugin.color(ChatColor.AQUA + "For more results visit page " + tempPage));
                    return;
                }
                empty = false;
                sender.sendMessage(ChatColor.GREEN + gen.getID() + ": " + ChatColor.GRAY + gen
                        .getName()
                        .replaceAll("_", " ")
                        .replaceAll("%dash%", "_"));
            }
        }
        if(empty){
            sender.sendMessage(plugin.color(plugin.config.getString("format.empty-list")));
        }
    }
}