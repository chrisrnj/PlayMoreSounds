package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public final class VersionUtils
{
    private static final boolean hasPersistentData;
    private static final boolean hasStopSound;
    private static final boolean hasOffHand;
    private static @NotNull String bukkitVersion;

    static {
        bukkitVersion = Bukkit.getBukkitVersion();

        // Removing release number.
        bukkitVersion = bukkitVersion.substring(0, bukkitVersion.indexOf("-"));

        // Checking if bukkit version is greater than 1.13.2 because persistent data was added in bukkit 1.14.
        hasPersistentData = StringUtils.isVersionGreater(bukkitVersion, "1.13.2");

        // Checking if bukkit version is greater than 1.10 because Player#stopSound was added in 1.10.2.
        hasStopSound = StringUtils.isVersionGreater(bukkitVersion, "1.10");

        hasOffHand = StringUtils.isVersionGreater(bukkitVersion, "1.8.8");
    }

    private VersionUtils()
    {
    }

    /**
     * Gets the running version of bukkit without the release number.
     *
     * @return The version of bukkit.
     */
    public static @NotNull String getBukkitVersion()
    {
        return bukkitVersion;
    }

    /**
     * Whether the version of bukkit running has persistent data api.
     *
     * @return If persistent data api is present.
     */
    public static boolean hasPersistentData()
    {
        return hasPersistentData;
    }

    /**
     * Whether the version of bukkit running has {@link Player#stopSound} method.
     *
     * @return If stop sound method is present.
     */
    public static boolean hasStopSound()
    {
        return hasStopSound;
    }

    /**
     * Whether the version of bukkit running has item in main hand or off hand methods for {@link PlayerInventory}.
     */
    public static boolean hasOffHand()
    {
        return hasOffHand;
    }
}
