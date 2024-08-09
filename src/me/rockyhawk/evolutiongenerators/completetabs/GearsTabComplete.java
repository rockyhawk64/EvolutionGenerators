package me.rockyhawk.evolutiongenerators.completetabs;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class GearsTabComplete implements TabCompleter {
    private final EvolutionGenerators plugin;
    public GearsTabComplete(EvolutionGenerators pl) {
        this.plugin = pl;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("egenerators.admin") && sender instanceof Player) {
            ArrayList<String> tabComplete = new ArrayList<String>();
            if(args.length == 1){
                tabComplete.add("give");
                tabComplete.add("remove");
            }
            if(args.length == 2){
                //returns null so that player names appear
                return null;
            }
            if(args.length == 3){
                tabComplete.add("1");
                tabComplete.add("2");
                tabComplete.add("3");
                tabComplete.add("4");
                tabComplete.add("5");
            }
            return tabComplete;
        }
        return null;
    }
}