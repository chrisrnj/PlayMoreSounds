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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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
    /**
     * Region IDs present in this set will be saved to data folder on {@link #saveAndUpdate()}.
     */
    static final @NotNull HashSet<String> regionsToSave = new HashSet<>();
    /**
     * Region IDs present in this set will be deleted on {@link #saveAndUpdate()}.
     */
    static final @NotNull HashSet<String> regionsToRemove = new HashSet<>();
    private static final @NotNull Path regionsFolder = PlayMoreSoundsCore.getFolder().resolve("Data").resolve("Regions");
    private static final @NotNull HashSet<SoundRegion> regions = new HashSet<>();
    private static final @NotNull Set<SoundRegion> unmodifiableRegions = Collections.unmodifiableSet(regions);
    private static final @NotNull Runnable wandUpdater;
    private static ItemStack wand;

    static {
        wandUpdater = () -> {
            var config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();

            String materialName = config.getString("Sound Regions.Wand.Material").orElse("FEATHER");
            var material = Material.getMaterial(materialName);

            if (material == null || material.isAir() || !material.isItem()) {
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
     * Adds a region to {@link #getRegions()}. Regions added by this method are automatically saved periodically and on
     * PlayMoreSounds disable.
     *
     * @param region The region object to be saved and managed by PlayMoreSounds.
     */
    public static void add(@NotNull SoundRegion region)
    {
        region.periodicallySave = true;
        regions.add(region);
        regionsToSave.add(region.getId().toString());
        loadAutoSave();
    }

    /**
     * Remove a region from {@link #getRegions()}. Once region updater task runs, this region will be removed from
     * PlayMoreSounds' data files.
     *
     * @param region The region to remove.
     */
    public static void remove(@NotNull SoundRegion region)
    {
        region.periodicallySave = false;
        regions.remove(region);
        regionsToRemove.add(region.getId().toString());
        loadAutoSave();
    }

    /**
     * Saves and removes regions that were scheduled in {@link #add(SoundRegion)} and {@link #remove(SoundRegion)} methods.
     * Then, it updates {@link #getRegions()} set with new {@link SoundRegion} instances based on the saved region files.
     */
    public static void saveAndUpdate()
    {
        var logger = PlayMoreSounds.getConsoleLogger();

        // Deleting regions scheduled to be removed.
        for (String id : regionsToRemove) {
            try {
                Files.deleteIfExists(regionsFolder.resolve(id + ".yml"));
            } catch (Exception e) {
                logger.log("Something went wrong while deleting region '" + id + "'.", ConsoleLogger.Level.ERROR);
                e.printStackTrace();
            }
        }
        regionsToRemove.clear();

        // Saving scheduled regions, and removing remaining regions in order to update them.
        regions.removeIf(region -> {
            String id = region.getId().toString();

            if (regionsToSave.contains(id)) {
                try {
                    save(region);
                    return false;
                } catch (Exception e) {
                    logger.log("Something went wrong while saving region '" + id + "'.", ConsoleLogger.Level.ERROR);
                    e.printStackTrace();
                    // Removing so it can attempt to load from last save, on the code below.
                    regionsToSave.remove(id);
                    return true;
                }
            } else {
                return true;
            }
        });

        // Updating instances of regions that weren't just saved. Regions files may change, for example: console might
        //want to edit region sounds. So, all instances of regions must be updated in case the files changed.
        if (Files.exists(regionsFolder)) {
            try (Stream<Path> regionFiles = Files.list(regionsFolder)) {
                var loader = new YamlConfigurationLoader();

                regionFiles.filter(regionFile -> regionFile.getFileName().toString().endsWith(".yml")).forEach(regionFile -> {
                    String name = regionFile.getFileName().toString();

                    // Don't want to re-add sound regions that were just saved.
                    if (regionsToSave.contains(name.substring(0, name.lastIndexOf(".")))) return;

                    try {
                        var region = new SoundRegion(loader.load(regionFile));
                        region.periodicallySave = true;
                        regions.add(region);
                    } catch (Exception e) {
                        logger.log("Error while reading region file \"" + name + "\": " + e.getMessage(), ConsoleLogger.Level.WARN);
                        logger.log("This region could not be loaded.", ConsoleLogger.Level.WARN);
                        PlayMoreSoundsCore.getErrorHandler().report(e, "File: " + name + "\nRegion instantiate from file exception:");
                    }
                });
            } catch (Exception e) {
                PlayMoreSounds.getConsoleLogger().log("Unable to read regions in Data/Regions folder.", ConsoleLogger.Level.ERROR);
                PlayMoreSoundsCore.getErrorHandler().report(e, "Region Read Exception:");
            }
        }

        regionsToSave.clear();
    }

    private static void save(@NotNull SoundRegion region) throws IOException
    {
        Files.deleteIfExists(regionsFolder.resolve(region.getId() + ".yml"));
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
    }

    private static void copySettings(ConfigurationSection section1, ConfigurationSection section2)
    {
        if (section1 == null || section2 == null) return;
        for (Map.Entry<String, Object> node : section1.getAbsoluteNodes().entrySet()) {
            section2.set(node.getKey(), node.getValue());
        }
    }

    private static @Nullable BukkitRunnable autoSaver;

    public static synchronized void loadAutoSave() {
        if (autoSaver != null) return;
        if (regionsToSave.isEmpty() && regionsToRemove.isEmpty()) return;

        PlayMoreSounds plugin = PlayMoreSounds.getInstance();

        if (plugin == null) return;

        autoSaver = new BukkitRunnable() {
            @Override
            public void run()
            {
                if (regionsToSave.isEmpty() && regionsToRemove.isEmpty()) {
                    cancel();
                    autoSaver = null;
                    return;
                }
                PlayMoreSounds.getConsoleLogger().log("Saving region changes...");
                saveAndUpdate();
            }
        };
        autoSaver.runTaskTimerAsynchronously(plugin, 0, 36000);
    }
}
