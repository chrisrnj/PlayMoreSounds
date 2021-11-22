package com.epicnicity322.naturesoundreplacer;

import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public final class NatureSoundReplacer extends PMSAddon implements Listener
{
    @Override
    protected void onStart()
    {
        Configurations.getConfigurationLoader().registerConfiguration(NatureSoundReplacerPacketAdapter.natureSoundReplacerConfig, new Version("4.0.0"), PlayMoreSoundsVersion.getVersion());
        PlayMoreSounds.getConsoleLogger().log("&eNature Sound Replacer configuration was registered.");

        // Running when server has fully started.
        Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> {
            Runnable loader = () -> NatureSoundReplacerPacketAdapter.loadNatureSoundReplacer(PlayMoreSounds.getInstance());

            loader.run();
            PlayMoreSounds.onReload(loader);

            PlayMoreSounds.getConsoleLogger().log("&aNature Sound Replacer was enabled successfully.");
        });
    }
}
