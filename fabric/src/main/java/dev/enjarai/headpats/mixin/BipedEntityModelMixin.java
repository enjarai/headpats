package dev.enjarai.headpats.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntityRenderState> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;

    @Inject(
            method = "animateArms(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", //"setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V",
            at = @At(
                    value = "HEAD"
                    //target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;animateArms(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;F)V"
            )
    )
    protected void positionModelParts(BipedEntityRenderState bipedEntityRenderState, CallbackInfo ci) {

    }
}
