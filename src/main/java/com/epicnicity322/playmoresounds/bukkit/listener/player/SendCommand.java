package com.epicnicity322.playmoresounds.bukkit.listener.player;

import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class SendCommand implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e)
    {
        String command = e.getMessage();
        boolean play = true;

        for (String operation : PMSHelper.getConfig("commands").getKeys(false)) {
            if (!operation.equals("Version")) {
                for (String key : PMSHelper.getConfig("commands").getConfigurationSection(operation).getKeys(false)) {
                    if (parseCommand(operation, key, command)) {
                        ConfigurationSection section = PMSHelper.getConfig("commands").getConfigurationSection(
                                operation).getConfigurationSection(key);

                        if (section.getBoolean("Enabled")) {
                            if (!e.isCancelled() || !section.getBoolean("Cancellable")) {
                                if (section.contains("Sounds")) {
                                    new RichSound(section).play(e.getPlayer());
                                }

                                if (section.getBoolean("Stop Other Sounds.SoundsYML")) {
                                    play = false;
                                }
                                if (section.getBoolean("Stop Other Sounds.CommandsYML")) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (play) {
            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Send Command");
            RichSound sound = new RichSound(section);

            if (!e.isCancelled() || !sound.isCancellable()) {
                sound.play(e.getPlayer());
            }
        }
    }

    private boolean parseCommand(String operation, String key, String command)
    {
        switch (operation) {
            case "Starts With":
                if (command.startsWith(key)) {
                    return true;
                }

                break;
            case "Ends With":
                if (command.endsWith(key)) {
                    return true;
                }

                break;
            case "Contains":
                if (command.contains(key)) {
                    return true;
                }

                break;
            case "Equals Ignore Case":
                if (command.equalsIgnoreCase(key)) {
                    return true;
                }

                break;
            case "Equals Exactly":
                if (command.equals(key)) {
                    return true;
                }

                break;
        }

        return false;
    }
}
