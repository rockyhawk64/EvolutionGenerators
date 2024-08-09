package me.rockyhawk.evolutiongenerators.method;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;

public class ItemDetector {
    private final EvolutionGenerators plugin;
    public ItemDetector(EvolutionGenerators pl) {
        this.plugin = pl;
    }

    //return false if missing an item
    public boolean checkItemsExist(String iden){
        if(!plugin.items.isSet("generated-items." + iden)){
            return false;
        }
        return plugin.items.isSet("spawner-blocks." + iden);
    }
}
