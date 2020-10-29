package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundType;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;

public final class OnRegionEnterLeave extends PMSListener
{
    private final @NotNull HashMap<String, BukkitRunnable> regionsInLoop = new HashMap<>();
    private final @NotNull HashMap<String, HashSet<String>> soundsToStop = new HashMap<>();

    public OnRegionEnterLeave(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
    }

    @Override
    public @NotNull String getName()
    {
        return "Region Enter|Region Leave";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionEnter(RegionEnterEvent event)
    {
        Player player = event.getPlayer();
        ConfigurationSection regions = Configurations.REGIONS.getPluginConfig().getConfiguration()
                .getConfigurationSection("PlayMoreSounds");
        SoundRegion region = event.getRegion();
        boolean defaultSound = true;

        if (regions != null) {
            ConfigurationSection loop = regions.getConfigurationSection(region.getName() + ".Loop");
            boolean playEnterSound = true;

            if (loop != null) {
                RichSound loopSound = new RichSound(loop);

                if (!event.isCancelled() || !loopSound.isCancellable()) {
                    long delay = loop.getNumber("Delay").orElse(0).longValue();
                    long period = loop.getNumber("Period").orElse(0).longValue();

                    String key = region.getId() + ";" + player.getName();

                    if (regionsInLoop.containsKey(key))
                        regionsInLoop.get(key).cancel();

                    regionsInLoop.put(key, loopSound.playInLoop(player, player.getLocation(), delay, period, () -> {
                        Configuration updatedRegions = Configurations.REGIONS.getPluginConfig().getConfiguration();

                        return !updatedRegions.getBoolean("PlayMoreSounds." + region.getName() + ".Loop.Enabled").orElse(false)
                                || !RegionManager.getAllRegions().contains(region) || !player.isOnline() || !region.isInside(player.getLocation());
                    }));

                    stopOnExit(player, loop);

                    if (loop.getBoolean("Stop Other Sounds.Default Sound").orElse(false))
                        defaultSound = false;
                    if (loop.getBoolean("Stop Other Sounds.Enter Sound").orElse(false))
                        playEnterSound = false;
                }
            }

            if (playEnterSound) {
                ConfigurationSection enter = regions.getConfigurationSection(region.getName() + ".Enter");

                if (enter != null) {
                    RichSound enterSound = new RichSound(enter);

                    if (enterSound.isEnabled()) {
                        if (!event.isCancelled() || !enterSound.isCancellable()) {
                            enterSound.play(player);

                            stopOnExit(player, enter);

                            if (enter.getBoolean("Stop Other Sounds").orElse(false))
                                defaultSound = false;
                        }
                    }
                }
            }
        }

        if (defaultSound) {
            ConfigurationSection section = Configurations.SOUNDS.getPluginConfig().getConfiguration()
                    .getConfigurationSection("Region Enter");

            if (section != null) {
                RichSound sound = new RichSound(section);

                if (!event.isCancelled() || !sound.isCancellable()) {
                    sound.play(player);

                    stopOnExit(player, section);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionLeave(RegionLeaveEvent event)
    {
        Player player = event.getPlayer();

        soundsToStop.entrySet().removeIf(entry -> {
            String key = entry.getKey();

            if (key.startsWith(player.getName())) {
                long delay = Long.parseLong(key.substring(key.indexOf(";") + 1));

                SoundManager.stopSounds(player, entry.getValue(), delay);
                return true;
            }

            return false;
        });

        SoundRegion region = event.getRegion();
        String key = region.getName() + ";" + player.getName();

        if (regionsInLoop.containsKey(key)) {
            regionsInLoop.get(key).cancel();
            regionsInLoop.remove(key);
        }

        boolean defaultSound = true;

        ConfigurationSection leave = Configurations.REGIONS.getPluginConfig().getConfiguration().getConfigurationSection("PlayMoreSounds." + region.getName() + ".Enter");

        if (leave != null) {
            RichSound leaveSound = new RichSound(leave);

            if (leaveSound.isEnabled()) {
                if (!event.isCancelled() || !leaveSound.isCancellable()) {
                    leaveSound.play(player);

                    if (leave.getBoolean("Stop Other Sounds").orElse(false))
                        defaultSound = false;
                }
            }
        }

        if (defaultSound) {
            ConfigurationSection section = Configurations.SOUNDS.getPluginConfig().getConfiguration()
                    .getConfigurationSection("Region Leave");

            if (section != null) {
                RichSound sound = new RichSound(section);

                if (!event.isCancelled() || !sound.isCancellable())
                    sound.play(event.getPlayer());
            }
        }
    }

    private void stopOnExit(Player player, ConfigurationSection section)
    {
        if (section.getBoolean("Stop On Exit.Enabled").orElse(false)) {
            String key = player.getName() + ";" + section.getNumber("Stop On Exit.Delay").orElse(0);
            HashSet<String> sounds = soundsToStop.getOrDefault(key, new HashSet<>());
            ConfigurationSection soundsSection = section.getConfigurationSection("Sounds");

            if (soundsSection != null)
                for (String sound : soundsSection.getNodes().keySet()) {
                    String soundToStop = soundsSection.getString(sound + ".Sound").orElse("");

                    sounds.add(SoundManager.getSoundList().contains(soundToStop) ?
                            SoundType.valueOf(soundToStop).getSound().get() : soundToStop);
                }

            soundsToStop.put(key, sounds);
        }
    }
}