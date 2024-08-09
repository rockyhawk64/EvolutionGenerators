package me.rockyhawk.evolutiongenerators.generator;

public class GeneratorUpgrades {

    public int speedIncrease = 0;
    public int slotLevel = 1;
    public int generatorLevel;
    public EGenerator generator;

    GeneratorUpgrades(EGenerator gen, int level){
        this.generatorLevel = level;
        this.generator = gen;
        refreshUpgrades();
    }

    //reloads upgrades
    public void refreshUpgrades(){
        for(int i = 0; i <= this.generatorLevel; i++){
            if(generator.getConfig().isSet("upgrades." + i + ".slots-increase")){
                slotLevel = slotLevel + generator.getConfig().getInt("upgrades." + i + ".slots-increase");
            }
            if(generator.getConfig().isSet("upgrades." + i + ".speed-increase")){
                speedIncrease = speedIncrease + generator.getConfig().getInt("upgrades." + i + ".speed-increase");
            }
        }
    }

    //price for next level
    public int buyPriceNext(){
        if(generator.getConfig().isSet("upgrades." + (generatorLevel+1) + ".price")){
            return generator.getConfig().getInt("upgrades." + (generatorLevel+1) + ".price");
        }
        return 0;
    }

    public boolean hasNextUpgrade(){
        return generator.getConfig().isSet("upgrades." + (generatorLevel+1));
    }

    //add a level to the generator
    public void addLevel(){
        generatorLevel += 1;
        if(generator.getConfig().isSet("upgrades." + generatorLevel + ".slots-increase")){
            slotLevel = slotLevel + generator.getConfig().getInt("upgrades." + generatorLevel + ".slots-increase");
        }
        if(generator.getConfig().isSet("upgrades." + generatorLevel + ".speed-increase")){
            speedIncrease = speedIncrease + generator.getConfig().getInt("upgrades." + generatorLevel + ".speed-increase");
        }
        generator.restartRunnable();
    }

    public int generatorSpeed(){
        int speed = generator.getConfig().getInt("speed") - speedIncrease;
        if(speed <= 0){
            speed = 1;
        }
        return speed;
    }
}
