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

package com.epicnicity322.playmoresounds.bukkit.region;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.subcommand.ReloadSubCommand;
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
            Path regionsFolder = PlayMoreSounds.getFolder().resolve("Data").resolve("Regions");

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
                ConfigurationSection wandSection = Configurations.CONFIG.getPluginConfig().getConfiguration().getConfigurationSection("Sound Regions.Wand");

                material = wandSection.getString("Material").orElse("FEATHER");

                ItemStack item = new ItemStack(Material.valueOf(material.toUpperCase()));
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        wandSection.getString("Name").orElse("&6&l&nRegion Selection Tool")));

                if (wandSection.getBoolean("Glowing").orElse(false))
                    meta.addEnchant(Enchantment.DURABILITY, 1, false);

                meta.addItemFlags(ItemFlag.values());
                item.setItemMeta(meta);
                wand = item;
            } catch (IllegalArgumentException ex) {
                PlayMoreSounds.getPMSLogger().log("&cCouldn't get region wand. \"" + material + "\" is not a valid material. Please verify your configuration.");
                PlayMoreSounds.getErrorLogger().report(ex, "Invalid material:");
            }
        };

        regionUpdater.run();
        ReloadSubCommand.addOnReloadRunnable(regionUpdater);
        ReloadSubCommand.addOnReloadRunnable(wandUpdater);
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
