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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerBedLeave extends PMSListener
{
    private final @NotNull PlayMoreSounds plugin;
    private RichSound bedLeave;
    private RichSound wakeUp;

    public OnPlayerBedLeave(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName()
    {
        return "Bed Leave|Wake Up";
    }

    @Override
    public void load()
    {
        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        ConfigurationSection leave = sounds.getConfigurationSection("Bed Leave");
        ConfigurationSection wake = sounds.getConfigurationSection("Wake Up");
        boolean leaveEnabled = leave != null && leave.getBoolean("Enabled").orElse(false);
        boolean wakeEnabled = wake != null && wake.getBoolean("Enabled").orElse(false);

        if (leaveEnabled || wakeEnabled) {
            if (leaveEnabled)
                bedLeave = new RichSound(leave);

            if (wakeEnabled)
                wakeUp = new RichSound(wake);

            if (!isLoaded()) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                setLoaded(true);
            }
        } else {
            if (isLoaded()) {
                HandlerList.unregisterAll(this);
                setLoaded(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBedLeave(PlayerBedLeaveEvent event)
    {
        Player player = event.getPlayer();

        if (bedLeave != null)
            bedLeave.play(player);

        if (wakeUp != null) {
            long time = player.getWorld().getTime();

            if (time < 300)
                wakeUp.play(player);
        }
    }
}
