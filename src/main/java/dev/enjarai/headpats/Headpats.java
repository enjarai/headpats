package dev.enjarai.headpats;

import dev.enjarai.headpats.config.ModConfig;
import dev.enjarai.headpats.net.PettingC2SPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Headpats implements ModInitializer, ClientModInitializer, EntityComponentInitializer {
	public static final String MOD_ID = "headpats";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ComponentKey<PettingComponent> PETTING_COMPONENT = ComponentRegistry.getOrCreate(id("petting"), PettingComponent.class);

	@Override
	public void onInitialize() {
		LOGGER.info("Meow!");

		var init = ModConfig.INSTANCE;

		PayloadTypeRegistry.playC2S().register(PettingC2SPacket.ID, PettingC2SPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PettingC2SPacket.ID, PettingC2SPacket::handle);
	}

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				var pettingComponent = PETTING_COMPONENT.get(client.player);
				if (pettingComponent.isPetting()) {
					if (!client.options.useKey.isPressed() || !pettingComponent.isPetting(client.targetedEntity)
							|| !client.player.getMainHandStack().isEmpty() || client.player.squaredDistanceTo(client.targetedEntity) > 1.5*1.5) {
						ClientPlayNetworking.send(new PettingC2SPacket(-1));
						pettingComponent.stopPetting();
					}
				}
			}
		});
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(PETTING_COMPONENT, PettingComponent::new, RespawnCopyStrategy.NEVER_COPY);
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}