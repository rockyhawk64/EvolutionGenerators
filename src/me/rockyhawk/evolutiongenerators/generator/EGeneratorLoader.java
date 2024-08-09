package me.rockyhawk.evolutiongenerators.generator;

import org.bukkit.block.Block;

import java.util.UUID;

public class EGeneratorLoader {
    public Block block;
    public UUID owner;
    public String ID;
    public int itemAmount = 0;
    public int level = 1;
    public int percentage = 0;
    public boolean locked = false;

    public EGeneratorLoader(String generatorID, Block generatorBlock, UUID playerUUID){
        ID = generatorID;
        block = generatorBlock;
        owner = playerUUID;
    }
    public void setItemAmount(int blo){
        itemAmount = blo;
    }
    public void setPercentage(int blo){
        percentage = blo;
    }
    public void setLocked(Boolean blo){
        locked = blo;
    }
    public void setLevel(int lvl){
        level = lvl;
    }

    public void makeGenerator(){
        new EGenerator(ID, block, itemAmount, percentage, level, locked, owner);
    }
}
