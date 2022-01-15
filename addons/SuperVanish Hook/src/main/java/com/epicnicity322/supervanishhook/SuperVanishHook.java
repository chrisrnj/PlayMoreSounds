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

package com.epicnicity322.supervanishhook;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.yamlhandler.Configuration;
import de.myzelyam.api.vanish.PlayerVanishStateChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.nio.file.Path;

public final class SuperVanishHook extends PMSAddon implements Listener
{
    private boolean registered = false;
    private PlayableRichSound vanishSound;
    private PlayableRichSound joinSound;
    private PlayableRichSound leaveSound;

    @Override
    protected void onStart()
    {
        if (!Bukkit.getPluginManager().isPluginEnabled("SuperVanish") && !Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
            PlayMoreSounds.getConsoleLogger().log("&cSuperVanish Hook addon depends on SuperVanish or PremiumVanish and both are absent.", ConsoleLogger.Level.WARN);
            PlayMoreSounds.getAddonManager().stopAddon(this);
            return;
        }

        Runnable runnable = () -> {
            Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
            Path path = Configurations.SOUNDS.getConfigurationHolder().getPath();

            if (!sounds.contains("Vanish Toggle")) {
                String data = "\n\n# When you vanish or unvanish yourself." +
                        "\n# Requires CMI Hook, Essentials Hook or SuperVanish Hook addons." +
                        "\nVanish Toggle:" +
                        "\n  Enabled: " + (SoundType.ENTITY_ELDER_GUARDIAN_CURSE.getSound().isPresent() ? "true" : "false # BLOCK_NOTE_BLOCK_CHIME is not available in " + PlayMoreSoundsCore.getServerVersion() + " please choose another sound.") +
                        "\n  Cancellable: true" +
                        "\n  # Plays Join Server sound when a player stops vanishing." +
                        "\n  Play Join Sound: true" +
                        "\n  # Plays Leave Server sound when a player vanishes." +
                        "\n  Play Leave Sound: true" +
                        "\n  Sounds:" +
                        "\n    '1':" +
                        "\n      Sound: ENTITY_ELDER_GUARDIAN_CURSE";

                try {
                    PathUtils.write(data, path);
                    PlayMoreSounds.getConsoleLogger().log("&eVanish Toggle sound added to sounds.yml.");
                } catch (Exception ex) {
                    PlayMoreSounds.getConsoleLogger().log("&cFailed to add Vanish Toggle sound to sounds.yml.", ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(ex, "Addon: SuperVanish Hook\nFailed to append Vanish Toggle:");
                }
            }

            try {
                Configurations.getConfigurationLoader().loadConfigurations();
            } catch (Exception ignored) {
            }

            boolean vanish = sounds.getBoolean("Vanish Toggle.Enabled").orElse(false) || sounds.getBoolean("Vanish Toggle.Play Join Server").orElse(false) || sounds.getBoolean("Vanish Toggle.Play Leave Server").orElse(false);

            synchronized (this) {
                if (vanish) {
                    if (sounds.getBoolean("Vanish Toggle.Enabled").orElse(false))
                        vanishSound = new PlayableRichSound(sounds.getConfigurationSection("Vanish Toggle"));
                    if (sounds.getBoolean("Vanish Toggle.Play Join Server").orElse(false) && sounds.contains("Join Server"))
                        joinSound = new PlayableRichSound(sounds.getConfigurationSection("Join Server"));
                    if (sounds.getBoolean("Vanish Toggle.Play Leave Server").orElse(false) && sounds.contains("Leave Server"))
                        leaveSound = new PlayableRichSound(sounds.getConfigurationSection("Leave Server"));

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerVanishStateChange(PlayerVanishStateChangeEvent event)
    {
        if (vanishSound != null) {
            Player player = Bukkit.getPlayer(event.getUUID());

            if (player != null) {
                if (!event.isCancelled())
                    if (event.isVanishing()) {
                        if (leaveSound != null)
                            leaveSound.play(player.getLocation());
                    } else {
                        if (joinSound != null)
                            joinSound.play(player.getLocation());
                    }

                if (!event.isCancelled() || !vanishSound.isCancellable())
                    vanishSound.play(player);
            }
        }
    }
}
