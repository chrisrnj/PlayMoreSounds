package com.epicnicity322.playmoresounds.core;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.logger.ErrorLogger;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface PlayMoreSounds
{
    String version = "3.0.0";

    @NotNull Path getJar();

    @NotNull Path getCoreDataFolder();

    @NotNull ErrorLogger getCoreErrorLogger();

    @NotNull ConsoleLogger<?> getCoreLogger();

    @NotNull AddonManager getAddonManager();
}
