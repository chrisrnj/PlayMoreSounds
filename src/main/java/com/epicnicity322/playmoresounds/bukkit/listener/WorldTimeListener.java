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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;

public final class WorldTimeListener
{
    private static final @NotNull HashMap<World, BukkitTask> runningWorlds = new HashMap<>();
    private static final @NotNull Random random = new Random();

    private WorldTimeListener()
    {
    }

    public static void load()
    {
        var worldTimes = Configurations.WORLD_TIME_TRIGGERS.getConfigurationHolder().getConfiguration();

        // Creeper sounds throughout the day to scare players on halloween.
        if (PMSHelper.halloweenEvent())
            worldTimes = getHalloweenWorldTimeTriggersConfig();

        for (World world : Bukkit.getWorlds()) {
            BukkitTask removed = runningWorlds.remove(world);

            if (removed != null) {
                removed.cancel();
            }

            var worldSection = worldTimes.getConfigurationSection(world.getName());

            if (worldSection != null) {
                var times = new HashMap<Long, PlayableRichSound>();

                // Filtering the nodes of worldSection to get only configuration sections that are numeric.
                worldSection.getNodes().forEach((key, value) -> {
                    // This exception will only be caught if the key is a long greater than Long#MAX_VALUE.
                    try {
                        if (StringUtils.isNumeric(key) && value instanceof ConfigurationSection section) {
                            var sound = PMSListener.getRichSound(section);
                            if (sound != null) times.put(Long.parseLong(key), sound);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                });

                runningWorlds.put(world, Bukkit.getScheduler().runTaskTimer(PlayMoreSounds.getInstance(), () -> {
                    long time = world.getTime();
                    PlayableRichSound sound = times.get(time);

                    if (sound != null)
                        sound.play(world.getSpawnLocation());
                }, 0, 1));
            }
        }
    }

    private static Configuration getHalloweenWorldTimeTriggersConfig()
    {
        var worldTimes = new Configuration(new YamlConfigurationLoader());

        for (var world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                for (int i = 0; i < 6; ++i) {
                    int randomTime = random.nextInt(24001);

                    worldTimes.set(world.getName() + "." + randomTime + ".Enabled", true);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Delay", 0);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Sound", "ENTITY_CREEPER_PRIMED");
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Volume", 10);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Pitch", 1);
                    // Relative Location in case the user has Extra Options addon.
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Options.Relative Location.BACK", 2);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Options.Radius", -2);
                }
            }
        }

        return worldTimes;
    }
}
