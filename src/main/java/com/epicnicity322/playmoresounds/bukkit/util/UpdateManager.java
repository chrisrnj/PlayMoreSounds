package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.epicpluginlib.updater.Updater;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateManager
{
    public static boolean AVAILABLE = false;
    private static boolean loaded = false;
    private static boolean proceed = true;

    public static void loadUpdater()
    {
        if (PMSHelper.getConfig("config").getBoolean("Updater.Enabled")) {
            if (loaded) {
                return;
            } else {
                loaded = true;
            }

            check(Bukkit.getConsoleSender(), true);

            if (loaded) {
                long ticks = PMSHelper.formatTicks(PMSHelper.getConfig("config").getString("Updater.Period"));

                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        if (proceed) {
                            check(Bukkit.getConsoleSender(), PMSHelper.getConfig("config").getBoolean("Updater.Log"));
                        } else {
                            cancel();
                        }
                    }
                }.runTaskTimer(PlayMoreSounds.getPlugin(), ticks, ticks);
            }
        }
    }

    public static boolean check(CommandSender sender, boolean log)
    {
        if (log) {
            PlayMoreSounds.LOGGER.log(sender, "&6Checking for updates...");
        }

        if (AVAILABLE) {
            PlayMoreSounds.LOGGER.log(sender, "&2Update Available! To download use: &n/pms update download");

            return true;
        } else {
            Updater u = new Updater(PlayMoreSounds.JAR, PlayMoreSounds.PMS_VERSION, 37429);
            Updater.CheckResult result = u.check();

            switch (result.toString()) {
                case "AVAILABLE":
                    AVAILABLE = true;
                    PlayMoreSounds.LOGGER.log(sender, "&2UPDATE FOUND! Type &n/pms update download&r&2 to update.");
                    proceed = false;

                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            PlayMoreSounds.LOGGER.log(sender, "&2PMS has a new update available. Please download using /pms update download.");
                        }
                    }.runTaskTimer(PlayMoreSounds.getPlugin(), 12000, 12000);

                    break;

                case "OFFLINE":
                    if (log)
                        PlayMoreSounds.LOGGER.log(sender, "&cFailed: &eThe network is off or spigot is down.");

                    break;
                case "TIMEOUT":
                    if (log)
                        PlayMoreSounds.LOGGER.log(sender, "&cFailed: &eTook too long to connect to api.spiget.org.");

                    break;
                case "UNEXPECTED_ERROR":
                    PlayMoreSounds.LOGGER.log(sender, "&cSomething went wrong while checking for updates. Please report.");

                    break;
                default:
                    if (log)
                        PlayMoreSounds.LOGGER.log(sender, "&6No updates found.");
            }
        }

        return AVAILABLE;
    }
}
