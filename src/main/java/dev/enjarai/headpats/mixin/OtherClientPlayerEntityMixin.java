package dev.enjarai.headpats.mixin;

import dev.enjarai.headpats.Headpats;
import dev.enjarai.headpats.net.PettingC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OtherClientPlayerEntity.class)
public abstract class OtherClientPlayerEntityMixin extends LivingEntityMixin {
    @Override
    protected void interact(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        double y = hitPos.y / (getScale() * getScaleFactor());
        double height = getHeight() / (getScale() * getScaleFactor());
        if (y > height - 0.5 && player instanceof ClientPlayerEntity clientPlayer && player.getMainHandStack().isEmpty() && squaredDistanceTo(player) < 1.5*1.5) {
            ClientPlayNetworking.send(new PettingC2SPacket(getId()));
            Headpats.PETTING_COMPONENT.get(clientPlayer).startPetting((PlayerEntity) (Object) this);
            cir.setReturnValue(ActionResult.success(false));
        }
    }
}
