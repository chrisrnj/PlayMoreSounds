/*
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

package com.epicnicity322.authmehook;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlayRichSoundEvent;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import fr.xephi.authme.events.AuthMeAsyncPreLoginEvent;
import fr.xephi.authme.events.AuthMeAsyncPreRegisterEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class AuthMeHook extends PMSAddon implements Listener
{
    private static final @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
    private boolean registered = false;
    private boolean preventJoin = false;
    private PlayableRichSound loginSound;
    private PlayableRichSound registerSound;

    @Override
    protected void onStart()
    {
        Runnable runnable = () -> {
            Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
            Path path = Configurations.SOUNDS.getConfigurationHolder().getPath();

            if (!sounds.contains("AuthMe Login")) {
                String data = "\n\n# When you login using AuthMe." +
                        "\n# Requires AuthMe Hook addon." +
                        "\n# This sound is cancellable" +
                        "\nAuthMe Login:" +
                        "\n  Enabled: true" +
                        "\n  Cancellable: true" +
                        "\n  # Prevents Join Server sound from playing." +
                        "\n  Prevent Join Server: true" +
                        "\n  Sounds:" +
                        "\n    '1':" +
                        "\n      Options:" +
                        "\n        Radius: -1.0" +
                        "\n      Sound: BLOCK_NOTE_BLOCK_PLING";

                try {
                    PathUtils.write(data, path);
                    PlayMoreSounds.getConsoleLogger().log("&eAuthMe Login sound added to sounds.yml.");
                } catch (Exception ex) {
                    PlayMoreSounds.getConsoleLogger().log("&cFailed to add AuthMe Login sound to sounds.yml.", ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(ex, "Addon: AuthMe Hook\nFailed to append AuthMe Login:");
                }
            }

            if (!sounds.contains("AuthMe Register")) {
                String data = "\n\n# When you register using AuthMe." +
                        "\n# Requires AuthMe Hook addon." +
                        "\n# This sound is cancellable" +
                        "\nAuthMe Register:" +
                        "\n  Enabled: true" +
                        "\n  Cancellable: true" +
                        "\n  Sounds:" +
                        "\n    '1':" +
                        "\n      Pitch: 2.0" +
                        "\n      Sound: BLOCK_NOTE_BLOCK_PLING";

                try {
                    PathUtils.write(data, path);
                    PlayMoreSounds.getConsoleLogger().log("&eAuthMe Register sound added to sounds.yml.");
                } catch (Exception ex) {
                    PlayMoreSounds.getConsoleLogger().log("&cFailed to add AuthMe Register sound to sounds.yml.", ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(ex, "Addon: AuthMe Hook\nFailed to append AuthMe Register:");
                }
            }

            try {
                Configurations.getConfigurationLoader().loadConfigurations();
            } catch (Exception ignored) {
            }

            boolean login = sounds.getBoolean("AuthMe Login.Enabled").orElse(false);
            boolean register = sounds.getBoolean("AuthMe Register.Enabled").orElse(false);

            synchronized (this) {
                preventJoin = sounds.getBoolean("AuthMe Login.Prevent Join Server").orElse(false);

                if (preventJoin || login || register) {
                    if (login)
                        loginSound = new PlayableRichSound(sounds.getConfigurationSection("AuthMe Login"));
                    if (register)
                        registerSound = new PlayableRichSound(sounds.getConfigurationSection("AuthMe Register"));

                    if (!registered) {
                        Bukkit.getPluginManager().registerEvents(this, PlayMoreSounds.getInstance());
                        registered = true;
                    }
                } else {
                    if (registered) {
                        HandlerList.unregisterAll(this);
                        registered = false;
                    }
                }
            }
        };

        runnable.run();
        PlayMoreSounds.onReload(runnable);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayRichSound(PlayRichSoundEvent event)
    {
        if (!preventJoin) return;

        ConfigurationSection section = event.getRichSound().getSection();

        if (section != null && section.getRoot().equals(Configurations.SOUNDS.getConfigurationHolder().getConfiguration()) &&
                section.getPath().equals("Join Server")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAuthMeAsyncPreLogin(AuthMeAsyncPreLoginEvent event)
    {
        if (loginSound != null && (event.canLogin() || !loginSound.isCancellable()))
            // Can't play sounds outside bukkit main thread.
            scheduler.runTask(PlayMoreSounds.getInstance(), () -> loginSound.play(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAuthMeAsyncPreRegister(AuthMeAsyncPreRegisterEvent event)
    {
        if (registerSound != null && (event.canRegister() || !registerSound.isCancellable()))
            // Can't play sounds outside bukkit main thread.
            scheduler.runTask(PlayMoreSounds.getInstance(), () -> registerSound.play(event.getPlayer()));
    }
}
