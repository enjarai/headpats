package dev.enjarai.headpats.net;

import dev.enjarai.headpats.Headpats;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record PettingC2SPacket(int entityId) implements CustomPayload {
    public static final Id<PettingC2SPacket> ID = new Id<>(Headpats.id("petting"));
    public static final PacketCodec<PacketByteBuf, PettingC2SPacket> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, PettingC2SPacket::entityId,
            PettingC2SPacket::new
    );

    public void handle(ServerPlayNetworking.Context context) {
        if (entityId != -1) {
            var target = context.player().getEntityWorld().getEntityById(entityId);
            if (target instanceof PlayerEntity player) {
                Headpats.PETTING_COMPONENT.get(context.player()).startPetting(player);
            }
        } else {
            Headpats.PETTING_COMPONENT.get(context.player()).stopPetting();
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
