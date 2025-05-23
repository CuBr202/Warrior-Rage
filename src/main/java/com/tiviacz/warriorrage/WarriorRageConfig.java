package com.tiviacz.warriorrage;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import java.util.*;

@Mod.EventBusSubscriber(modid = WarriorRage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WarriorRageConfig {
        public static class Server {
                public final ForgeConfigSpec.IntValue minimalKillCount;
                public final ForgeConfigSpec.IntValue rageDuration;
                public final ForgeConfigSpec.IntValue maxKillCountCap;
                public final ForgeConfigSpec.IntValue killIntervalBetweenNextBonus;
                public final ForgeConfigSpec.DoubleValue bonusDamage;
                public final ForgeConfigSpec.DoubleValue bonusAttackSpeed;
                public final ForgeConfigSpec.BooleanValue enableFireDamage;
                public final ForgeConfigSpec.BooleanValue enableFireParticle;

                public final ForgeConfigSpec.BooleanValue enableRageWhenHurt;
                public final ForgeConfigSpec.DoubleValue hurtAmountCountAsKill;
                public final ForgeConfigSpec.IntValue maximalKillCountPerHurt;
                public final ForgeConfigSpec.BooleanValue enableRageWhenDamage;
                public final ForgeConfigSpec.BooleanValue enableOnlyMonster;
                public final ForgeConfigSpec.DoubleValue damageAmountCountAsKill;
                public final ForgeConfigSpec.IntValue maximalKillCountPerDamage;

                public final ForgeConfigSpec.IntValue fireDamageRequiredKillCount;

                public final ForgeConfigSpec.ConfigValue<List<? extends String>> advancementMilestones;
                public final ForgeConfigSpec.ConfigValue<List<? extends Double>> penaltyAfterAdvancements;
                public final ForgeConfigSpec.ConfigValue<List<? extends Double>> capMultiplier;

                Server(final ForgeConfigSpec.Builder builder) {
                        builder.comment("Server config settings")
                                        .push("server");

                        minimalKillCount = builder
                                        .comment("Minimal kill count for rage to enable")
                                        .defineInRange("minimalKillCount", 3, 0, 1000);

                        rageDuration = builder
                                        .comment("In Seconds")
                                        .defineInRange("rageDuration", 20, 0, 1000);

                        maxKillCountCap = builder
                                        .comment("Max kill count for rage bonus damage")
                                        .defineInRange("maxKillCountCap", 20, 0, 1000);

                        killIntervalBetweenNextBonus = builder
                                        .comment("Number of kills, which will multiply the bonus damage eg. 5 means, every 5 kills attack damage will be increased by bonusDamage value")
                                        .defineInRange("killIntervalBetweenNextBonus", 4, 1, 1000);

                        bonusDamage = builder
                                        .comment("Bonus damage percentage per 4 kills in a row")
                                        .defineInRange("bonusDamage", 0.03D, 0.01D, 10.0D);

                        bonusAttackSpeed = builder
                                        .comment("Bonus attack speed per 4 kills in a row")
                                        .defineInRange("bonusAttackSpeed", 0.05D, 0.01D, 10.0D);

                        enableFireDamage = builder
                                        .comment("Enable Fire Damage")
                                        .define("enableFireDamage", true);

                        enableFireParticle = builder
                                        .comment("Enable Fire Particle")
                                        .define("enableFireParticle", true);

                        enableRageWhenHurt = builder
                                        .comment("Enable rage accumulation when taking damage.")
                                        .define("enableRageWhenHurt", true);

                        hurtAmountCountAsKill = builder
                                        .comment("How many damage taken can count as one kill.")
                                        .defineInRange("hurtAmountCountAsKill", 20.0, 1.0, 10000.0);

                        maximalKillCountPerHurt = builder
                                        .comment("No matter how many damage is taken once, the count that are taken as kills in a single time cannot exceed this value.")
                                        .defineInRange("maximalKillCountPerHurt", 1, 1, 20);

                        enableRageWhenDamage = builder
                                        .comment("Enable rage accumulation when dealing damage.")
                                        .define("enableRageWhenDamage", true);

                        enableOnlyMonster = builder
                                        .comment("Enable rage accumulation only when damage target is a monster.(Animals, iron golem,and dragon in Ice and Fire all don't count as monsters.)")
                                        .define("enableOnlyMonster", true);

                        damageAmountCountAsKill = builder
                                        .comment("How many damage dealt can count as one kill.")
                                        .defineInRange("damageAmountCountAsKill", 100.0, 1.0, 10000.0);

                        maximalKillCountPerDamage = builder
                                        .comment("No matter how many damage is dealt once, the count that are taken as kills in a single time cannot exceed this value.")
                                        .defineInRange("maximalKillCountPerDamage", 1, 1, 20);

                        fireDamageRequiredKillCount = builder
                                        .comment("Required minimal kill count for fire damage to apply")
                                        .defineInRange("fireDamageRequiredKillCount", 20, 0, 1000);

                        penaltyAfterAdvancements = builder
                                        .comment("After achieving some advancements, the damage dealt that can be count as one kill will grow. It will be multiplied by the corresponding value according to the achievement.")
                                        .defineList("penaltyAfterAdvancements", Arrays.asList(3.0), entry -> true);

                        capMultiplier = builder
                                        .comment("After achieving some advancements, the max kill count for rage bonus damage will grow. It will be multiplied by the corresponding value according to the achievement.")
                                        .defineList("capMultiplier", Arrays.asList(1.4), entry -> true);

                        advancementMilestones = builder
                                        .comment("The list of advancements for above. One advancement corresponds to one value.")
                                        .defineList("advancementMilestones", Arrays.asList("minecraft:end/kill_dragon"),
                                                        entry -> true);

                        builder.pop();
                }
        }

        public static class Client {
                public final ForgeConfigSpec.BooleanValue renderRageBar;
                public final ForgeConfigSpec.BooleanValue renderRageIcon;
                public final ForgeConfigSpec.IntValue offsetX;
                public final ForgeConfigSpec.IntValue offsetY;

                Client(final ForgeConfigSpec.Builder builder) {
                        builder.comment("Client-only settings")
                                        .push("client");

                        renderRageBar = builder
                                        .comment("Render Rage Bar on experience bar")
                                        .define("renderRageBar", true);

                        renderRageIcon = builder
                                        .comment("Render Rage Icon next to player's Hotbar")
                                        .define("renderRageIcon", true);

                        offsetX = builder
                                        .comment("Negative offsets to left side, positive to right")
                                        .defineInRange("offsetX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

                        offsetY = builder
                                        .comment("Negative offsets to up, positive to down")
                                        .defineInRange("offsetY", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

                        builder.pop();
                }
        }

        // COMMON
        private static final ForgeConfigSpec serverSpec;
        public static final Server SERVER;

        static {
                final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
                serverSpec = specPair.getRight();
                SERVER = specPair.getLeft();
        }

        // CLIENT
        private static final ForgeConfigSpec clientSpec;
        public static final Client CLIENT;

        static {
                final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
                clientSpec = specPair.getRight();
                CLIENT = specPair.getLeft();
        }

        // Register
        public static void register(final ModLoadingContext context) {
                context.registerConfig(ModConfig.Type.SERVER, serverSpec);
                context.registerConfig(ModConfig.Type.CLIENT, clientSpec);
        }
}
