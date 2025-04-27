package com.tiviacz.warriorrage.capability;

import com.tiviacz.warriorrage.WarriorRage;
import com.tiviacz.warriorrage.WarriorRageConfig;
import com.tiviacz.warriorrage.network.SyncRageCapabilityClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;

public class Rage implements IRage {
    private static final UUID RAGE = UUID.fromString("6e982d48-e5e6-11ec-8fea-0242ac120002");
    private static final UUID RAGE2 = UUID.fromString("6e982d48-e5e6-11ec-8fea-0242ac120003");

    public static final int DEFAULT_RAGE_DURATION = 20 * WarriorRageConfig.SERVER.rageDuration.get(); // 20ticks*10 = 10
                                                                                                      // seconds
    public static final int MAX_KILL_COUNT_CAP = WarriorRageConfig.SERVER.maxKillCountCap.get();
    public static final double BASE_MULTIPLIER = WarriorRageConfig.SERVER.bonusDamage.get();
    private int rageDuration = 0;
    private int killCount = 0;
    private float hurtNum = 0;
    private float damageNum = 0;
    private int multiplier = 0;
    private final Player playerEntity;

    public Rage(final Player playerEntity) {
        this.playerEntity = playerEntity;
    }

    public void addRageAttribute(UUID uuid, String pName, AttributeModifier.Operation pOperation,
            net.minecraft.world.entity.ai.attributes.Attribute pAttribute) {
        AttributeModifier attrModifier = new AttributeModifier(uuid, pName,
                calculateBonusDamage(this.killCount, BASE_MULTIPLIER), pOperation);
        AttributeInstance attribute = playerEntity.getAttribute(pAttribute);
        if (attribute.getModifier(uuid) != null) {
            if (attribute.getModifier(uuid).getAmount() != attrModifier.getAmount()) {
                attribute.removeModifier(uuid);
                attribute.addPermanentModifier(attrModifier);
            }
        } else {
            attribute.addPermanentModifier(attrModifier);
        }
    }

    @Override
    public void startRage() {
        // System.out.println("current kill count is" + this.killCount);
        // System.out.println("remaining rage time" + getRemainingRageDuration());
        // System.out.println("attack damage" +
        // playerEntity.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
        addRageAttribute(RAGE, "RageBonusDamage", AttributeModifier.Operation.MULTIPLY_TOTAL, Attributes.ATTACK_DAMAGE);
        addRageAttribute(RAGE2, "RageBonusAttackSpeed", AttributeModifier.Operation.ADDITION, Attributes.ATTACK_SPEED);
    }

    @Override
    public boolean canStartRage() {
        return this.killCount >= WarriorRageConfig.SERVER.minimalKillCount.get() && getRemainingRageDuration() > 0;
    }

    public double calculateBonusDamage(int killCount, double multiplier) {
        return WarriorRageConfig.SERVER.killIntervalBetweenNextBonus.get() == 0 ? killCount * multiplier
                : (killCount / WarriorRageConfig.SERVER.killIntervalBetweenNextBonus.get()) * multiplier;
    }

    @Override
    public int getRemainingRageDuration() {
        return this.rageDuration;
    }

    @Override
    public int getCurrentKillCount() {
        return this.killCount;
    }

    @Override
    public void addKill(int count) {
        if (this.killCount + count <= MAX_KILL_COUNT_CAP * getCapMultiplier(this.multiplier)) {
            this.killCount += count;
        }
        refreshRageDuration();
    }

    @Override
    public void decreaseRageDuration() {
        if (this.rageDuration > 0) {
            this.rageDuration -= 1;
        }
        if (rageDuration == 0) {
            this.killCount = 0;
            this.hurtNum = 0;
            this.damageNum = 0;
        }
    }

    @Override
    public void removeRageEffects() {
        if (playerEntity.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(RAGE) != null) {
            playerEntity.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(RAGE);
        }

        if (playerEntity.getAttribute(Attributes.ATTACK_SPEED).getModifier(RAGE2) != null) {
            playerEntity.getAttribute(Attributes.ATTACK_SPEED).removeModifier(RAGE2);
        }
    }

    @Override
    public void refreshRageDuration() {
        if (this.rageDuration < DEFAULT_RAGE_DURATION) {
            this.rageDuration = DEFAULT_RAGE_DURATION;
        }
    }

    @Override
    public float getHurtAmount() {
        return this.hurtNum;
    }

    @Override
    public void addHurtAmount(float s) {
        this.hurtNum += s;
    }

    @Override
    public void setHurtAmount(float s) {
        this.hurtNum = s;
    }

    @Override
    public float getDamageAmount() {
        return this.damageNum;
    }

    @Override
    public void addDamageAmount(float s) {
        this.damageNum += s;
    }

    @Override
    public void setDamageAmount(float s) {
        this.damageNum = s;
    }

    @Override
    public void setMultiplier(int s) {
        this.multiplier = s;
    }

    @Override
    public int getMultiplier() {
        return this.multiplier;
    }

    @Override
    public void addMultiplier(int s) {
        this.multiplier += s;
    }

    @Override
    public float getCapMultiplier(int multiplier) {
        List<? extends Double> capList = WarriorRageConfig.SERVER.capMultiplier.get();
        int len = capList.size();
        float finalMult = 1;

        for (int i = 0; i < len; i++) {
            if (multiplier % 2 == 1) {
                double t = capList.get(i);
                finalMult = (float) t;
            }
            multiplier = multiplier / 2;
        }

        return finalMult;
    }

    @Override
    public float getDmgMultiplier(int multiplier) {
        List<? extends Double> valList = WarriorRageConfig.SERVER.penaltyAfterAdvancements.get();
        int len = valList.size();
        float finalMult = 1;

        for (int i = 0; i < len; i++) {
            if (multiplier % 2 == 1) {
                double t = valList.get(i);
                finalMult = (float) t;
            }
            multiplier = multiplier / 2;
        }

        return finalMult;
    }

    @Override
    public void setKillCount(int count) {
        this.hurtNum = 0;
        this.damageNum = 0;
        if (count > MAX_KILL_COUNT_CAP * getCapMultiplier(this.multiplier)) {
            this.killCount = (int) Math.floor(MAX_KILL_COUNT_CAP * getCapMultiplier(this.multiplier));
            refreshRageDuration();
        } else if (count >= 0) {
            this.killCount = count;
            if (count > 0) {
                refreshRageDuration();
            }
        }
    }

    @Override
    public void setRageDuration(int timeInTicks) {
        this.rageDuration = timeInTicks;
    }

    @Override
    public void synchronise() {
        if (playerEntity != null && !playerEntity.level().isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) playerEntity;
            CapabilityUtils.getCapability(serverPlayer)
                    .ifPresent(cap -> WarriorRage.NETWORK.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new SyncRageCapabilityClient(this.killCount, this.rageDuration, this.damageNum,
                                    this.hurtNum, this.multiplier, serverPlayer.getId())));
        }
    }

    @Override
    public void synchroniseToOthers(Player player) {
        if (player != null && !player.level().isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            // CapabilityUtils.getCapability(serverPlayer).ifPresent(cap ->
            // WarriorRage.NETWORK.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()
            // -> serverPlayer), new SyncBackpackCapabilityClient(this.wearable.save(new
            // CompoundTag()), serverPlayer.getId())));
        }
    }

    @Override
    public CompoundTag saveTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("KillCount", this.killCount);
        tag.putInt("Duration", this.rageDuration);
        tag.putFloat("damageNum", this.damageNum);
        tag.putFloat("hurtNum", this.hurtNum);
        tag.putInt("multiplier", this.multiplier);
        return tag;
    }

    @Override
    public void loadTag(CompoundTag compoundTag) {
        this.killCount = compoundTag.getInt("KillCount");
        this.rageDuration = compoundTag.getInt("Duration");
        this.damageNum = compoundTag.getFloat("damageNum");
        this.hurtNum = compoundTag.getFloat("hurtNum");
        this.multiplier = compoundTag.getInt("multiplier");
    }
}
