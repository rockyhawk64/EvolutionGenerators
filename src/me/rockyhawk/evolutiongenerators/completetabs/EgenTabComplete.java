package me.rockyhawk.evolutiongenerators.completetabs;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import me.rockyhawk.evolutiongenerators.generator.EGeneratorLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class EgenTabComplete implements TabCompleter {
    private final EvolutionGenerators plugin;
    public EgenTabComplete(EvolutionGenerators pl) {
        this.plugin = pl;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && args.length == 1) {
            ArrayList<String> tabComplete = new ArrayList<>();
            tabComplete.add("help");
            if(sender.hasPermission("egenerators.version")) {
                tabComplete.add("version");
            }
            if(sender.hasPermission("egenerators.list")) {
                tabComplete.add("list");
            }
            if(sender.hasPermission("egenerators.reload")) {
                tabComplete.add("reload");
                tabComplete.add("restart");
            }
            if(sender.hasPermission("egenerators.item")) {
                tabComplete.add("item");
            }
            if(sender.hasPermission("egenerators.admin")) {
                tabComplete.add("place");
            }
            if(sender.hasPermission("egenerators.lock.on")) {
                tabComplete.add("lock");
            }
            if(sender.hasPermission("egenerators.lock.off")) {
                tabComplete.add("unlock");
            }
            return tabComplete;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("item")) {
                if(sender.hasPermission("egenerators.item")) {
                    return new ArrayList<>(Objects.requireNonNull(plugin.config.getConfigurationSection("generators")).getKeys(false));
                }
            }
            if (args[0].equalsIgnoreCase("place")) {
                if(sender.hasPermission("egenerators.admin")) {
                    return new ArrayList<>(Objects.requireNonNull(plugin.config.getConfigurationSection("generators")).getKeys(false));
                }
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("place") && sender instanceof Player) {
                if (sender.hasPermission("egenerators.admin")) {
                    ArrayList<String> tabComplete = new ArrayList<>();
                    Player p = (Player)sender;
                    tabComplete.add(String.valueOf((int)p.getLocation().getX()));
                    return tabComplete;
                }
            }
        }
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("place") && sender instanceof Player) {
                if (sender.hasPermission("egenerators.admin")) {
                    ArrayList<String> tabComplete = new ArrayList<>();
                    Player p = (Player)sender;
                    tabComplete.add(String.valueOf((int)p.getLocation().getY()));
                    return tabComplete;
                }
            }
        }
        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("place") && sender instanceof Player) {
                if (sender.hasPermission("egenerators.admin")) {
                    ArrayList<String> tabComplete = new ArrayList<>();
                    Player p = (Player)sender;
                    tabComplete.add(String.valueOf((int)p.getLocation().getZ()));
                    return tabComplete;
                }
            }
        }
        if (args.length == 6) {
            if (args[0].equalsIgnoreCase("place") && sender instanceof Player) {
                if (sender.hasPermission("egenerators.admin")) {
                    ArrayList<String> tabComplete = new ArrayList<>();
                    Player p = (Player)sender;
                    tabComplete.add(p.getLocation().getWorld().getName());
                    return tabComplete;
                }
            }
        }
        return null;
    }
}