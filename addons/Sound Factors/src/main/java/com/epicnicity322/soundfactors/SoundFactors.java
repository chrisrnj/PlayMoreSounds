/*
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

package com.epicnicity322.soundfactors;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PrePlaySoundEvent;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

public class SoundFactors extends PMSAddon implements Listener
{
    @Override
    protected void onStart()
    {
        Bukkit.getPluginManager().registerEvents(this, PlayMoreSounds.getInstance());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPrePlaySound(PrePlaySoundEvent event)
    {
        ConfigurationSection section = event.getSound().getSection();

        if (section != null) {
            Location location = event.getLocation();
            ConfigurationSection timeFactor = section.getConfigurationSection("Time Factor");

            if (timeFactor != null) {
                Optional<Number> minTime = timeFactor.getNumber("Min Time");
                Optional<Number> maxTime = timeFactor.getNumber("Max Time");
                long currentTime = location.getWorld().getTime();

                if ((minTime.isPresent() && currentTime < minTime.get().longValue()) ||
                        (maxTime.isPresent() && currentTime > maxTime.get().longValue())) {
                    event.setCancelled(true);
                    return;
                }
            }

            ConfigurationSection heightFactor = section.getConfigurationSection("Height Factor");

            if (heightFactor != null) {
                Optional<Number> minY = heightFactor.getNumber("Min Y");
                Optional<Number> maxY = heightFactor.getNumber("Max Y");
                double currentY = location.getY();

                if ((minY.isPresent() && currentY < minY.get().doubleValue()) ||
                        (maxY.isPresent() && currentY > maxY.get().doubleValue()))
                    event.setCancelled(true);
            }
        }
    }
}
