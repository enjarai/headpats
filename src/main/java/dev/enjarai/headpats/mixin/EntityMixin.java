package dev.enjarai.headpats.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow public abstract int getId();

	@Shadow public abstract double squaredDistanceTo(Entity entity);

	@Shadow public abstract float getYaw();

	@Shadow public abstract float getHeight();

	@Inject(
			at = @At("HEAD"),
			method = "interactAt",
			cancellable = true
	)
	protected void interact(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {

	}
}