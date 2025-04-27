package com.tiviacz.warriorrage.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface IRage {
    void startRage();

    boolean canStartRage();

    int getRemainingRageDuration();

    int getCurrentKillCount();

    void addKill(int count);

    void decreaseRageDuration();

    void removeRageEffects();

    void refreshRageDuration();

    float getHurtAmount();

    void addHurtAmount(float s);

    void setHurtAmount(float s);

    float getDamageAmount();

    void addDamageAmount(float s);

    void setDamageAmount(float s);

    void setKillCount(int count);

    void addMultiplier(int s);

    void setMultiplier(int s);

    int getMultiplier();

    float getCapMultiplier(int multiplier);

    float getDmgMultiplier(int multiplier);

    void setRageDuration(int time);

    void synchronise();

    void synchroniseToOthers(Player player);

    CompoundTag saveTag();

    void loadTag(CompoundTag compoundTag);
}
