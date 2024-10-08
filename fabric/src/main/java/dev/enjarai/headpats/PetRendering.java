package dev.enjarai.headpats;

import dev.enjarai.headpats.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
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
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.sin(petTime * 0.4f) * 16.0f * multiplier));
            matrices.translate(player.getMainArm() == Arm.RIGHT ? -1 : 1, 1, 0);
        }
    }

    public static void setPetAngles(PlayerEntity player, float tickDelta, ModelPart rightArm, ModelPart leftArm, ModelPart head) {
        var petting = Headpats.PETTING_COMPONENT.get(player);

        if (petting.pettingMultiplier > 0) {
            var petTime = MathHelper.lerp(tickDelta, (float) petting.prevPettingTicks, (float) petting.pettingTicks);
            var multiplier = MathHelper.lerp(tickDelta, petting.prevPettingMultiplier, petting.pettingMultiplier);

            if (player.getMainArm() == Arm.RIGHT) {
                rightArm.pitch = rightArm.pitch * (1 - multiplier) - multiplier * 2.1f;
                rightArm.yaw = rightArm.yaw * (1 - multiplier) - MathHelper.sin(petTime * 0.4f) * multiplier * 0.5f;
            } else {
                leftArm.pitch = leftArm.pitch * (1 - multiplier) - multiplier * 2.1f;
                leftArm.yaw = leftArm.yaw * (1 - multiplier) - MathHelper.sin(petTime * 0.4f) * multiplier * 0.5f;
            }
        }

        if (petting.pettedMultiplier > 0) {
            var petTime = MathHelper.lerp(tickDelta, (float) petting.prevPettedTicks, (float) petting.pettedTicks);
            var multiplier = MathHelper.lerp(tickDelta, petting.prevPettedMultiplier, petting.pettedMultiplier);

            head.pitch += multiplier * 0.4f;
            head.roll = -MathHelper.sin(petTime * 0.4f) * multiplier * 0.15f;
        } else {
            head.roll = 0;
        }
    }

    public static void fixFirstPersonAngles(PlayerEntity player, float tickDelta, ModelPart arm, ModelPart sleeve) {
        var petting = Headpats.PETTING_COMPONENT.get(player);

        var multiplier = 1 - MathHelper.lerp(tickDelta, petting.prevPettingMultiplier, petting.pettingMultiplier);
        arm.yaw *= multiplier;
        arm.roll *= multiplier;
        sleeve.yaw *= multiplier;
        sleeve.roll *= multiplier;
    }

    public static @Nullable Float getCameraRoll(PlayerEntity player, float tickDelta) {
        var petting = Headpats.PETTING_COMPONENT.get(player);

        if (petting.pettedMultiplier > 0 && ModConfig.INSTANCE.firstPersonSwayStrength > 0) {
            var petTime = MathHelper.lerp(tickDelta, (float) petting.prevPettedTicks, (float) petting.pettedTicks);
            var multiplier = MathHelper.lerp(tickDelta, petting.prevPettedMultiplier, petting.pettedMultiplier);

            return -MathHelper.sin(petTime * 0.4f) * multiplier * 0.1f * ModConfig.INSTANCE.firstPersonSwayStrength;
        }

        return null;
    }
}
