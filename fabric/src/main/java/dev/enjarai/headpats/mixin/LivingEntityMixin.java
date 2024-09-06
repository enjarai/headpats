package dev.enjarai.headpats.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import dev.enjarai.headpats.Headpats;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {
    @Shadow public abstract float getScale();

    @Shadow public abstract float getScaleFactor();

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/LivingEntity;handSwingProgress:F"
            )
    )
    private void turnBodyWhenPetting(CallbackInfo ci, @Local(ordinal = 1) LocalFloatRef g) {
        var component = Headpats.PETTING_COMPONENT.getNullable(this);
        if (component != null && component.isPetting()) {
            g.set(getYaw());
        }
    }
}
