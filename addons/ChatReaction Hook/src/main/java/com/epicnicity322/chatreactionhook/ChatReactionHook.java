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

package com.epicnicity322.chatreactionhook;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.yamlhandler.Configuration;
import me.clip.chatreaction.events.ReactionFailEvent;
import me.clip.chatreaction.events.ReactionStartEvent;
import me.clip.chatreaction.events.ReactionWinEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.nio.file.Path;

public class ChatReactionHook extends PMSAddon implements Listener
{
    private boolean registered = false;
    private PlayableRichSound failSound;
    private PlayableRichSound startSound;
    private PlayableRichSound winSound;

    @Override
    protected void onStart()
    {
        Runnable runnable = () -> {
            Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
            Path path = Configurations.SOUNDS.getConfigurationHolder().getPath();
            String soundEnabled = SoundType.BLOCK_NOTE_BLOCK_CHIME.getSound().isPresent() ? "true" : "false # BLOCK_NOTE_BLOCK_CHIME is not available in " + PlayMoreSoundsCore.getServerVersion() + " please choose another sound.";

            if (!sounds.contains("Reaction Fail")) {
                String data = "\n\n# When no one unscrambles or types a word from ChatReaction fast enough." +
                        "\n# Requires ChatReaction Hook addon." +
                        "\n# This sound is not cancellable." +
                        "\nReaction Fail:" +
                        "\n  Enabled: " + soundEnabled +
                        "\n  Sounds:" +
                        "\n    '1':" +
                        "\n      Options:" +
                        "\n        Radius: -1.0" +
                        "\n      Pitch: 0.0" +
                        "\n      Sound: BLOCK_NOTE_BLOCK_CHIME";

                try {
                    PathUtils.write(data, path);
                    PlayMoreSounds.getConsoleLogger().log("&eReaction Fail sound added to sounds.yml.");
                } catch (Exception ex) {
                    PlayMoreSounds.getConsoleLogger().log("&cFailed to add Reaction Fail sound to sounds.yml.", ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(ex, "Addon: ChatReaction Hook\nFailed to append Reaction Fail:");
                }
            }

            if (!sounds.contains("Reaction Start")) {
                String data = "\n\n# When ChatReaction asks players to unscramble or type words." +
                        "\n# Requires ChatReaction Hook addon." +
                        "\n# This sound is cancellable." +
                        "\nReaction Start:" +
                        "\n  Enabled: " + soundEnabled +
                        "\n  Cancellable: true" +
                        "\n  Sounds:" +
                        "\n    '1':" +
                        "\n      Options:" +
                        "\n        Radius: -1.0" +
                        "\n      Sound: BLOCK_NOTE_BLOCK_CHIME";


                try {
                    PathUtils.write(data, path);
                    PlayMoreSounds.getConsoleLogger().log("&eReaction Start sound added to sounds.yml.");
                } catch (Exception ex) {
                    PlayMoreSounds.getConsoleLogger().log("&cFailed to add Reaction Start sound to sounds.yml.", ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(ex, "Addon: ChatReaction Hook\nFailed to append Reaction Start:");
                }
            }

            if (!sounds.contains("Reaction Win")) {
                String data = "\n\n# When you unscramble or type a word from ChatReaction fast enough." +
                        "\n# Requires ChatReaction Hook addon." +
                        "\n# This sound is not cancellable." +
                        "\nReaction Win:" +
                        "\n  Enabled: " + soundEnabled +
                        "\n  Sounds:" +
                        "\n    '1':" +
                        "\n      Pitch: 2.0" +
                        "\n      Sound: BLOCK_NOTE_BLOCK_CHIME";

                try {
                    PathUtils.write(data, path);
                    PlayMoreSounds.getConsoleLogger().log("&eReaction Win sound added to sounds.yml.");
                } catch (Exception ex) {
                    PlayMoreSounds.getConsoleLogger().log("&cFailed to add Reaction Win sound to sounds.yml.", ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(ex, "Addon: ChatReaction Hook\nFailed to append Reaction Win:");
                }
            }

            try {
                Configurations.getConfigurationLoader().loadConfigurations();
            } catch (Exception ignored) {
            }

            boolean fail = sounds.getBoolean("Reaction Fail.Enabled").orElse(false);
            boolean start = sounds.getBoolean("Reaction Start.Enabled").orElse(false);
            boolean win = sounds.getBoolean("Reaction Win.Enabled").orElse(false);

            synchronized (this) {
                if (fail || start || win) {
                    if (fail)
                        failSound = new PlayableRichSound(sounds.getConfigurationSection("Reaction Fail"));
                    if (start)
                        startSound = new PlayableRichSound(sounds.getConfigurationSection("Reaction Start"));
                    if (win)
                        winSound = new PlayableRichSound(sounds.getConfigurationSection("Reaction Win"));

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

    @EventHandler
    public void onReactionFail(ReactionFailEvent event)
    {
        if (failSound != null)
            failSound.play(Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onReactionStart(ReactionStartEvent event)
    {
        if (startSound != null && (!event.isCancelled() || !startSound.isCancellable()))
            startSound.play(Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    @EventHandler
    public void onReactionWin(ReactionWinEvent event)
    {
        if (winSound != null)
            winSound.play(event.getWinner());
    }
}
