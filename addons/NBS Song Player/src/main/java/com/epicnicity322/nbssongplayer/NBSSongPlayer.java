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

package com.epicnicity322.nbssongplayer;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandLoader;
import com.epicnicity322.playmoresounds.bukkit.command.subcommands.ReloadSubCommand;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlaySoundEvent;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;

public final class NBSSongPlayer extends PMSAddon implements Listener
{
    private static final @NotNull Path nbsSongsFolder = PlayMoreSoundsCore.getFolder().resolve("Note Block Songs");
    private static final @NotNull HashMap<String, Song> songs = new HashMap<>();
    private static final @NotNull HashMap<String, RadioSongPlayer> playingSongs = new HashMap<>();
    private final @NotNull HashMap<String, HashSet<String>> soundsToStop = new HashMap<>();

    /**
     * Plays a nbs inside 'Note Block Songs' folder using NoteBlockAPI.
     *
     * @param player  The player to play the song.
     * @param nbsName The name of the nbs song.
     */
    public static void play(@NotNull Player player, @NotNull String nbsName)
    {
        if (songs.containsKey(nbsName)) {
            String key = player.getUniqueId() + ";" + nbsName;

            if (playingSongs.containsKey(key)) {
                RadioSongPlayer songPlayer = playingSongs.get(key);

                songPlayer.setPlaying(false);
                playingSongs.remove(key);
            }

            RadioSongPlayer songPlayer = new RadioSongPlayer(songs.get(nbsName));

            songPlayer.addPlayer(player);
            songPlayer.setPlaying(true);

            playingSongs.put(key, songPlayer);
        } else {
            throw new IllegalArgumentException(nbsName + " is not a valid note block song.");
        }
    }

    /**
     * Stops a note block song inside 'Note Block Songs' folder from playing using NoteBlockAPI.
     *
     * @param player  The player the song is playing.
     * @param nbsName The song name or null to stop all songs playing for this player.
     */
    public static void stop(@NotNull Player player, @Nullable String nbsName)
    {
        if (nbsName == null) {
            playingSongs.keySet().removeIf(key -> {
                if (key.startsWith(player.getUniqueId() + ";")) {
                    RadioSongPlayer songPlayer = playingSongs.get(key);

                    songPlayer.setPlaying(false);
                    return true;
                } else {
                    return false;
                }
            });
        } else if (songs.containsKey(nbsName)) {
            String key = player.getUniqueId() + ";" + nbsName;

            if (playingSongs.containsKey(key)) {
                RadioSongPlayer songPlayer = playingSongs.get(key);

                songPlayer.setPlaying(false);
                playingSongs.remove(key);
            }
        }
    }

    /**
     * @return An immutable set with names of the songs inside 'Note Block Songs' folder.
     */
    public static @NotNull HashSet<String> getSongNames()
    {
        return new HashSet<>(songs.keySet());
    }

    @Override
    protected void onStart()
    {
        Runnable runnable = () -> {
            for (Configurations language : Configurations.values()) {
                if (language.name().startsWith("LANGUAGE") && !language.getConfigurationHolder().getConfiguration().contains("NBS Song Player")) {
                    String data = "\n\nNBS Song Player:" +
                            "\n  Help: |-" +
                            "\n    &e/<label> nbssongplayer play|stop" +
                            "\n    &7 > Plays or stops a note block song." +
                            "\n  Play:" +
                            "\n    Success: '&7Playing the note block song \"&f<song>&7\" to &f<target>&7.'" +
                            "\n    Error:" +
                            "\n      Invalid Song: '&cA song with the name \"&7<song>&c\" was not found.'" +
                            "\n  Stop:" +
                            "\n    Success: '&7Stopping all note block songs to &f<target>&7.'";
                    try {
                        // Appending new defaults and reloading.
                        PathUtils.write(data, language.getConfigurationHolder().getPath());
                    } catch (Exception ignored) {
                    }
                }
            }

            try {
                Configurations.getConfigurationLoader().loadConfigurations();
            } catch (Exception ignored) {
            }

            songs.clear();

            if (Files.notExists(nbsSongsFolder)) {
                try {
                    Files.createDirectory(nbsSongsFolder);
                } catch (IOException e) {
                    PlayMoreSounds.getConsoleLogger().log("&cFailed to create NBS Songs folder.", ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(e, "Could not create 'NBS Songs' directory:");
                }
            }

            try {
                Files.list(nbsSongsFolder).filter(path -> path.getFileName().toString().endsWith(".nbs")).forEach(song -> {
                    String name = song.getFileName().toString();

                    songs.put(name.substring(0, name.lastIndexOf(".")), NBSDecoder.parse(song.toFile()));
                });
            } catch (IOException e) {
                PlayMoreSounds.getConsoleLogger().log("&cFailed to read NBS Songs folder.", ConsoleLogger.Level.WARN);
                PlayMoreSoundsCore.getErrorHandler().report(e, "Could not read NBS Songs files:");
            }
        };

        PlayMoreSounds.onReload(runnable);
        runnable.run();
        CommandLoader.addCommand(new NBSCommand());
        Bukkit.getPluginManager().registerEvents(this, PlayMoreSounds.getInstance());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlaySound(PlaySoundEvent event)
    {
        String sound = event.getSound().getSound();

        if (sound.toLowerCase().startsWith("nbs:")) {
            Player player = event.getPlayer();
            ConfigurationSection childSection = event.getSound().getSection();

            event.setCancelled(true);
            sound = sound.substring(4);

            try {
                play(player, sound);
            } catch (IllegalArgumentException e) {
                PlayMoreSounds.getConsoleLogger().log(e.getMessage() + (childSection == null ? "" : " on sound " + childSection.getPath()), ConsoleLogger.Level.WARN);
                PlayMoreSoundsCore.getErrorHandler().report(e, "Not in NBS folder:" + (childSection == null ? "" : " " + childSection.getPath()));
                return;
            }

            if (childSection != null) {
                Path optional = childSection.getRoot().getFilePath().orElse(null);

                if (optional != null && optional.equals(Configurations.REGIONS.getConfigurationHolder().getPath())) {
                    ConfigurationSection section = childSection.getParent().getParent();

                    if (!section.getName().equals("Leave") && section.getBoolean("Stop On Exit.Enabled").orElse(false)) {
                        String key = section.getParent().getName() + ";" + player.getUniqueId() + ";" + section.getNumber("Stop On Exit.Delay").orElse(0);
                        HashSet<String> sounds = soundsToStop.get(key);

                        if (sounds == null)
                            sounds = new HashSet<>();

                        sounds.add(sound);
                        soundsToStop.put(key, sounds);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionLeave(RegionLeaveEvent event)
    {
        Player player = event.getPlayer();
        SoundRegion region = event.getRegion();
        String key = region.getName() + ";" + player.getUniqueId();

        soundsToStop.entrySet().removeIf(entry -> {
            String stopKey = entry.getKey();
            HashSet<String> toStop = entry.getValue();

            if (stopKey.startsWith(key)) {
                long delay = Long.parseLong(stopKey.substring(stopKey.lastIndexOf(";") + 1));

                Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), () -> {
                    for (String sound : toStop)
                        stop(player, sound);
                }, delay);

                return true;
            }

            return false;
        });
    }
}
