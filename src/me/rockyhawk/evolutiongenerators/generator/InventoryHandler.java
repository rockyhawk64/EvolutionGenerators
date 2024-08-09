package me.rockyhawk.evolutiongenerators.generator;

import me.rockyhawk.commandpanels.api.PanelClosedEvent;
import me.rockyhawk.commandpanels.api.PanelCommandEvent;
import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class InventoryHandler implements Listener {
    private final EvolutionGenerators plugin;
    public InventoryHandler(EvolutionGenerators pl) {
        this.plugin = pl;
    }

    //currently open generators
    private final HashMap<EGenerator,Player> openGenerators = new HashMap<>();
    public HashMap<Integer,Integer> generatorSlots = new HashMap<>(); //returns [slot,level required]
    public int generatorUpgradeSlot; //returns slot

    public void open(EGenerator generator, Player p){
        if(!isOpen(generator)) {
            generator.openGUI(p);
            openGenerators.put(generator, p);
        }
    }

    public void close(EGenerator generator){
        if(isOpen(generator)) {
            openGenerators.get(generator).closeInventory();
            openGenerators.remove(generator);
        }
    }

    //add an item to the generator
    public void addItem(EGenerator generator){
        if(isOpen(generator)) {
            ItemStack item = new ItemStack(generator.getItem().getItemStack());
            if(!isOpen(generator)){
                return;
            }
            openGenerators.get(generator).getOpenInventory().getTopInventory().addItem(item);
        }
    }

    public boolean isOpen(EGenerator generator){
        return openGenerators.containsKey(generator);
    }

    @EventHandler
    private void panelCommandEvent(PanelCommandEvent e){
        String[] message = e.getMessage().split("=");
        if(message[0].equalsIgnoreCase("GENERATOR_UPGRADE")){
            String genName = message[1];
            World gWorld = Bukkit.getWorld(genName.split("_")[0].replaceAll("%dash%", "_"));
            int x = Integer.parseInt(genName.split("_")[1]);
            int y = Integer.parseInt(genName.split("_")[2]);
            int z = Integer.parseInt(genName.split("_")[3]);
            Location loc = new Location(gWorld, x, y, z);

            EGenerator gen = plugin.generatorsList.get(loc);

            if(!plugin.gearsManager.payEconomy(e.getPlayer(),gen.getUpgrades().buyPriceNext())){
                e.getPlayer().sendMessage(plugin.color(plugin.tag + plugin.config.getString("gears.insufficient")));
                return;
            }

            gen.getUpgrades().addLevel();
            e.getPlayer().sendMessage(plugin.color(plugin.tag + plugin.config.getString("upgrade.upgraded")));

            for(Map.Entry<Integer, Integer> slot : generatorSlots.entrySet()){
                if(slot.getValue() == gen.getUpgrades().slotLevel){
                    e.getPlayer().getOpenInventory().setItem(slot.getKey(),new ItemStack(Material.AIR));
                }
            }
            e.getPlayer().closeInventory();
        }
    }

    @EventHandler
    private void panelCloseEvent(PanelClosedEvent e){
        if(e.getPanel().getConfig().contains("EvolutionGenerators_Location")){
            String genName = e.getPanel().getConfig().getString("EvolutionGenerators_Location");
            World gWorld = Bukkit.getWorld(genName.split("_")[0].replaceAll("%dash%", "_"));
            int x = Integer.parseInt(genName.split("_")[1]);
            int y = Integer.parseInt(genName.split("_")[2]);
            int z = Integer.parseInt(genName.split("_")[3]);
            Location loc = new Location(gWorld, x, y, z);

            EGenerator gen = plugin.generatorsList.get(loc);
            openGenerators.remove(gen);

            int totalAmount = 0;
            for(Map.Entry<Integer, Integer> slot : generatorSlots.entrySet()){
                ItemStack itm = e.getPlayer().getOpenInventory().getTopInventory().getItem(slot.getKey());
                if(itm == null || gen.getUpgrades().slotLevel < slot.getValue()){
                    continue;
                }
                if(!plugin.isIdentical(itm,gen.getItem().getItemStack())){
                    e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(),itm);
                    continue;
                }
                totalAmount += itm.getAmount();
            }
            gen.setAmount(totalAmount);
            gen.updateTag();
        }
    }
}
