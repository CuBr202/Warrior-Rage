package com.tiviacz.warriorrage.network;

import com.tiviacz.warriorrage.capability.CapabilityUtils;
import com.tiviacz.warriorrage.capability.IRage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncRageCapabilityClient {
    private final int killCount;
    private final int remainingDuration;
    private final float damageNum;
    private final float hurtNum;
    private final float multiplier;
    private final int entityID;

    public SyncRageCapabilityClient(int killCount, int remainingDuration, float damage, float hurt, float mult,
            int entityID) {
        this.killCount = killCount;
        this.remainingDuration = remainingDuration;
        this.damageNum = damage;
        this.hurtNum = hurt;
        this.multiplier = mult;
        this.entityID = entityID;
    }

    public static SyncRageCapabilityClient decode(final FriendlyByteBuf buffer) {
        final int killCount = buffer.readInt();
        final int remainingDuration = buffer.readInt();
        final float damageNum = buffer.readFloat();
        final float hurtNum = buffer.readFloat();
        final float multiplier = buffer.readFloat();
        final int entityID = buffer.readInt();

        return new SyncRageCapabilityClient(killCount, remainingDuration, damageNum, hurtNum, multiplier, entityID);
    }

    public static void encode(final SyncRageCapabilityClient message, final FriendlyByteBuf buffer) {
        buffer.writeInt(message.killCount);
        buffer.writeInt(message.remainingDuration);
        buffer.writeFloat(message.damageNum);
        buffer.writeFloat(message.hurtNum);
        buffer.writeFloat(message.multiplier);
        buffer.writeInt(message.entityID);
    }

    public static void handle(final SyncRageCapabilityClient message, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {

            final Player playerEntity = (Player) Minecraft.getInstance().player.level().getEntity(message.entityID);
            IRage cap = CapabilityUtils.getCapability(playerEntity).orElse(null);
            if (cap != null) {
                cap.setKillCount(message.killCount);
                cap.setRageDuration(message.remainingDuration);
                cap.setDamageAmount(message.damageNum);
                cap.setHurtAmount(message.hurtNum);
                cap.setMultiplier(message.multiplier);
            }
        }));

        ctx.get().setPacketHandled(true);
    }
}