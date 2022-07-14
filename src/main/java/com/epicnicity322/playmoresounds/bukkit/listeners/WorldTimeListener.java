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

package com.epicnicity322.playmoresounds.bukkit.listeners;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class WorldTimeListener {
    private static @Nullable BukkitTask timeCheckerTask;

    private WorldTimeListener() {
    }

    public static void load(@NotNull PlayMoreSounds plugin) {
        final Configuration timeTriggersConfig;

        // Creeper sounds throughout the day to scare players on Halloween.
        if (PMSHelper.halloweenEvent()) {
            timeTriggersConfig = getHalloweenWorldTimeTriggersConfig();
        } else {
            timeTriggersConfig = Configurations.WORLD_TIME_TRIGGERS.getConfigurationHolder().getConfiguration();
        }

        // Defining and populating time triggers according to config.
        var timeTriggers = new HashMap<TimeTrigger, PlayableRichSound>();

        for (Map.Entry<String, Object> worldNode : timeTriggersConfig.getNodes().entrySet()) {
            if (!(worldNode.getValue() instanceof ConfigurationSection worldSection)) continue;

            World world = Bukkit.getWorld(worldNode.getKey());

            if (world == null) continue;
            for (Map.Entry<String, Object> timeNode : worldSection.getNodes().entrySet()) {
                if (!StringUtils.isNumeric(timeNode.getKey()) || !(timeNode.getValue() instanceof ConfigurationSection timeSection)
                        || !timeSection.getBoolean("Enabled").orElse(false)) {
                    continue;
                }

                try {
                    timeTriggers.put(new TimeTrigger(world, Long.parseLong(timeNode.getKey())), new PlayableRichSound(timeSection));
                } catch (NumberFormatException ignored) {
                } catch (IllegalArgumentException soundException) {
                    // This should never happen for #getHalloweenWorldTimeTriggersConfig.
                    PlayMoreSounds.getConsoleLogger().log("The sound " + timeSection.getPath() + " in config " +
                            Configurations.WORLD_TIME_TRIGGERS.getConfigurationHolder().getPath().getFileName().toString() +
                            " has a child sound with invalid namespaced key characters, so it was ignored.", ConsoleLogger.Level.WARN);
                }
            }
        }

        if (timeTriggers.isEmpty()) return;

        // Assigning world time checker task.
        synchronized (WorldTimeListener.class) {
            if (timeCheckerTask != null) {
                if (!timeCheckerTask.isCancelled()) timeCheckerTask.cancel();
                timeCheckerTask = null;
            }

            timeCheckerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                for (Map.Entry<TimeTrigger, PlayableRichSound> entry : timeTriggers.entrySet()) {
                    TimeTrigger trigger = entry.getKey();

                    if (trigger.time() == trigger.world().getTime()) {
                        entry.getValue().play(trigger.world().getSpawnLocation());
                    }
                }
            }, 0, 1);
        }
    }

    /**
     * A configuration that should be used instead of the default in Halloween, in case the user has enabled
     * 'Halloween Event' in config.
     *
     * @return A valid world time triggers configurations set to play with random creeper sounds throughout the day.
     */
    private static Configuration getHalloweenWorldTimeTriggersConfig() {
        var worldTimes = new Configuration(new YamlConfigurationLoader());
        var random = new Random();

        for (var world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                for (int i = 0; i < 2; ++i) {
                    int randomTime = random.nextInt(24001);

                    worldTimes.set(world.getName() + "." + randomTime + ".Enabled", true);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Delay", 0);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Sound", "ENTITY_CREEPER_PRIMED");
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Volume", 1);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Pitch", 1);
                    // Relative Location in case the user has Extra Options addon.
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Options.Relative Location.BACK", 2);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Options.Radius", -2);
                }
            }
        }

        return worldTimes;
    }

    record TimeTrigger(@NotNull World world, long time) {
    }
}
