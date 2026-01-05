package dev.enjarai.headpats;

import dev.enjarai.headpats.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

public class PetRendering {
    public static void modifyHandMatrix(PlayerEntity player, float tickDelta, MatrixStack matrices) {
        var petting = Headpats.PETTING_COMPONENT.get(player);
        if (petting.pettingMultiplier > 0) {
            var petTime = MathHelper.lerp(tickDelta, (float) petting.prevPettingTicks, (float) petting.pettingTicks);
            var multiplier = MathHelper.lerp(tickDelta, petting.prevPettingMultiplier, petting.pettingMultiplier);
            matrices.translate(player.getMainArm() == Arm.RIGHT ? 1 : -1, -1, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) Math.sin(petTime * 0.4f) * 16.0f * multiplier));
            matrices.translate(player.getMainArm() == Arm.RIGHT ? -1 : 1, 1, 0);
        }
    }

    public static void updateRenderState(PlayerEntity player, EntityRenderState renderState, float tickDelta) {
        var petting = Headpats.PETTING_COMPONENT.get(player);

        Headpats.PETTING_MULTIPLIER_KEY.put(renderState, MathHelper.lerp(tickDelta, petting.prevPettingMultiplier, petting.pettingMultiplier));
        Headpats.PETTING_TIME_KEY.put(renderState, MathHelper.lerp(tickDelta, (float) petting.prevPettingTicks, (float) petting.pettingTicks));
        Headpats.PETTED_MULTIPLIER_KEY.put(renderState, MathHelper.lerp(tickDelta, petting.prevPettedMultiplier, petting.pettedMultiplier));
        Headpats.PETTED_TIME_KEY.put(renderState, MathHelper.lerp(tickDelta, (float) petting.prevPettedTicks, (float) petting.pettedTicks));
    }

    public static void setPetAngles(EntityRenderState state, ModelPart rightArm, ModelPart leftArm, ModelPart head) {
        var pettingTime = Headpats.PETTING_TIME_KEY.get(state);
        var pettingMultiplier = Headpats.PETTING_MULTIPLIER_KEY.get(state);

        if (pettingMultiplier > 0) {
            var arm = Arm.RIGHT;
            if (state instanceof BipedEntityRenderState playerState) {
                arm = playerState.mainArm;
            }

            if (arm == Arm.RIGHT) {
                rightArm.pitch = rightArm.pitch * (1 - pettingMultiplier) - pettingMultiplier * 2.1f;
                rightArm.yaw = rightArm.yaw * (1 - pettingMultiplier) - (float) Math.sin(pettingTime * 0.4f) * pettingMultiplier * 0.5f;
            } else {
                leftArm.pitch = leftArm.pitch * (1 - pettingMultiplier) - pettingMultiplier * 2.1f;
                leftArm.yaw = leftArm.yaw * (1 - pettingMultiplier) - (float) Math.sin(pettingTime * 0.4f) * pettingMultiplier * 0.5f;
            }
        }

        var pettedTime = Headpats.PETTED_TIME_KEY.get(state);
        var pettedMultiplier = Headpats.PETTED_MULTIPLIER_KEY.get(state);

        if (pettedMultiplier > 0) {
            head.pitch += pettedMultiplier * 0.4f;
            head.roll = -(float) Math.sin(pettedTime * 0.4f) * pettedMultiplier * 0.15f;
        } else {
            head.roll = 0;
        }
    }

    public static void fixFirstPersonAngles(PlayerEntity player, float tickDelta, ModelPart arm) {
        var petting = Headpats.PETTING_COMPONENT.get(player);

        var multiplier = 1 - MathHelper.lerp(tickDelta, petting.prevPettingMultiplier, petting.pettingMultiplier);
        arm.yaw *= multiplier;
        arm.roll *= multiplier;
//        sleeve.yaw *= multiplier;
//        sleeve.roll *= multiplier;
    }

    public static @Nullable Float getCameraRoll(PlayerEntity player, float tickDelta) {
        var petting = Headpats.PETTING_COMPONENT.get(player);
        var finalFirstPersonSwayStrength = ModConfig.INSTANCE.firstPersonSwayStrength * MinecraftClient.getInstance().options.getDistortionEffectScale().getValue();

        if (petting.pettedMultiplier > 0 && finalFirstPersonSwayStrength > 0) {
            var petTime = MathHelper.lerp(tickDelta, (float) petting.prevPettedTicks, (float) petting.pettedTicks);
            var multiplier = MathHelper.lerp(tickDelta, petting.prevPettedMultiplier, petting.pettedMultiplier);

            return -(float) Math.sin(petTime * 0.4f) * multiplier * 0.1f * (float) finalFirstPersonSwayStrength;
        }

        return null;
    }
}
