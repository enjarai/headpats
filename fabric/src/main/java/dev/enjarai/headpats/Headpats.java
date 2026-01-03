package dev.enjarai.headpats;

import dev.enjarai.headpats.config.ModConfig;
import dev.enjarai.headpats.net.PettingC2SPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import nl.enjarai.cicada.api.render.RenderStateKey;
import nl.enjarai.cicada.api.render.RenderStateUpdateEvent;
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
	public static final RenderStateKey<Float> PETTING_MULTIPLIER_KEY = RenderStateKey.of(id("petting_multiplier"), 0f);
	public static final RenderStateKey<Float> PETTING_TIME_KEY = RenderStateKey.of(id("petting_time"), 0f);
	public static final RenderStateKey<Float> PETTED_MULTIPLIER_KEY = RenderStateKey.of(id("petted_multiplier"), 0f);
	public static final RenderStateKey<Float> PETTED_TIME_KEY = RenderStateKey.of(id("petted_time"), 0f);

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
					if (client.crosshairTarget != null && client.targetedEntity instanceof PlayerEntity otherEntity) {
						var hitPos = client.crosshairTarget.getPos().subtract(otherEntity.getSyncedPos());
						double y = hitPos.y / (otherEntity.getScale() * otherEntity.getScaleFactor());
						double height = otherEntity.getHeight() / (otherEntity.getScale() * otherEntity.getScaleFactor());
						if (y > height - 0.5 && client.options.useKey.isPressed() && pettingComponent.isPetting(otherEntity)
								&& client.player.getMainHandStack().isEmpty() && client.player.squaredDistanceTo(otherEntity) < 1.5 * 1.5) {
							return;
						}
					}

					ClientPlayNetworking.send(new PettingC2SPacket(-1));
					pettingComponent.stopPetting();
				}
			}
		});

		RenderStateUpdateEvent.get(PlayerEntity.class).register(PetRendering::updateRenderState);
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(PETTING_COMPONENT, PettingComponent::new, RespawnCopyStrategy.NEVER_COPY);
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}