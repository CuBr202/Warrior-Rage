package com.tiviacz.warriorrage;

import java.io.ObjectInputFilter.Config;
import java.lang.Math;
import java.util.*;

import com.tiviacz.warriorrage.capability.CapabilityUtils;
import com.tiviacz.warriorrage.capability.IRage;
import com.tiviacz.warriorrage.capability.Rage;
import com.tiviacz.warriorrage.capability.RageCapability;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent.AdvancementEarnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod.EventBusSubscriber(modid = WarriorRage.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(IRage.class);
    }

    @SubscribeEvent
    public static void attachCapabilities(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            final Rage rage = new Rage(player);
            event.addCapability(RageCapability.ID, RageCapability.createProvider(rage));
        }
    }

    @SubscribeEvent
    public static void playerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (CapabilityUtils.getCapability(player).isPresent()) {
                CapabilityUtils.getCapability(player).ifPresent(IRage::removeRageEffects);
                CapabilityUtils.getCapability(player).ifPresent(r -> r.setKillCount(0));
            }
            CapabilityUtils.synchronise(player);
        }
    }

    @SubscribeEvent
    public static void playerClone(final PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        oldPlayer.revive();

        CapabilityUtils.getCapability(oldPlayer)
                .ifPresent(oldRage -> CapabilityUtils.getCapability(event.getEntity())
                        .ifPresent(newRage -> newRage.setKillCount(oldRage.getCurrentKillCount())));
    }

    @SubscribeEvent
    public static void playerChangeDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
        CapabilityUtils.synchronise(event.getEntity());
    }

    @SubscribeEvent
    public static void playerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        CapabilityUtils.synchronise(event.getEntity());
    }

    @SubscribeEvent
    public static void entityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            CapabilityUtils.synchronise(player);
        }
    }

    @SubscribeEvent
    public static void playerTracking(final PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player && !event.getTarget().level().isClientSide) {
            ServerPlayer target = (ServerPlayer) event.getTarget();

            // CapabilityUtils.getCapability(target).ifPresent(c ->
            // WarriorRage.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)
            // event.getPlayer()),
            // new
            // SyncBackpackCapabilityClient(CapabilityUtils.getWearingBackpack(target).save(new
            // CompoundTag()), target.getId())));
        }
    }

    @SubscribeEvent
    public static void playerAttackMob(final LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (CapabilityUtils.getCapability(player).isPresent()) {
                IRage rage = CapabilityUtils.getCapability(player).resolve().get();

                if (WarriorRageConfig.SERVER.enableFireDamage.get()
                        && rage.getCurrentKillCount() >= WarriorRageConfig.SERVER.fireDamageRequiredKillCount.get()) {
                    event.getEntity().setSecondsOnFire(2);

                    // CapabilityUtils.getCapability(player).ifPresent(r -> r.addScore(100));
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerSlayMob(final LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            LazyOptional<IRage> rage = CapabilityUtils.getCapability(player);

            if (rage.isPresent() && event.getEntity() instanceof Monster) {
                rage.ifPresent(r -> r.addKill(1));
                CapabilityUtils.synchronise(player);
            }
        }
    }

    @SubscribeEvent
    public static void mobHurt(final LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player
                && WarriorRageConfig.SERVER.enableRageWhenDamage.get()) {
            LazyOptional<IRage> rage = CapabilityUtils.getCapability(player);

            if (rage.isPresent()
                    && !(WarriorRageConfig.SERVER.enableOnlyMonster.get() && !(event.getEntity() instanceof Monster))) {
                float total = rage.resolve().get().getDamageAmount()
                        + Math.min(event.getAmount(), event.getEntity().getHealth());
                float mult = rage.resolve().get().getMultiplier();

                if (mult == 0) {
                    mult = 1;
                    rage.ifPresent(r -> r.setMultiplier(1));
                }

                double value = WarriorRageConfig.SERVER.damageAmountCountAsKill.get()
                        * mult;
                int cap = WarriorRageConfig.SERVER.maximalKillCountPerDamage.get();
                LOGGER.debug(String.format("Mob is Hurt with total: %f, value: %f, multiplier: %f, addkill: %f", total,
                        WarriorRageConfig.SERVER.damageAmountCountAsKill.get(), mult,
                        total / value));

                rage.ifPresent(r -> r.setDamageAmount(total % (float) value));
                rage.ifPresent(r -> r.addKill(Math.min(cap, (int) (total / value))));

                CapabilityUtils.synchronise(player);
            }
        } else {
            if (event.getEntity() instanceof Player player && WarriorRageConfig.SERVER.enableRageWhenHurt.get()) {
                LazyOptional<IRage> rage = CapabilityUtils.getCapability(player);

                float total = rage.resolve().get().getHurtAmount() + event.getAmount();
                double value = WarriorRageConfig.SERVER.hurtAmountCountAsKill.get();
                int cap = WarriorRageConfig.SERVER.maximalKillCountPerHurt.get();
                LOGGER.debug(String.format("Player is Hurt with total: %f, addkill: %f", total, total / value));

                rage.ifPresent(r -> r.setHurtAmount(total % (float) value));
                rage.ifPresent(r -> r.addKill(Math.min(cap, (int) (total / value))));

                CapabilityUtils.synchronise(player);
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(final AdvancementEarnEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        String advancementId = event.getAdvancement().getId().toString();
        List<? extends String> advList = WarriorRageConfig.SERVER.advancementMilestones.get();
        List<? extends Double> valList = WarriorRageConfig.SERVER.penaltyAfterAdvancements.get();
        // int len = Math.min(advList.size(),valList.size());

        if (advList.contains(advancementId)) {
            double mult = valList.get(advList.indexOf(advancementId));

            LazyOptional<IRage> rage = CapabilityUtils.getCapability(player);

            rage.ifPresent(r -> r.setMultiplier((float) mult));

            CapabilityUtils.synchronise(player);

        }

    }

    @SubscribeEvent
    public static void playerTick(final TickEvent.PlayerTickEvent event) {
        LazyOptional<IRage> rage = CapabilityUtils.getCapability(event.player);

        if (rage.isPresent()) {
            IRage irage = rage.resolve().get();

            if (irage.canStartRage()) {
                // event.player.level.addParticle(ParticleTypes.FLAME, event.player.getX(),
                // event.player.getY(), event.player.getZ(), 0.0D, 1.0D, 0.0D);
                if (WarriorRageConfig.SERVER.enableFireParticle.get())
                    addParticlesAroundSelf(ParticleTypes.FLAME, event.player);
                irage.startRage();
                irage.decreaseRageDuration();
            } else if (irage.getRemainingRageDuration() > 0
                    && irage.getCurrentKillCount() < WarriorRageConfig.SERVER.minimalKillCount.get()) {
                irage.decreaseRageDuration();
            } else {
                irage.removeRageEffects();
            }
        }
    }

    private static int tick = 0;

    protected static void addParticlesAroundSelf(ParticleOptions particleOptions, Player player) {
        tick++;

        if (tick == 50) {
            for (int i = 0; i < 5; ++i) {
                double d0 = player.level().random.nextGaussian() * 0.02D;
                double d1 = player.level().random.nextGaussian() * 0.02D;
                double d2 = player.level().random.nextGaussian() * 0.02D;
                player.level().addParticle(particleOptions, player.getRandomX(1.0D), player.getRandomY() + 1.0D,
                        player.getRandomZ(1.0D), d0, d1, d2);
            }
            tick = 0;
        }
    }
}
