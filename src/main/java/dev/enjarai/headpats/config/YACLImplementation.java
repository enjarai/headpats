package dev.enjarai.headpats.config;

import dev.enjarai.headpats.Headpats;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class YACLImplementation {
    public static Screen generateConfigScreen(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(getText("title"))
                .category(ConfigCategory.createBuilder()
                        .name(getText("general"))
                        .option(getOption(Double.class, "general", "first_person_sway_strength", false, false)
                                .controller(option -> getDoubleSlider(option, 0, 2, 0.1))
                                .binding(1.0, () -> (double) ModConfig.INSTANCE.firstPersonSwayStrength, value -> ModConfig.INSTANCE.firstPersonSwayStrength = value.floatValue())
                                .build())
                        .option(getBooleanOption("general", "petted_players_purr", false, false)
                                .binding(false, () -> ModConfig.INSTANCE.pettedPlayersPurr, value -> ModConfig.INSTANCE.pettedPlayersPurr = value)
                                .build())
                        .build()
                )
                .save(ModConfig.INSTANCE::save)
                .build()
                .generateScreen(parent);
    }

    private static <T> Option.Builder<T> getOption(Class<T> clazz, String category, String key, boolean description, boolean image) {
        Option.Builder<T> builder = Option.<T>createBuilder()
                .name(getText(category, key));
        var descBuilder = OptionDescription.createBuilder();
        if (description) {
            descBuilder.text(getText(category, key + ".description"));
        }
        if (image) {
            descBuilder.image(Headpats.id("textures/gui/config/images/" + category + "/" + key + ".png"), 480, 275);
        }
        builder.description(descBuilder.build());
        return builder;
    }

    private static Option.Builder<Boolean> getBooleanOption(String category, String key, boolean description, boolean image) {
        return getOption(Boolean.class, category, key, description, image)
                .controller(TickBoxControllerBuilder::create);
    }

    private static MutableText getText(String category, String key) {
        return Text.translatable("config.headpats." + category + "." + key);
    }

    private static MutableText getText(String key) {
        return Text.translatable("config.headpats." + key);
    }

    private static DoubleSliderControllerBuilder getDoubleSlider(Option<Double> option, double min, double max, double step) {
        return DoubleSliderControllerBuilder.create(option)
                .range(min, max)
                .step(step);
    }
}
