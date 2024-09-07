package dev.enjarai.headpats;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.google.common.io.ByteStreams;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class Headpats extends JavaPlugin implements Listener {
    private static final String UPDATE_CHANNEL = "headpats:petting";
    private static final String SYNC_CHANNEL = "cardinal-components:entity_sync";

    private final HashMap<UUID, UUID> pettingMap = new HashMap<>();
    private final HashMap<UUID, Integer> pettedCounts = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, SYNC_CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, UPDATE_CHANNEL, (channel, player, message) -> {
            var buf = ByteStreams.newDataInput(message);
            var petted = buf.readInt();

            ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();
            net.minecraft.world.entity.player.Player pettedPlayer = null;
            if (petted != -1) {
                var pettedEntity = world.getEntity(petted);

                if (!(pettedEntity instanceof net.minecraft.world.entity.player.Player)) {
                    return;
                }
                pettedPlayer = (net.minecraft.world.entity.player.Player) pettedEntity;
            }

            // if map shouldnt have key but does, or mapped uuid != current uuid
            if (pettedPlayer == null ? pettingMap.containsKey(player.getUniqueId()) : !pettedPlayer.getUUID().equals(pettingMap.get(player.getUniqueId()))) {
                UUID playerToUpdate;
                if (pettedPlayer == null) {
                    playerToUpdate = pettingMap.get(player.getUniqueId());
                    pettedCounts.computeIfPresent(pettingMap.get(player.getUniqueId()), (k, v) -> v == 1 ? null : v - 1);
                    pettingMap.remove(player.getUniqueId());
                } else {
                    playerToUpdate = pettedPlayer.getUUID();
                    pettedCounts.compute(pettedPlayer.getUUID(), (k, v) -> v == null ? 1 : v + 1);
                    pettingMap.put(player.getUniqueId(), pettedPlayer.getUUID());
                }

                var pettingBuf = getSyncBuf(player.getEntityId(), player.getUniqueId());
                sendSyncBuf(player.getUniqueId(), pettingBuf);

                var playerToUpdateEntity = world.getPlayerByUUID(playerToUpdate);
                if (playerToUpdateEntity != null) {
                    var pettedBuf = getSyncBuf(playerToUpdateEntity.getId(), playerToUpdate);
                    sendSyncBuf(playerToUpdate, pettedBuf);
                }
            }
        });
    }

    private byte[] getSyncBuf(int networkId, UUID uuid) {
        var pettingBuf = new FriendlyByteBuf(Unpooled.buffer());
        // CCA header
        pettingBuf.writeVarInt(networkId);
        pettingBuf.writeBoolean(false);
        Utf8String.write(pettingBuf, "headpats:petting", 32767);
        // payload
        var subBuf = new FriendlyByteBuf(Unpooled.buffer());
        subBuf.writeBoolean(pettingMap.containsKey(uuid));
        if (pettingMap.containsKey(uuid)) {
            subBuf.writeLong(pettingMap.get(uuid).getMostSignificantBits());
            subBuf.writeLong(pettingMap.get(uuid).getLeastSignificantBits());
        }
        subBuf.writeInt(pettedCounts.getOrDefault(uuid, 0));
        pettingBuf.writeVarInt(subBuf.readableBytes());
        pettingBuf.writeBytes(subBuf);

        var array = new byte[pettingBuf.readableBytes()];
        pettingBuf.readBytes(array);
        return array;
    }

    private void sendSyncBuf(UUID uuid, byte[] buf) {
        var player = getServer().getPlayer(uuid);
        if (player != null) {
            var watching = player.getLocation().getNearbyPlayers(getServer().getViewDistance());
            for (Player other : watching) {
                other.sendPluginMessage(this, SYNC_CHANNEL, buf);
            }
        }
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();
        pettedCounts.remove(uuid);
        pettingMap.remove(uuid);
        pettingMap.values().removeIf(u -> u.equals(uuid));
    }

    @EventHandler
    public void onServerTickStart(ServerTickStartEvent event) {
        if (event.getTickNumber() % 20 == 0) {
            for (UUID uuid : pettedCounts.keySet()) {
                if (!pettingMap.containsValue(uuid)) {
                    pettedCounts.remove(uuid);

                    var player = getServer().getPlayer(uuid);
                    if (player != null) {
                        var pettedBuf = getSyncBuf(((CraftPlayer) player).getHandle().getId(), uuid);
                        sendSyncBuf(uuid, pettedBuf);
                    }
                }
            }
        }
    }
}
