/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public final class VersionUtils
{
    private static final boolean hasPersistentData;
    private static final boolean hasStopSound;
    private static final boolean hasOffHand;
    private static final @NotNull Version bukkitVersion;
    private static boolean hasHoverContentApi = false;
    private static boolean paperMC = false;

    static {
        String version = Bukkit.getBukkitVersion();

        // Removing release number.
        bukkitVersion = new Version(version.substring(0, version.indexOf("-")));

        // Checking if bukkit version is 1.14 because persistent data was added in that version.
        hasPersistentData = bukkitVersion.compareTo(new Version("1.14")) >= 0;

        // Checking if bukkit version is 1.10.2 because Player#stopSound was added in that version.
        hasStopSound = bukkitVersion.compareTo(new Version("1.10.2")) >= 0;

        // Checking if bukkit version is 1.9 because off hand was added in that version.
        hasOffHand = bukkitVersion.compareTo(new Version("1.9")) >= 0;

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
