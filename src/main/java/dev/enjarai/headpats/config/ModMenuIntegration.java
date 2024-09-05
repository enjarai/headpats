package dev.enjarai.headpats.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.net.URI;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            if (!FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
                return new ConfirmScreen((result) -> {
                    if (result) {
                        Util.getOperatingSystem().open(URI.create("https://modrinth.com/mod/yacl/versions"));
                    }
                    MinecraftClient.getInstance().setScreen(parent);
                }, Text.translatable("config.headpats.yacl.missing"), Text.translatable("config.headpats.yacl.missing.message"), ScreenTexts.YES, ScreenTexts.NO);
            } else {
                return YACLImplementation.generateConfigScreen(parent);
            }
        };
    }
}
