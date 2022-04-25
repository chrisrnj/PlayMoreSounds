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

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public final class RegionManager
{
    private static final @NotNull Path regionsFolder = PlayMoreSoundsCore.getFolder().resolve("Data").resolve("Regions");
    private static final @NotNull HashSet<SoundRegion> regions = new HashSet<>();
    private static final @NotNull Set<SoundRegion> unmodifiableRegions = Collections.unmodifiableSet(regions);
    private static final @NotNull Runnable regionUpdater;
    private static final @NotNull Runnable wandUpdater;
    private static ItemStack wand;

    static {
        regionUpdater = () -> {
            regions.clear();

            if (Files.exists(regionsFolder)) {
                try (Stream<Path> regionFiles = Files.list(regionsFolder)) {
                    regionFiles.forEach(regionFile -> {
                        try {
                            regions.add(new SoundRegion(new YamlConfigurationLoader().load(regionFile)));
                        } catch (Exception e) {
                            PlayMoreSounds.getConsoleLogger().log("Error when reading region file \"" + regionFile.getFileName().toString() + "\": " + e.getMessage());
                            PlayMoreSoundsCore.getErrorHandler().report(e, "Region instantiate from file exception:");
                        }
                    });
                } catch (IOException e) {
                    PlayMoreSounds.getConsoleLogger().log("Unable to read regions in Data/Regions folder.", ConsoleLogger.Level.ERROR);
                    PlayMoreSoundsCore.getErrorHandler().report(e, "Region Read Exception:");
                }
            }
        };

        wandUpdater = () -> {
            var config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();

            String materialName = config.getString("Sound Regions.Wand.Material").orElse("FEATHER");
            var material = Material.getMaterial(materialName);

            if (material == null || material.isAir()) {
                PlayMoreSounds.getConsoleLogger().log("&cRegion wand has an invalid material: " + material + ". Using default FEATHER.");
                material = Material.FEATHER;
            }

            var item = new ItemStack(material);
            var meta = item.getItemMeta();

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    config.getString("Sound Regions.Wand.Name").orElse("&6&l&nRegion Selection Tool")));

            if (config.getBoolean("Sound Regions.Wand.Glowing").orElse(false))
                meta.addEnchant(Enchantment.DURABILITY, 1, false);

            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            wand = item;
        };

        regionUpdater.run();
        wandUpdater.run();
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

    /**
     * Gets all regions that are in this location.
     *
     * @param location The location to check if there's a region.
     * @return The regions in this location.
     */
    public static @NotNull Set<SoundRegion> getRegionsAt(@NotNull Location location)
    {
        return regions.stream().filter(region -> region.isInside(location)).collect(Collectors.toSet());
    }

    /**
     * Gets all regions made by the specified creator.
     *
     * @param creator The creator's {@link UUID}, null for console.
     * @return The regions made by this creator.
     */
    public static @NotNull Set<SoundRegion> getRegionsOf(@Nullable UUID creator)
    {
        return regions.stream().filter(region -> Objects.equals(region.getCreator(), creator)).collect(Collectors.toSet());
    }

    /**
     * Gets region selection tool based on the configuration keys from when the configuration was last loaded by {@link PlayMoreSounds#reload()}.
     *
     * @return An immutable {@link ItemStack} of the region selection tool.
     */
    public static @NotNull ItemStack getWand()
    {
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

        Configuration data = new Configuration(new YamlConfigurationLoader());

        data.set("Name", region.getName());
        data.set("World", region.getMaxDiagonal().getWorld().getUID().toString());

        if (region.getCreator() != null)
            data.set("Creator", region.getCreator().toString());

        data.set("Creation Date", region.getCreationDate().toString());

        if (region.getDescription() != null)
            data.set("Description", region.getDescription());

        Location maxDiagonal = region.getMaxDiagonal();
        Location minDiagonal = region.getMinDiagonal();

        data.set("Diagonals.Max.X", maxDiagonal.getBlockX());
        data.set("Diagonals.Max.Y", maxDiagonal.getBlockY());
        data.set("Diagonals.Max.Z", maxDiagonal.getBlockZ());
        data.set("Diagonals.Min.X", minDiagonal.getBlockX());
        data.set("Diagonals.Min.Y", minDiagonal.getBlockY());
        data.set("Diagonals.Min.Z", minDiagonal.getBlockZ());
        // Copying region sounds to the configuration, in case there are any.
        if (region.getEnterSound() != null) copySettings(region.getEnterSound().getSection(), data);
        if (region.getLeaveSound() != null) copySettings(region.getLeaveSound().getSection(), data);
        if (region.getLoopSound() != null) copySettings(region.getLoopSound().getSection(), data);

        data.save(regionsFolder.resolve(region.getId() + ".yml"));
        regions.add(region);
    }

    private static void copySettings(ConfigurationSection section1, ConfigurationSection section2)
    {
        if (section1 == null || section2 == null) return;
        for (Map.Entry<String, Object> node : section1.getAbsoluteNodes().entrySet()) {
            section2.set(node.getKey(), node.getValue());
        }
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
        regions.remove(region);
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
