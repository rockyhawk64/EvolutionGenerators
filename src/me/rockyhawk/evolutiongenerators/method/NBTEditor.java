package me.rockyhawk.evolutiongenerators.method;

import me.rockyhawk.evolutiongenerators.EvolutionGenerators;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class NBTEditor {
    EvolutionGenerators plugin;
    public NBTEditor(EvolutionGenerators pl) {
        this.plugin = pl;
    }
    //PersistentDataContainer is 1.14+

    public ItemStack addNBT(ItemStack item, String key, String value){
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
        item.setItemMeta(itemMeta);
        return item;
    }

    public String getNBT(ItemStack item, String key){
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        ItemMeta itemMeta = item.getItemMeta();
        return itemMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
    }

    public boolean hasNBT(ItemStack item, String key){
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        ItemMeta itemMeta = item.getItemMeta();
        return itemMeta.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING);
    }
}
