package dev.enjarai.headpats;

import dev.enjarai.headpats.config.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

public class PettingComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    @Nullable
    private UUID petting;
    private int incomingPetters;
    public int prevPettingTicks;
    public int pettingTicks;
    public float prevPettingMultiplier;
    public float pettingMultiplier;
    public int prevPettedTicks;
    public int pettedTicks;
    public float prevPettedMultiplier;
    public float pettedMultiplier;

    private final PlayerEntity player;

    public PettingComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // Nope!
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // Don't need it :3
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        if (buf.readBoolean()) {
            petting = buf.readUuid();
        } else {
            petting = null;
        }
        incomingPetters = buf.readInt();
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeBoolean(petting != null);
        if (petting != null) {
            buf.writeUuid(petting);
        }
        buf.writeInt(incomingPetters);
    }

    @Override
    public boolean isRequiredOnClient() {
        return false;
    }

    @Override
    public void serverTick() {
        if (petting != null && player.getWorld().getPlayerByUuid(petting) == null) {
            stopPetting();
        }
    }

    @Override
    public void clientTick() {
        prevPettingTicks = pettingTicks;
        prevPettingMultiplier = pettingMultiplier;
        if (isPetting()) {
            pettingTicks++;
            pettingMultiplier += (1 - pettingMultiplier) * 0.3f;
        } else {
            pettingMultiplier -= pettingMultiplier * 0.3f;
            if (pettingMultiplier < 0.01f) {
                pettingMultiplier = 0;
                pettingTicks = 0;
            }
        }

        // Imagine clean code :clueless:
        prevPettedTicks = pettedTicks;
        prevPettedMultiplier = pettedMultiplier;
        if (isBeingPet()) {
            if (pettedTicks % 40 == 0 && ModConfig.INSTANCE.pettedPlayersPurr) {
                player.playSound(SoundEvents.ENTITY_CAT_PURR);
            }

            pettedTicks++;
            pettedMultiplier += (1 - pettedMultiplier) * 0.3f;
        } else {
            pettedMultiplier -= pettedMultiplier * 0.3f;
            if (pettedMultiplier < 0.01f) {
                pettedMultiplier = 0;
                pettedTicks = 0;
            }
        }
    }

    public void startPetting(PlayerEntity other) {
        if (petting == null) {
            petting = other.getUuid();
            Headpats.PETTING_COMPONENT.get(other).incomingPetters++;
            Headpats.PETTING_COMPONENT.sync(other);
            Headpats.PETTING_COMPONENT.sync(player);
        }
    }

    public void stopPetting() {
        if (petting != null) {
            var server = player.getServer();

            PlayerEntity other;
            if (server != null) {
                other = server.getPlayerManager().getPlayer(petting);
            } else {
                other = player.getWorld().getPlayerByUuid(petting);
            }

            if (other != null) {
                Headpats.PETTING_COMPONENT.get(other).incomingPetters--;
                Headpats.PETTING_COMPONENT.sync(other);
            }

            petting = null;
            Headpats.PETTING_COMPONENT.sync(player);
        }
    }

    public boolean isPetting() {
        return petting != null;
    }

    public boolean isBeingPet() {
        return incomingPetters > 0;
    }

    public boolean isPetting(@Nullable Entity player) {
        return petting != null && player != null && player.getUuid().equals(petting);
    }
}
