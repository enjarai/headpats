package dev.enjarai.headpats.config;

import net.fabricmc.loader.api.FabricLoader;
import nl.enjarai.cicada.api.util.AbstractModConfig;

public class ModConfig extends AbstractModConfig {
    public static final ModConfig INSTANCE = loadConfigFile(
            FabricLoader.getInstance().getConfigDir().resolve("headpats.json"), new ModConfig());

    public float firstPersonSwayStrength = 1f;
    public boolean pettedPlayersPurr = false;
}
