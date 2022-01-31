/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2022 Christiano Rangel
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

import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
    //TODO: Fix this mess
    private static final @NotNull YamlConfigurationLoader loader = new YamlConfigurationLoader();
    private static final @NotNull Path regionsFolder = PlayMoreSoundsCore.getFolder().resolve("Data").resolve("Regions");
    private static final @NotNull Runnable regionUpdater;
    private static final @NotNull Runnable wandUpdater;
    private static final @NotNull HashSet<SoundRegion> regions = new HashSet<>();
    private static @NotNull Set<SoundRegion> unmodifiableRegions = Collections.unmodifiableSet(new HashSet<>());
    private static ItemStack wand;

    static {
        regionUpdater = () -> {
            regions.clear();

            if (Files.exists(regionsFolder)) {
                try (Stream<Path> regionFiles = Files.list(regionsFolder)) {
                    regionFiles.forEach(regionFile -> {
                        try {
                            regions.add(new SoundRegion(loader.load(regionFile)));
                        } catch (Exception ignored) {
                            // Ignoring files that aren't considered valid regions.
                        }
                    });
                } catch (IOException ignored) {
                }
            }

            unmodifiableRegions = Collections.unmodifiableSet(regions);
        };

        wandUpdater = () -> {
            String material = null;

            try {
                ConfigurationSection wandSection = ObjectUtils.getOrDefault(
                        Configurations.CONFIG.getConfigurationHolder().getConfiguration().getConfigurationSection("Sound Regions.Wand"),
                        Configurations.CONFIG.getConfigurationHolder().getDefaultConfiguration().getConfigurationSection("Sound Regions.Wand"));

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
        PlayMoreSounds.onReload(regionUpdater);
        PlayMoreSounds.onReload(wandUpdater);
        PlayMoreSounds.onEnable(wandUpdater);
    }

    private RegionManager()
    {
    }

    /**
     * Gets all regions saved in PlayMoreSounds' data folder based on the last time PlayMoreSounds was enabled or reloaded
     * by {@link PlayMoreSounds#reload()}.
     *
     * @return An unmodifiable set of PlayMoreSounds' regions.
     */
    public static @NotNull Set<SoundRegion> getRegions()
    {
        return unmodifiableRegions;
    }

    public static @NotNull HashSet<SoundRegion> getRegions(@NotNull Location location)
    {
        HashSet<SoundRegion> regionsInLocation = new HashSet<>();

        for (SoundRegion region : regions) {
            if (region.isInside(location)) regionsInLocation.add(region);
        }

        return regionsInLocation;
    }

    /**
     * Gets region selection tool based on the configuration keys from when the configuration was last loaded by {@link PlayMoreSounds#reload()}.
     *
     * @return An immutable {@link ItemStack} of the region selection tool.
     */
    public static @Nullable ItemStack getWand()
    {
        if (wand == null)
            return null;
        else
            return wand.clone();
    }

    /**
     * Saves a {@link SoundRegion} in PlayMoreSounds' data folder. If another region with the same {@link java.util.UUID}
     * was found there, it is deleted and replaced by this one.
     * <p>
     * Regions saved by this method are automatically added to {@link #getRegions()}.
     *
     * @param region The region to save.
     * @throws IllegalArgumentException If region is a sub-class of {@link SoundRegion}.
     */
    public static void save(@NotNull SoundRegion region) throws IOException
    {
        delete(region);

        Configuration data = new Configuration(loader);

        data.set("Name", region.getName());
        data.set("World", region.getMaxDiagonal().getWorld().getUID().toString());

        if (region.getCreator() != null)
            data.set("Creator", region.getCreator().toString());

        data.set("Creation Date", region.getCreationDate().toString());

        if (region.getDescription() != null)
            data.set("Description", region.getDescription());

        Location maxDiagonal = region.getMaxDiagonal();
        Location minDiagonal = region.getMinDiagonal();

        data.set("Diagonals.First.X", maxDiagonal.getBlockX());
        data.set("Diagonals.First.Y", maxDiagonal.getBlockY());
        data.set("Diagonals.First.Z", maxDiagonal.getBlockZ());
        data.set("Diagonals.Second.X", minDiagonal.getBlockX());
        data.set("Diagonals.Second.Y", minDiagonal.getBlockY());
        data.set("Diagonals.Second.Z", minDiagonal.getBlockZ());

        data.save(regionsFolder.resolve(region.getId() + ".yml"));
        regions.add(region);
        unmodifiableRegions = Collections.unmodifiableSet(regions);
    }

    /**
     * Removes the data of this region on PlayMoreSounds' data folder, if it exists.
     * <p>
     * Regions deleted by this method are automatically removed from {@link #getRegions()}.
     *
     * @param region The region to delete.
     * @throws IllegalArgumentException If region is a sub-class of {@link SoundRegion}.
     */
    public static void delete(@NotNull SoundRegion region) throws IOException
    {
        if (region.getClass() != SoundRegion.class)
            throw new IllegalArgumentException("Region is a sub-class of SoundRegion.");

        Files.deleteIfExists(regionsFolder.resolve(region.getId() + ".yml"));

        if (regions.remove(region))
            unmodifiableRegions = Collections.unmodifiableSet(regions);
    }

    /**
     * Loads {@link #getRegions()} and {@link #getWand()} based on the regions saved on PlayMoreSounds' data folder and
     * the wand keys on PlayMoreSounds' main config.
     */
    public static void reload()
    {
        regionUpdater.run();
        wandUpdater.run();
    }
}
