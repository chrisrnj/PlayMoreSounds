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
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerToggleFlight extends PMSListener
{
    private final @NotNull PlayMoreSounds plugin;
    private RichSound stopSound;
    private RichSound startSound;

    public OnPlayerToggleFlight(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName()
    {
        return "Stop Flying|Start Flying";
    }

    @Override
    public void load()
    {
        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        ConfigurationSection stop = sounds.getConfigurationSection("Stop Flying");
        ConfigurationSection start = sounds.getConfigurationSection("Start Flying");
        boolean stopEnabled = stop != null && stop.getBoolean("Enabled").orElse(false);
        boolean startEnabled = start != null && start.getBoolean("Enabled").orElse(false);

        if (stopEnabled || startEnabled) {
            stopSound = new RichSound(stop);
            startSound = new RichSound(start);

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
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event)
    {
        Player player = event.getPlayer();
        RichSound sound;

        if (player.isFlying())
            sound = stopSound;
        else
            sound = startSound;

        if (!event.isCancelled() || !sound.isCancellable())
            sound.play(player);
    }
}
