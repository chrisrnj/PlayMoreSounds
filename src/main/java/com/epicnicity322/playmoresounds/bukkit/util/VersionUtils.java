/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2021 Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public final class VersionUtils
{
    private static final boolean hasStopSound;
    private static final boolean hasOffHand;
    private static final @NotNull Version bukkitVersion;
    private static boolean hasPersistentData = false;
    private static boolean hasSoundEffects = false;
    private static boolean hasHoverContentApi = false;
    private static boolean paperMC = false;
    private static boolean bStats = false;
    private static boolean resourcePacks = false;
    private static boolean hasItemFlags = false;

    static {
        String version = Bukkit.getBukkitVersion();

        // Removing release number.
        bukkitVersion = new Version(version.substring(0, version.indexOf("-")));

        // Checking if bukkit version is 1.10.2 because Player#stopSound was added in that version.
        hasStopSound = bukkitVersion.compareTo(new Version("1.10.2")) >= 0;

        // Checking if bukkit version is 1.9 because off hand was added in that version.
        hasOffHand = bukkitVersion.compareTo(new Version("1.9")) >= 0;

        try {
            Class.forName("org.bukkit.persistence.PersistentDataContainer");
            hasPersistentData = true;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("net.md_5.bungee.api.chat.hover.content.Content");
            hasHoverContentApi = true;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            paperMC = true;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("com.google.gson.JsonElement");
            bStats = true;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("org.bukkit.event.player.PlayerResourcePackStatusEvent");
            resourcePacks = true;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("org.bukkit.inventory.ItemFlag");
            hasItemFlags = true;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("net.minecraft.server." + ReflectionUtil.getNmsVersion() + ".SoundEffect");
            hasSoundEffects = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private VersionUtils()
    {
    }

    /**
     * Gets the running version of bukkit without the release number.
     *
     * @return The version of bukkit.
     */
    public static @NotNull Version getBukkitVersion()
    {
        return bukkitVersion;
    }

    /**
     * Whether the version of bukkit running supports bStats.
     *
     * @return If bStats is supported.
     */
    public static boolean supportsBStats()
    {
        return bStats;
    }

    /**
     * Whether the version of bukkit running supports resource pack asking feature.
     *
     * @return If Resource Packs feature is supported.
     */
    public static boolean supportsResourcePacks()
    {
        return resourcePacks;
    }

    /**
     * Whether the version of bukkit running has {@link org.bukkit.inventory.ItemFlag} class.
     *
     * @return If {@link org.bukkit.inventory.ItemFlag} is present.
     */
    public static boolean hasItemFlags()
    {
        return hasItemFlags;
    }

    /**
     * Whether the version of net.minecraft.server running has SoundEffect class.
     *
     * @return If SoundEffect class is present.
     */
    public static boolean hasSoundEffects()
    {
        return hasSoundEffects;
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

    /**
     * Whether you are running Spigot and the bungee text component api has net.md_5.bungee.api.chat.hover.content package.
     */
    public static boolean hasHoverContentApi()
    {
        return hasHoverContentApi;
    }

    /**
     * Whether the server is running PaperMC.
     */
    public static boolean isPaperMC()
    {
        return paperMC;
    }
}
