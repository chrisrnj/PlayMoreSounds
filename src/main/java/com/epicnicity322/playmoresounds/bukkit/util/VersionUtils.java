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
import com.epicnicity322.epicpluginlib.bukkit.reflection.type.PackageType;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class VersionUtils
{
    private static final @NotNull Version bukkitVersion = new Version(Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf("-")));
    // Checking if bukkit version is 1.10.2 because Player#stopSound was added in that version.
    private static final boolean hasStopSound = bukkitVersion.compareTo(new Version("1.10.2")) >= 0;
    // Checking if bukkit version is 1.9 because off hand was added in that version.
    private static final boolean hasOffHand = bukkitVersion.compareTo(new Version("1.9")) >= 0;
    private static final boolean hasPersistentData = ReflectionUtil.getClass("org.bukkit.persistence.PersistentDataContainer") != null;
    private static final boolean hasSoundEffects = ReflectionUtil.getClass("net.minecraft.sounds.SoundEffect") != null || ReflectionUtil.getClass("SoundEffect", PackageType.MINECRAFT_SERVER) != null;
    private static final boolean hasHoverContentApi = ReflectionUtil.getClass("net.md_5.bungee.api.chat.hover.content.Content") != null;
    private static final boolean paperMC = ReflectionUtil.getClass("com.destroystokyo.paper.PaperConfig") != null;
    private static final boolean resourcePacks = ReflectionUtil.getClass("org.bukkit.event.player.PlayerResourcePackStatusEvent") != null;
    private static final boolean hasItemFlags = ReflectionUtil.getClass("org.bukkit.inventory.ItemFlag") != null;

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
     * Whether the version of bukkit running has {@link org.bukkit.entity.Player#stopSound} method.
     *
     * @return If stop sound method is present.
     */
    public static boolean hasStopSound()
    {
        return hasStopSound;
    }

    /**
     * Whether the version of bukkit running has item in main hand or off hand methods for {@link org.bukkit.inventory.PlayerInventory}.
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
