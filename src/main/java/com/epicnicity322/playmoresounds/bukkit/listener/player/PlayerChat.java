package com.epicnicity322.playmoresounds.bukkit.listener.player;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.regex.Pattern;

public class PlayerChat implements Listener
{
    private static boolean parseFilter(String filter, String messageKey, String messageSent)
    {
        switch (filter) {
            case "Starts With":
                return messageSent.startsWith(messageKey);
            case "Ends With":
                return messageSent.endsWith(messageKey);
            case "Contains SubString":
                return messageSent.toLowerCase().contains(messageKey.toLowerCase());
            case "Contains":
                return messageSent.toLowerCase().matches(".*\\b" + Pattern.quote(messageKey.toLowerCase()) + "\\b.*");
            case "Equals Ignore Case":
                return messageSent.equalsIgnoreCase(messageKey);
            case "Equals Exactly":
                return messageSent.equals(messageKey);
            default:
                return false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e)
    {
        String message = e.getMessage();
        FileConfiguration chat = PMSHelper.getConfig("chat");
        boolean soundsYML = true;

        outer:
        for (String filter : chat.getKeys(false)) {
            if (!filter.equalsIgnoreCase("version")) {
                for (String key : chat.getConfigurationSection(filter).getKeys(false)) {
                    ConfigurationSection section1 = chat.getConfigurationSection(filter).getConfigurationSection(key);

                    if (parseFilter(filter, section1.getString("Phrase"), message)) {
                        if (section1.getBoolean("Enabled")) {
                            if (!e.isCancelled() || !section1.getBoolean("Cancellable")) {
                                if (section1.contains("Sounds")) {
                                    new BukkitRunnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            new RichSound(section1).play(e.getPlayer());
                                        }
                                    }.runTaskLater(PlayMoreSounds.getPlugin(), 0);
                                }
                                if (section1.getBoolean("Stop Other Sounds.SoundsYML")) {
                                    soundsYML = false;
                                }
                                if (section1.getBoolean("Stop Other Sounds.ChatYML")) {
                                    break outer;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (soundsYML) {
            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Player Chat");
            RichSound sound = new RichSound(section);

            if (!e.isCancelled() || !sound.isCancellable()) {
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        sound.play(e.getPlayer());
                    }
                }.runTaskLater(PlayMoreSounds.getPlugin(), 0);
            }
        }
    }
}
