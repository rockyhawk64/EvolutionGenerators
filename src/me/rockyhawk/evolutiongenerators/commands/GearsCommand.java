package me.rockyhawk.evolutiongenerators.commands;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class GearsCommand implements CommandExecutor {
    private final EvolutionGenerators plugin;
    public GearsCommand(EvolutionGenerators pl) {
        this.plugin = pl;
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0){
            if(!(sender instanceof Player)) {
                sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.asPlayer")));
                return true;
            }
            if(!sender.hasPermission("egenerators.gears.balance.others")){
                sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                return true;
            }
            Player p = (Player)sender;
            String message = plugin.config.getString("gears.amount").replaceAll("%gears%",String.valueOf(plugin.gearsManager.getGears(p)));
            p.sendMessage(plugin.color(plugin.tag + message));
            return true;
        }
        if(args.length == 1) {
            if (!sender.hasPermission("egenerators.gears.balance.others")) {
                sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                return true;
            }
            Player p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.notPlayer")));
                return true;
            }
            String message = plugin.config.getString("gears.other").replaceAll("%gears%", String.valueOf(plugin.gearsManager.getGears(p))).replaceAll("%player%",p.getDisplayName());
            p.sendMessage(plugin.color(plugin.tag + message));
            return true;
        }
        if(args.length == 3) {
            if (!sender.hasPermission("egenerators.admin")) {
                sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.noperms")));
                return true;
            }
            if(args[0].equalsIgnoreCase("give")){
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) {
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.notPlayer")));
                    return true;
                }
                try {
                    plugin.gearsManager.addGears(p, Integer.parseInt(args[2]));
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("gears.give").replaceAll("%gears%", args[2]).replaceAll("%player%",p.getDisplayName())));
                }catch(Exception ignore){
                    ignore.printStackTrace();
                }
            }
            if(args[0].equalsIgnoreCase("remove")){
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) {
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("format.notPlayer")));
                    return true;
                }
                try {
                    plugin.gearsManager.removeGears(p, Integer.parseInt(args[2]));
                    sender.sendMessage(plugin.color(plugin.tag + plugin.config.getString("gears.remove").replaceAll("%gears%", args[2]).replaceAll("%player%",p.getDisplayName())));
                }catch(Exception ignore){
                    ignore.printStackTrace();
                }
            }
        }
        return true;
    }
}
