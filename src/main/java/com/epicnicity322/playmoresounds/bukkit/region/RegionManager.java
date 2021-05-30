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

package com.epicnicity322.playmoresounds.bukkit.region;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public final class RegionManager
{
    protected static final @NotNull HashSet<SoundRegion> regions = new HashSet<>();
    private static ItemStack wand;

    static {
        Runnable regionUpdater = () -> {
            regions.clear();
            Path regionsFolder = PlayMoreSoundsCore.getFolder().resolve("Data").resolve("Regions");

            if (Files.exists(regionsFolder)) {
                try (Stream<Path> regionFiles = Files.list(regionsFolder)) {
                    regionFiles.forEach(regionFile -> {
                        try {
                            regions.add(new SoundRegion(regionFile));
                        } catch (IllegalArgumentException ignored) {
                            // Ignoring files that aren't considered valid regions.
                        }
                    });
                } catch (IOException ignored) {
                }
            }
        };

        Runnable wandUpdater = () -> {
            String material = null;

            try {
                ConfigurationSection wandSection = Configurations.CONFIG.getConfigurationHolder().getConfiguration().getConfigurationSection("Sound Regions.Wand");

                material = wandSection.getString("Material").orElse("FEATHER");

                ItemStack item = new ItemStack(Material.valueOf(material.toUpperCase()));
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        wandSection.getString("Name").orElse("&6&l&nRegion Selection Tool")));

                if (wandSection.getBoolean("Glowing").orElse(false))
                    meta.addEnchant(Enchantment.DURABILITY, 1, false);

                if (VersionUtils.hasItemFlags())
                    meta.addItemFlags(ItemFlag.values());

                item.setItemMeta(meta);
                wand = item;
            } catch (IllegalArgumentException ex) {
                PlayMoreSounds.getConsoleLogger().log("&cCouldn't get region wand. \"" + material + "\" is not a valid material. Please verify your configuration.");
                PlayMoreSoundsCore.getErrorHandler().report(ex, "Invalid material:");
            }
        };

        regionUpdater.run();
        PlayMoreSounds.addOnReloadRunnable(regionUpdater);
        PlayMoreSounds.addOnReloadRunnable(wandUpdater);
        PlayMoreSounds.addOnEnableRunnable(wandUpdater);
    }

    private RegionManager()
    {
    }

    public static @NotNull Set<SoundRegion> getAllRegions()
    {
        return Collections.unmodifiableSet(regions);
    }

    public static @Nullable ItemStack getWand()
    {
        if (wand == null)
            return null;
        else
            return wand.clone();
    }
}
