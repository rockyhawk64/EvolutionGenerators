package me.rockyhawk.evolutiongenerators.gears;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final EvolutionGenerators plugin;
    public EconomyManager(EvolutionGenerators pl) {
        this.plugin = pl;
    }

    public EconomyType economy = EconomyType.GEARS;
    private Economy econ = null;

    public enum EconomyType{
        GEARS,
        VAULT
    }

    public int getGears(Player p){
        hasData(p);
        return plugin.gears.getInt("players." + p.getUniqueId());
    }

    public void addGears(Player p, int amount){
        hasData(p);
        int math = plugin.gears.getInt("players." + p.getUniqueId()) + amount;
        plugin.gears.set("players." + p.getUniqueId(), math);
    }

    public void removeGears(Player p, int amount){
        hasData(p);
        int math = plugin.gears.getInt("players." + p.getUniqueId()) - amount;
        if(math < 0){
            math = 0;
        }
        plugin.gears.set("players." + p.getUniqueId(), math);
    }

    //for all economies
    public boolean payEconomy(Player p, int amount){
        if(economy == EconomyType.GEARS) {
            hasData(p);
            if (getGears(p) >= amount) {
                removeGears(p, amount);
                return true;
            }
        }
        if(economy == EconomyType.VAULT){
            if (econ.getBalance(p) >= amount) {
                econ.withdrawPlayer(p,amount);
                return true;
            }
        }
        return false;
    }

    //for all economies
    public void reloadEconomy(){
        economy = EconomyType.GEARS;
        if(plugin.config.isSet("config.economy-type")){
            economy = EconomyType.valueOf(plugin.config.getString("config.economy-type"));
        }
        if(economy == EconomyType.VAULT){
            setupVault();
        }
    }

    private void hasData(Player p){
        if(!plugin.gears.isSet("players." + p.getUniqueId())){
            plugin.gears.set("players." + p.getUniqueId(),0);
        }
    }

    private void setupVault() {
        if(economy == EconomyType.VAULT) {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
                return;
            }
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return;
            }
            econ = rsp.getProvider();
        }
    }
}
