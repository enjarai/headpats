package dev.enjarai.headpats.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow public abstract int getId();

	@Shadow public abstract double squaredDistanceTo(Entity entity);

	@Inject(
			at = @At("HEAD"),
			method = "interact",
			cancellable = true
	)
	protected void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {

	}
}